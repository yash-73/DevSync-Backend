package com.github.oauth.controller;


import com.github.oauth.model.User;
import com.github.oauth.payload.ProjectDTO;
import com.github.oauth.service.ProjectService;
import com.github.oauth.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")

public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private final UserService userService;
    private final ProjectService projectService;

    public ProjectController(UserService userService, ProjectService projectService){
        this.userService = userService;
        this.projectService = projectService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createProject(Authentication authentication, @RequestBody ProjectDTO projectDTO) {
        try {
            User user = userService.getCurrentUser(authentication);
            ProjectDTO newProject = projectService.createNewProject(projectDTO, user);
            logger.info("Project created successfully by user: {}", user.getLogin());
            return new ResponseEntity<>(newProject, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create project: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating project", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/createdProjects")
    public ResponseEntity<?> getMyCreatedProjects(Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            List<ProjectDTO> myCreatedProjects = userService.getMyCreatedProjects(user);
            logger.info("Retrieved created projects for user: {}", user.getLogin());
            return ResponseEntity.ok(myCreatedProjects);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to get created projects: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error getting created projects", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateMyProject(Authentication authentication, @RequestBody ProjectDTO projectDTO) {
        try {
            User user = userService.getCurrentUser(authentication);
            ProjectDTO updatedProject = projectService.updateProject(projectDTO, user);
            logger.info("Project updated successfully by user: {}", user.getLogin());
            return new ResponseEntity<>(updatedProject, HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update project: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating project", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @DeleteMapping("/delete/{projectId}")
    public ResponseEntity<?> deleteProject(Authentication authentication, @PathVariable Long projectId) {
        try {
            User user = userService.getCurrentUser(authentication);
            String result = projectService.deleteProject(projectId, user);
            logger.info("Project deleted successfully by user: {}", user.getLogin());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to delete project: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting project", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}