package com.github.oauth.service;

import com.github.oauth.model.User;
import com.github.oauth.payload.ProjectDTO;
import com.github.oauth.payload.UserDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface ProjectService {

    @Transactional
    ProjectDTO createNewProject(ProjectDTO projectDTO, User user);

    @Transactional
    ProjectDTO updateProject(ProjectDTO projectDTO, User user);

    @Transactional
    String deleteProject(Long projectId, User user);

    List<ProjectDTO> searchProjectsByTechStack(Set<String> techNames);

    ProjectDTO getProjectById(Long projectId);

    List<UserDTO> getProjectMembers(Long projectId);

    // private ProjectDTO convertToDTO(Project project) {
    // ProjectDTO dto = new ProjectDTO();
    // dto.setProjectId(project.getProjectId());
    // dto.setProjectName(project.getProjectName());
    // dto.setDescription(project.getDescription());
    // dto.setGithubRepository(project.getGithubRepository());
    // dto.setProjectStatus(project.getProjectStatus());
    // dto.setCreatorId(project.getCreator().getId());
    // dto.setTechStack(project.getTechStack().stream()
    // .map(Tech::getTechName)
    // .collect(Collectors.toSet()));
    // return dto;
    // }
}
