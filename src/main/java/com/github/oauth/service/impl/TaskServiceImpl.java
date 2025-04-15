package com.github.oauth.service.impl;

import com.google.cloud.firestore.*;
import com.google.api.core.ApiFuture;
import com.github.oauth.exception.GeneralException;
import com.github.oauth.exception.ResourceNotFound;
import com.github.oauth.model.Project;
import com.github.oauth.model.Task;
import com.github.oauth.model.User;
import com.github.oauth.repository.ProjectRepository;
import com.github.oauth.repository.UserRepository;
import com.github.oauth.service.TaskService;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;


@Service
public class TaskServiceImpl implements TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);
    private final Firestore firestore;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public TaskServiceImpl(Firestore firestore, UserRepository userRepository, ProjectRepository projectRepository) {
        this.firestore = firestore;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public Task assignTask(Task task, User creator) {
        Long projectId = task.getProjectId();
        if (projectId == null) throw new GeneralException("Project ID is null");

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFound("Project not found with projectId: " + projectId));

        if (!project.getCreator().getId().equals(creator.getId()))
            throw new GeneralException("You are not authorized to assign tasks in this project");

        User assignedUser = userRepository.findById((task.getAssignedTo()))
                .orElseThrow(() -> new ResourceNotFound("User not found with ID: " + task.getAssignedTo()));

        if (!project.getMembers().contains(assignedUser))
            throw new GeneralException("Assigned user is not a member of the project");

        try {
            task.setStatus("REQUESTED");
            task.setId(task.getAssignedTo() + "_" + task.getDetails() + "_" + task.getProjectId());

            DocumentReference docRef = firestore.collection("Tasks").document(task.getId());
            docRef.set(task).get();
            return task;
        } catch (Exception e) {
            throw new GeneralException("Error assigning task: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Task updateTaskStatus(Task task, User assignedUser) {
        if (task.getId() == null) throw new GeneralException("Task ID is null");

        if (!task.getAssignedTo().equals(assignedUser.getId()))
            throw new GeneralException("Task was not assigned to you");

        try {
            DocumentReference docRef = firestore.collection("Tasks").document(task.getId());
            DocumentSnapshot document = docRef.get().get();

            if (!document.exists()) {
                throw new ResourceNotFound("Task not found");
            }

            String currentStatus = document.getString("status");
            String newStatus = task.getStatus();

            String pullRequestUrl = task.getPullRequestUrl();
            if(pullRequestUrl != null) {
                docRef.set(Map.of("pullRequestUrl", pullRequestUrl), SetOptions.merge()).get();
            }

            // Validate status transitions
            if (currentStatus != null && currentStatus.equals("REQUESTED")) {
                if (!newStatus.equals("PENDING") && !newStatus.equals("REJECTED")) {
                    throw new GeneralException("Invalid status transition from REQUESTED");
                }
            } else if (currentStatus != null && currentStatus.equals("PENDING")) {
                if (!newStatus.equals("REQUEST_COMPLETE") && !newStatus.equals("REJECTED")) {
                    throw new GeneralException("Invalid status transition from PENDING");
                }
            } else {
                throw new GeneralException("Invalid current status for update");
            }

            // Update status
            docRef.set(Map.of("status", newStatus), SetOptions.merge()).get();
            task.setStatus(newStatus);
            return task;
        } catch (Exception e) {
            throw new GeneralException("Failed to update task: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Task updateTaskCompletion(Task task, User creator) {
        if (task.getId() == null) throw new GeneralException("Task ID is null");

        try {
            DocumentReference docRef = firestore.collection("Tasks").document(task.getId());
            DocumentSnapshot document = docRef.get().get();

            if (!document.exists()) {
                throw new ResourceNotFound("Task not found");
            }

            // Verify creator
            Project project = projectRepository.findById(task.getProjectId())
                    .orElseThrow(() -> new ResourceNotFound("Project not found"));
            
            if (!project.getCreator().getId().equals(creator.getId())) {
                throw new GeneralException("Only the project creator can update task completion");
            }

            String currentStatus = document.getString("status");
            String newStatus = task.getStatus();

            // Validate status transitions
            if (currentStatus != null && !currentStatus.equals("REQUEST_COMPLETE")) {
                throw new GeneralException("Task must be in REQUEST_COMPLETE status");
            }

            if (newStatus != null && !newStatus.equals("COMPLETED") && !newStatus.equals("REQUEST_REJECTED")) {
                throw new GeneralException("Invalid status transition from REQUEST_COMPLETE");
            }

            // Update status
            docRef.set(Map.of("status", newStatus), SetOptions.merge()).get();
            task.setStatus(newStatus);
            return task;
        } catch (Exception e) {
            throw new GeneralException("Failed to update task completion: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteTask(String taskId, User user) {
        if (taskId == null) throw new GeneralException("Task ID is null");

        try {
            DocumentReference docRef = firestore.collection("Tasks").document(taskId);
            DocumentSnapshot document = docRef.get().get();

            if (!document.exists()) {
                throw new ResourceNotFound("Task not found");
            }

            // Verify user is either creator or assigned user
            Project project = projectRepository.findById(document.getLong("projectId"))
                    .orElseThrow(() -> new ResourceNotFound("Project not found"));

            if (!project.getCreator().getId().equals(user.getId())) {
                Long assignedTo = document.getLong("assignedTo");
                if (assignedTo == null || !assignedTo.equals(user.getId())) {
                    throw new GeneralException("You are not authorized to delete this task");
                }   
            }
            docRef.delete().get();
        } catch (Exception e) {
            throw new GeneralException("Failed to delete task: " + e.getMessage());
        }
    }

    @Override
    public List<Task> getTasksByStatus(String status) {
        try {
            logger.info("Getting tasks with status: {}", status);
            // Query Firebase for tasks with specific status
            CollectionReference tasksCollection = firestore.collection("Tasks");
            Query query = tasksCollection.whereEqualTo("status", status);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();

            List<Task> tasks = new ArrayList<>();
            QuerySnapshot snapshot = querySnapshot.get();
            logger.info("Found {} tasks with status {}", snapshot.size(), status);

            for (DocumentSnapshot document : snapshot.getDocuments()) {
                try {
                    logger.debug("Processing document: {}", document.getId());
                    Task task = document.toObject(Task.class);
                    if (task != null) {
                        logger.debug("Task fields - assignedTo: {}, details: {}, projectId: {}", 
                            task.getAssignedTo(), task.getDetails(), task.getProjectId());
                        // Set the ID using our custom format
                        task.setId(task.getAssignedTo() + "_" + task.getDetails() + "_" + task.getProjectId());
                        tasks.add(task);
                    } else {
                        logger.warn("Failed to convert document to Task object: {}", document.getId());
                    }
                } catch (Exception e) {
                    logger.error("Error processing document {}: {}", document.getId(), e.getMessage(), e);
                }
            }
            logger.info("Successfully retrieved {} tasks", tasks.size());
            return tasks;
        } catch (Exception e) {
            logger.error("Error getting tasks by status: {}", e.getMessage(), e);
            logger.error("Stack trace: ", e);
            throw new RuntimeException("Failed to get tasks by status: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateLastChecked(String taskId) {
        try {
            DocumentReference taskRef = firestore.collection("Tasks").document(taskId);
            taskRef.update("lastChecked", com.google.cloud.Timestamp.now());
        } catch (Exception e) {
            logger.error("Error updating last checked timestamp for task {}: {}", taskId, e.getMessage());
            throw new RuntimeException("Failed to update last checked timestamp", e);
        }
    }

    @Override
    public Task getTaskById(String taskId) {
        try {
            DocumentReference taskRef = firestore.collection("Tasks").document(taskId);
            DocumentSnapshot document = taskRef.get().get();
            
            if (document.exists()) {
                Task task = document.toObject(Task.class);
                if (task != null) {
                    task.setId(document.getId());
                    return task;
                }
            }
            throw new ResourceNotFound("Task not found with ID: " + taskId);
        } catch (Exception e) {
            logger.error("Error getting task by ID: {}", e.getMessage());
            throw new RuntimeException("Failed to get task by ID", e);
        }
    }

    @Override
    public Task updateTaskStatusById(String taskId, String status) {
        try {
            DocumentReference docRef = firestore.collection("Tasks").document(taskId);
            DocumentSnapshot document = docRef.get().get();

            if (!document.exists()) {
                throw new ResourceNotFound("Task not found with ID: " + taskId);
            }

            // Update status
            docRef.set(Map.of("status", status), SetOptions.merge()).get();
            
            // Return updated task
            Task task = document.toObject(Task.class);
            if (task != null) {
                task.setId(document.getId());
                task.setStatus(status);
            }
            return task;
        } catch (Exception e) {
            logger.error("Error updating task status: {}", e.getMessage());
            throw new RuntimeException("Failed to update task status", e);
        }
    }

    @Override
    public Task findById(String id) {
        try {
            // Parse the id to get assignedTo and projectId
            String[] parts = id.split("_");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid task ID format");
            }
            String assignedToStr = parts[0];
            String projectId = parts[2];
            
            // Query the task
            Query query = firestore.collection("tasks")
                .whereEqualTo("assignedTo", Long.parseLong(assignedToStr))
                .whereEqualTo("projectId", projectId)
                .whereEqualTo("details", parts[1]);
            
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
            
            if (documents.isEmpty()) {
                throw new RuntimeException("Task not found");
            }
            
            return documents.get(0).toObject(Task.class);
        } catch (Exception e) {
            logger.error("Error finding task by id: {}", e.getMessage());
            throw new RuntimeException("Failed to find task", e);
        }
    }

    @Override
    public String getTaskCreatorAccessToken(String taskId) {
        try {
            DocumentReference docRef = firestore.collection("Tasks").document(taskId);
            DocumentSnapshot document = docRef.get().get();

            if (!document.exists()) {
                throw new ResourceNotFound("Task not found");
            }

            // Get the project ID from the task
            Long projectId = document.getLong("projectId");
            if (projectId == null) {
                throw new GeneralException("Project ID not found in task");
            }

            // Get the project to find the creator
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFound("Project not found"));

            // Get the creator's access token
            User creator = project.getCreator();
            if (creator == null || creator.getAccessToken() == null) {
                throw new GeneralException("Creator's access token not found");
            }

            return creator.getAccessToken();
        } catch (Exception e) {
            logger.error("Error getting task creator's access token: {}", e.getMessage());
            throw new RuntimeException("Failed to get task creator's access token", e);
        }
    }
}