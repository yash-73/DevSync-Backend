package com.github.oauth.service.impl;



import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import com.github.oauth.exception.GeneralException;
import com.github.oauth.exception.ResourceNotFound;
import com.github.oauth.model.Project;
import com.github.oauth.model.ProjectJoinRequest;
import com.github.oauth.model.User;
import com.github.oauth.repository.ProjectRepository;
import com.github.oauth.repository.UserRepository;
import com.github.oauth.service.NotificationService;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;


@Service
public class NotificationServiceImpl implements  NotificationService{

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private Firestore firestore;

    private UserRepository userRepository;

    private ProjectRepository projectRepository;

    public NotificationServiceImpl(Firestore firestore, UserRepository userRepository,  ProjectRepository projectRepository){
        this.userRepository = userRepository;
        this.firestore = firestore;
        this.projectRepository = projectRepository;
    }

    @Override
    public String addRequest(ProjectJoinRequest joinRequest, User user) {

        Long projectId = joinRequest.getProjectId();
        if(projectId == null) throw new GeneralException("Project Id is null");

        Project project = projectRepository.findById(projectId)
                    .orElseThrow(()-> new ResourceNotFound("Project not found with projectId: "+ projectId));

        if(project.getMembers().contains(user)) throw new GeneralException("User already part of the project");

        try {
            // Check if user has already sent a "PENDING" request
            Query query = firestore.collection("ProjectJoinRequests")
                    .whereEqualTo("projectId", projectId)
                    .whereEqualTo("userId", user.getId())
                    .whereEqualTo("status", "PENDING");

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            if (!querySnapshot.get().isEmpty()) {
                return "You have already sent a join request for this project.";
            }

            // If both conditions pass, create a new join request
            joinRequest.setStatus("PENDING");
            joinRequest.setUserId(user.getId());
            joinRequest.setTimeStamp(new Date());

            String docId = user.getId() + "_" + projectId;

            DocumentReference documentReference = firestore.collection("ProjectJoinRequests").document(docId);
            WriteResult result = documentReference.set(joinRequest).get();

            return "Document saved with updateTime: " + result.getUpdateTime() + " customId: "+docId;
        }
        catch (Exception e) {
            System.out.println("Notification service exception: " + e.getMessage());
            throw new GeneralException("Error processing join request");
        }

    }

    @Override
    @Transactional
    public String updateRequest(ProjectJoinRequest joinRequest, User creator) {
        Long projectId = joinRequest.getProjectId();
        if (projectId == null) throw new GeneralException("Project Id is null");

        logger.info("Fetching project with ID: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFound("Project not found with projectId " + projectId));

        logger.info("Checking if creator is valid");
        if (!project.getCreator().getId().equals(creator.getId()))
            return "You are not the creator of the project";

        logger.info("Fetching user with ID: {}", joinRequest.getUserId());
        User user = userRepository.findById(joinRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFound("User not found with userId: " + joinRequest.getUserId()));

        logger.info("Checking if user is already in the project");
        if (project.getMembers().contains(user))
            return "User is already a member of the project";


        if(joinRequest.getStatus().equals("ACCEPTED")){
            logger.info("Adding user to project members");
            project.getMembers().add(user);
            projectRepository.save(project);
            user.getProjects().add(project);
        }

        logger.info("Updating FireStore...");

        try {
            String docId = joinRequest.getUserId() + "_" + projectId;
            DocumentReference docRef = firestore.collection("ProjectJoinRequests").document(docId);
            //Update status as ACCEPTED or REJECTED
            String requestStatus = joinRequest.getStatus();
            if(requestStatus == null) throw new Exception("Request is null");

            docRef.set(Map.of("status", requestStatus), SetOptions.merge()).get();
            System.out.println("Firestore update successful!");
        } catch (Exception e) {
            throw new GeneralException("Failed to update Firestore: " + e.getMessage());
        }

        return "User successfully added to project";
    }

    @Override
    @Transactional
    public String deleteRequest(Long userId, Long projectId, User user) {

            if(projectId == null) throw new GeneralException("Project ID was null");

            Project project = projectRepository.findById(projectId)
                    .orElseThrow(()-> new ResourceNotFound("Project not found with projectId: "+ projectId));

        if (!userId.equals(user.getId()))
            return "You are not authorized to delete this request";

        logger.info("Deleting Firestore document...");
        try {
            String docId = userId + "_" + projectId;
            DocumentReference docRef = firestore.collection("ProjectJoinRequests").document(docId);

            // Delete document from Firestore
            docRef.delete().get();
            logger.info("Firestore document deleted successfully!");

        } catch (Exception e) {
            throw new GeneralException("Failed to delete Firestore document: " + e.getMessage());
        }

        return "Request successfully deleted";
    }
}