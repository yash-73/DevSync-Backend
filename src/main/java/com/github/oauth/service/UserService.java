package com.github.oauth.service;

import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import com.github.oauth.model.AppRole;
import com.github.oauth.model.Tech;
import com.github.oauth.model.User;
import com.github.oauth.payload.ProjectDTO;
import com.github.oauth.payload.UserDTO;

public interface UserService {

    Set<Tech> addTech(Set<String> techStack, User user);

    @Transactional
    Set<Tech> removeTech(String technology, User user);

    List<ProjectDTO> getMyCreatedProjects(User user);

    void updateEmail(Authentication authentication, String newEmail);

    User getCurrentUser(Authentication authentication);

    void addRoleToUser(Authentication authentication, AppRole roleName);

    Set<Tech> getTechStack(User user);

    List<ProjectDTO> getProjects(User user);

    UserDTO getUserDTO(User user);

    UserDTO getUserDTOById(Long userId);

    List<ProjectDTO> getCreatedProjectsByUserId(Long userId);

    List<ProjectDTO> getJoinedProjectsByUserId(Long userId);
}
