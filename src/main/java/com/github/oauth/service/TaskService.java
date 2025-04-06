package com.github.oauth.service;

import com.github.oauth.model.Task;
import com.github.oauth.model.User;

public interface TaskService {
    String assignTask(Task task, User creator);

    String updateTask(Task task, User assignedUser);

    String deleteTask(String taskId, User user);
}
