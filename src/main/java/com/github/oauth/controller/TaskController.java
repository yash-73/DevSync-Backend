package com.github.oauth.controller;

import com.github.oauth.model.Task;
import com.github.oauth.model.User;
import com.github.oauth.service.TaskService;
import com.github.oauth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/task")
@RequiredArgsConstructor
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private final TaskService taskService;
    private final UserService userService;

    @PostMapping("/assign")
    public ResponseEntity<?> assignTask(Authentication authentication, @RequestBody Task task) {
        try {
            User creator = userService.getCurrentUser(authentication);
            Task assignedTask = taskService.assignTask(task, creator);
            logger.info("Task assigned successfully by user: {}", creator.getLogin());
            return new ResponseEntity<>(assignedTask, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to assign task: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error assigning task", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PutMapping("/status")
    public ResponseEntity<?> updateTaskStatus(Authentication authentication, @RequestBody Task task) {
        try {
            User assignedUser = userService.getCurrentUser(authentication);
            Task updatedTask = taskService.updateTaskStatus(task, assignedUser);
            logger.info("Task status updated successfully by user: {}", assignedUser.getLogin());
            return new ResponseEntity<>(updatedTask, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update task status: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating task status", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PutMapping("/completion")
    public ResponseEntity<?> updateTaskCompletion(Authentication authentication, @RequestBody Task task) {
        try {
            User creator = userService.getCurrentUser(authentication);
            Task updatedTask = taskService.updateTaskCompletion(task, creator);
            logger.info("Task completion updated successfully by creator: {}", creator.getLogin());
            return new ResponseEntity<>(updatedTask, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update task completion: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating task completion", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteTask(Authentication authentication, @RequestBody Map<String, String> request) {
        try {
            String taskId = request.get("taskId");
            logger.info("Deleting task with ID: {}", taskId);
            if (taskId == null) {
                throw new IllegalArgumentException("Task ID is required");
            }
            
            User user = userService.getCurrentUser(authentication);
            taskService.deleteTask(taskId, user);
            logger.info("Task deleted successfully by user: {}", user.getLogin());
            return new ResponseEntity<>("Task deleted successfully", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to delete task: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting task", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}
