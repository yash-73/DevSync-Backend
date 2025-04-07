package com.github.oauth.service.impl;


import com.google.cloud.firestore.Firestore;
import com.github.oauth.exception.ResourceNotFound;
import com.github.oauth.model.*;

import com.github.oauth.payload.ProjectDTO;
import com.github.oauth.repository.ProjectRepository;
import com.github.oauth.repository.TechRepository;


import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import java.util.List;
import java.util.stream.Collectors;

import com.github.oauth.service.ProjectService;


@Service
public class ProjectServiceImpl implements  ProjectService{

    private ProjectRepository projectRepository;

    private ModelMapper modelMapper;

    private TechRepository techRepository;

    private Firestore firestore;

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);




    public ProjectServiceImpl(ProjectRepository projectRepository,ModelMapper modelMapper
    , TechRepository techRepository, Firestore firestore){
        this.projectRepository = projectRepository;
        this.modelMapper = modelMapper;
        this.techRepository = techRepository;
        this.firestore = firestore;

    }




    @Override
    @Transactional
    public ProjectDTO createNewProject(ProjectDTO projectDTO, User user) {

        Project project  = new Project();
        project.setProjectName(projectDTO.getProjectName()); //projectName
        project.setCreator(user); //creator
        project.setProjectStatus(ProjectStatus.OPEN); //projectStatus
        project.setDescription(projectDTO.getDescription()); //description
        project.setGithubRepository(projectDTO.getGithubRepository()); //githubRepository

        Set<User> members = new HashSet<>();
        members.add(user);
        project.setMembers(members); //members


        Set<Tech> techStack = new HashSet<>();
        projectDTO.getTechStack().forEach(
                tech -> {
                    Tech foundTech = techRepository.findByTechName(tech);
                    if (foundTech == null) throw new ResourceNotFound("Tech not found: "+tech);
                    else techStack.add(foundTech);
                }
        );
        project.setTechStack(techStack); //techStack
        Project savedProject = projectRepository.save(project); //Save Project

        user.getCreatedProjects().add(savedProject); //bi-directional mapping
        user.getProjects().add(savedProject); //bi-directional mapping

        ProjectDTO savedProjectDTO = modelMapper.map(savedProject, ProjectDTO.class);
        savedProjectDTO.setTechStack(projectDTO.getTechStack());



        return savedProjectDTO;

    }

    @Override
    public ProjectDTO updateProject(ProjectDTO projectDTO, User user) {
        Project project = projectRepository.findById(projectDTO.getProjectId())
                .orElseThrow(()-> new ResourceNotFound("Project not found with projectId "+ projectDTO.getProjectId()));

        if(project.getCreator().getId().equals(user.getId())){
            project.setProjectName(projectDTO.getProjectName()); //projectName
            project.setDescription(projectDTO.getDescription()); // description
            Set<Tech> stack = new HashSet<>();
            projectDTO.getTechStack().forEach(
                    tech -> {
                        Tech technology = techRepository.findByTechName(tech);
                        if (technology == null) throw new ResourceNotFound("Tech not found " + tech);
                        else stack.add(technology);
                    }
            );
            project.setTechStack(stack); //techStack
            project.setGithubRepository(projectDTO.getGithubRepository());

            projectRepository.save(project);
            return projectDTO;
            }

        else {
            throw new RuntimeException("User with userId "+ user.getId() +
                    " is not the creator of the project with projectId "+ projectDTO.getProjectId());
        }
    }

    @Override
    @Transactional
    public String deleteProject(Long projectId, User user) {

        Project project = projectRepository.findById(projectId)
                        .orElseThrow(()-> new ResourceNotFound("Project not found with projectId: "+projectId));

        if(project.getCreator().getId().equals(user.getId())){

            projectRepository.delete(project);
            user.getProjects().remove(project);
            user.getCreatedProjects().remove(project);
            return "Project deleted";
        }

        else return "You are not the creator of the project";
    }

    @Transactional
    public List<ProjectDTO> searchProjectsByTechStack(Set<String> techNames) {
        try {
            // Convert tech names to Tech entities
            Set<Tech> techStack = techNames.stream()
                .map(name -> {
                    Tech tech = techRepository.findByTechName(name);
                    if(tech == null) throw new ResourceNotFound("Tech not found " + name);
                    return tech;
                })
                .collect(Collectors.toSet());

            // Search projects with matching tech stack
            List<Project> projects = projectRepository.findByTechStackOrderByMatchCount(techStack);

            // Convert to DTOs
            return projects.stream()
                .map(project -> modelMapper.map(project, ProjectDTO.class))
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error searching projects by tech stack", e);
            throw new RuntimeException("Failed to search projects by tech stack", e);
        }
    }


}