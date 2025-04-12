package com.github.oauth.service.impl;

import com.google.cloud.firestore.*;
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
    public String assignTask(Task task, User creator) {
        Long projectId = task.getProjectId();
        if (projectId == null) throw new GeneralException("Project ID is null");

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFound("Project not found with projectId: " + projectId));

        if (!project.getCreator().getId().equals(creator.getId()))
            return "You are not authorized to assign tasks in this project";

        User assignedUser = userRepository.findById(task.getAssignedTo())
                .orElseThrow(() -> new ResourceNotFound("User not found with ID: " + task.getAssignedTo()));

        if (!project.getMembers().contains(assignedUser))
            throw new GeneralException("Assigned user is not a member of the project");

        try {
            task.setStatus("REQUESTED");
            task.setId(task.getAssignedTo() + "_" + task.getDetails() + "_" + task.getProjectId());

            DocumentReference docRef = firestore.collection("Tasks").document(task.getId());
            WriteResult result = docRef.set(task).get();
            return "Task assigned successfully with updateTime: " + result.getUpdateTime();
        } catch (Exception e) {
            throw new GeneralException("Error assigning task: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String updateTaskStatus(Task task, User assignedUser) {
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

            // Validate status transitions
            if (currentStatus.equals("REQUESTED")) {
                if (!newStatus.equals("PENDING") && !newStatus.equals("REJECTED")) {
                    throw new GeneralException("Invalid status transition from REQUESTED");
                }
            } else if (currentStatus.equals("PENDING")) {
                if (!newStatus.equals("REQUEST_COMPLETE") && !newStatus.equals("REJECTED")) {
                    throw new GeneralException("Invalid status transition from PENDING");
                }
            } else {
                throw new GeneralException("Invalid current status for update");
            }

            // Update status
            docRef.set(Map.of("status", newStatus), SetOptions.merge()).get();

            // Delete task if rejected
            if (newStatus.equals("REJECTED")) {
                docRef.delete().get();
                return "Task rejected and deleted successfully";
            }

            return "Task status updated successfully";
        } catch (Exception e) {
            throw new GeneralException("Failed to update task: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String updateTaskCompletion(Task task, User creator) {
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
            if (!currentStatus.equals("REQUEST_COMPLETE")) {
                throw new GeneralException("Task must be in REQUEST_COMPLETE status");
            }

            if (!newStatus.equals("COMPLETED") && !newStatus.equals("REQUEST_REJECTED")) {
                throw new GeneralException("Invalid status transition from REQUEST_COMPLETE");
            }

            // Update status
            docRef.set(Map.of("status", newStatus), SetOptions.merge()).get();

            // Delete task if request is rejected
            if (newStatus.equals("REQUEST_REJECTED")) {
                docRef.delete().get();
                return "Task completion rejected and task deleted successfully";
            }

            return "Task marked as completed successfully";
        } catch (Exception e) {
            throw new GeneralException("Failed to update task completion: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String deleteTask(String taskId, User user) {
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

            if (!project.getCreator().getId().equals(user.getId()) && 
                !document.getLong("assignedTo").equals(user.getId())) {
                throw new GeneralException("You are not authorized to delete this task");
            }

            docRef.delete().get();
            return "Task deleted successfully";
        } catch (Exception e) {
            throw new GeneralException("Failed to delete task: " + e.getMessage());
        }
    }
}