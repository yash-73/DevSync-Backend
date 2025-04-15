package com.github.oauth.controller;

import com.github.oauth.model.User;
import com.github.oauth.payload.ProjectDTO;
import com.github.oauth.payload.UserDTO;
import com.github.oauth.service.ProjectService;
import com.github.oauth.service.UserService;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.github.oauth.exception.ResourceNotFound;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private final UserService userService;
    private final ProjectService projectService;

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
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (ResourceNotFound e) {
            logger.warn("Project not found: {}", e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting project", e);
            return ResponseEntity.status(500).body("Failed to delete project: " + e.getMessage());
        }
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchProjectsByTechStack(Authentication authentication,
            @RequestBody Set<String> techStack) {
        try {
            // Validate authentication
            userService.getCurrentUser(authentication);

            // Search projects
            List<ProjectDTO> projects = projectService.searchProjectsByTechStack(techStack);
            logger.info("Found {} projects matching tech stack: {}", projects.size(), techStack);
            return ResponseEntity.ok(projects);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to search projects: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error searching projects", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProjectById(Authentication authentication, @PathVariable Long projectId) {
        try {
            // Validate authentication
            userService.getCurrentUser(authentication);

            // Get project
            ProjectDTO project = projectService.getProjectById(projectId);
            logger.info("Retrieved project with ID: {}", projectId);
            return ResponseEntity.ok(project);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to get project: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (ResourceNotFound e) {
            logger.warn("Project not found: {}", e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error getting project", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<?> getProjectMembers(Authentication authentication, @PathVariable Long projectId) {
        try {
            // Validate authentication
            userService.getCurrentUser(authentication);

            // Get project members
            List<UserDTO> members = projectService.getProjectMembers(projectId);
            logger.info("Retrieved {} members for project ID: {}", members.size(), projectId);
            return ResponseEntity.ok(members);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to get project members: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (ResourceNotFound e) {
            logger.warn("Project not found: {}", e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error getting project members", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}