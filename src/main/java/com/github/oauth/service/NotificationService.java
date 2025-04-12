package com.github.oauth.service;


import com.github.oauth.model.ProjectJoinRequest;
import com.github.oauth.model.User;


public interface NotificationService {

    public String addRequest(ProjectJoinRequest joinRequest, User user);

    public String updateRequest(ProjectJoinRequest joinRequest, User creator);


    String deleteRequest(Long userId, Long projectId, User user);

    String deleteOwnRequest(Long projectId, User user);
}
