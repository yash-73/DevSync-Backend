package com.github.oauth.service.impl;

import com.github.oauth.model.AppRole;
import com.github.oauth.model.Role;
import com.github.oauth.model.User;

import com.github.oauth.exception.ResourceNotFound;
import com.github.oauth.repository.RoleRepository;
import com.github.oauth.repository.UserRepository;
import com.github.oauth.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;
import com.github.oauth.repository.TechRepository;
import com.github.oauth.model.Tech;
import com.github.oauth.model.Project;
import com.github.oauth.payload.ProjectDTO;
import com.github.oauth.payload.UserDTO;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TechRepository techRepository;
    
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
            TechRepository techRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.techRepository = techRepository;
       
        this.modelMapper = modelMapper;
    }

    @Transactional
    public void updateEmail(Authentication authentication, String newEmail) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            throw new IllegalArgumentException("User not authenticated");
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Object githubIdObj = oAuth2User.getAttribute("id");
        if (githubIdObj == null) {
            throw new IllegalArgumentException("GitHub ID not found in authentication");
        }
        String githubId = githubIdObj.toString();

        userRepository.updateEmailByGithubId(githubId, newEmail);
    }

    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            throw new IllegalArgumentException("User not authenticated");
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Object githubIdObj = oAuth2User.getAttribute("id");
        if (githubIdObj == null) {
            throw new IllegalArgumentException("GitHub ID not found in authentication");
        }
        String githubId = githubIdObj.toString();

        return userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public void addRoleToUser(Authentication authentication, AppRole roleName) {
        User user = getCurrentUser(authentication);

        // Find or create the role
        Role role = roleRepository.findByRoleName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role(roleName);
                    return roleRepository.save(newRole);
                });

        // Add role to user if not already present
        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            userRepository.save(user);
        }
    }

    @Override
    public Set<Tech> addTech(Set<String> techStack, User user) {
        Set<Tech> existingTechStack = user.getTechStack();
        techStack.forEach(
                tech -> {
                    Tech foundTech = techRepository.findByTechName(tech);
                    if (foundTech == null)
                        throw new ResourceNotFound("Tech not found" + tech);
                    else
                        existingTechStack.add(foundTech);
                });
        user.setTechStack(existingTechStack);
        userRepository.save(user);
        return existingTechStack;
    }

    @Override
    @Transactional
    public Set<Tech> removeTech(String technology, User user) {

        Set<Tech> existingTechStack = user.getTechStack();
        Tech techToRemove = techRepository.findByTechName(technology);
        if (techToRemove == null)
            throw new ResourceNotFound("Tech not found " + technology);
        else
            existingTechStack.remove(techToRemove);
        user.setTechStack(existingTechStack);
        return existingTechStack;
    }

    @Override
    public List<ProjectDTO> getMyCreatedProjects(User user) {
        Set<Project> createdProjects = user.getCreatedProjects();
        return createdProjects.stream()
                .map(project -> {
                    ProjectDTO projectDTO = modelMapper.map(project, ProjectDTO.class);
                    projectDTO.setTechStack(
                            project.getTechStack().stream()
                                    .map(Tech::getTechName)
                                    .collect(Collectors.toSet()));
                    projectDTO.setCreatorId(project.getCreator().getId());
                    return projectDTO;
                })
                .toList();
    }

    @Override
    public Set<Tech> getTechStack(User user) {
        return user.getTechStack();
    }

    @Override
    public List<ProjectDTO> getProjects(User user) {
        Set<Project> projects = user.getProjects();
        return projects.stream()
                .map(project -> {
                    ProjectDTO projectDTO = modelMapper.map(project, ProjectDTO.class);
                    projectDTO.setTechStack(
                            project.getTechStack().stream()
                                    .map(Tech::getTechName)
                                    .collect(Collectors.toSet()));
                    projectDTO.setCreatorId(project.getCreator().getId());
                    return projectDTO;
                })
                .toList();
    }

    @Override
    public UserDTO getUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setLogin(user.getLogin());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setAvatarUrl(user.getAvatarUrl());
        userDTO.setGithubId(user.getGithubId());

        // Convert roles to string names
        userDTO.setRoles(user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toSet()));

        // Convert tech stack to string names
        userDTO.setTechStack(user.getTechStack().stream()
                .map(Tech::getTechName)
                .collect(Collectors.toSet()));

        return userDTO;
    }

    @Override
    public UserDTO getUserDTOById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFound("User not found with id: " + userId));
        return getUserDTO(user);
    }

    @Override
    public List<ProjectDTO> getCreatedProjectsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFound("User not found with id: " + userId));
        
        Set<Project> createdProjects = user.getCreatedProjects();
        return createdProjects.stream()
                .map(project -> {
                    ProjectDTO projectDTO = modelMapper.map(project, ProjectDTO.class);
                    projectDTO.setTechStack(
                            project.getTechStack().stream()
                                    .map(Tech::getTechName)
                                    .collect(Collectors.toSet()));
                    projectDTO.setCreatorId(project.getCreator().getId());
                    return projectDTO;
                })
                .toList();
    }

    @Override
    public List<ProjectDTO> getJoinedProjectsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFound("User not found with id: " + userId));
        
        Set<Project> joinedProjects = user.getProjects();
        return joinedProjects.stream()
                .map(project -> {
                    ProjectDTO projectDTO = modelMapper.map(project, ProjectDTO.class);
                    projectDTO.setTechStack(
                            project.getTechStack().stream()
                                    .map(Tech::getTechName)
                                    .collect(Collectors.toSet()));
                    projectDTO.setCreatorId(project.getCreator().getId());
                    return projectDTO;
                })
                .toList();
    }

}