package com.github.oauth.service;


import com.google.cloud.firestore.*;
import com.github.oauth.exception.GeneralException;
import com.github.oauth.exception.ResourceNotFound;
import com.github.oauth.model.Project;
import com.github.oauth.model.Task;
import com.github.oauth.model.User;
import com.github.oauth.repository.ProjectRepository;
import com.github.oauth.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class TaskServiceImpl implements  TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final Firestore firestore;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public TaskServiceImpl(Firestore firestore, UserRepository userRepository, ProjectRepository projectRepository) {
        this.firestore = firestore;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

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
            task.setStatus("PENDING");
            task.setId(task.getAssignedTo() + "_" + task.getDetails() + "_" + task.getProjectId());

            DocumentReference docRef = firestore.collection("Tasks").document(task.getId());
            WriteResult result = docRef.set(task).get();
            return "Task assigned successfully with updateTime: " + result.getUpdateTime();
        } catch (Exception e) {
            throw new GeneralException("Error assigning task: " + e.getMessage());
        }
    }

    @Transactional
    public String updateTask(Task task, User assignedUser) {
        if (task.getId() == null) throw new GeneralException("Task ID is null");


        if(!task.getAssignedTo().equals(assignedUser.getId()))
            throw new GeneralException("Task was not assigned to you");

        try {
            DocumentReference docRef = firestore.collection("ProjectTasks").document(task.getId());

            docRef.set(Map.of("status", task.getStatus()), SetOptions.merge()).get();
            return "Task updated successfully";
        } catch (Exception e) {
            throw new GeneralException("Failed to update task: " + e.getMessage());
        }
    }


    @Transactional
    public String deleteTask(String taskId, User user) {
        if (taskId == null) throw new GeneralException("Task ID is null");

        try {
            DocumentReference docRef = firestore.collection("ProjectTasks").document(taskId);
            docRef.delete().get();
            return "Task deleted successfully";
        } catch (Exception e) {
            throw new GeneralException("Failed to delete task: " + e.getMessage());
        }
    }
}