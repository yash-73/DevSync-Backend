package com.github.oauth.service;

import com.github.oauth.model.User;
import com.github.oauth.payload.ProjectDTO;
import jakarta.transaction.Transactional;

public interface ProjectService {

    @Transactional
    ProjectDTO createNewProject(ProjectDTO projectDTO, User user);


    ProjectDTO updateProject(ProjectDTO projectDTO, User user);

    String deleteProject(Long projectId, User user);
}
