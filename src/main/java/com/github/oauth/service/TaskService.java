package com.github.oauth.service;

import com.github.oauth.model.Task;
import com.github.oauth.model.User;

public interface TaskService {
    String assignTask(Task task, User creator);

    String updateTaskStatus(Task task, User assignedUser);

    String updateTaskCompletion(Task task, User creator);

    String deleteTask(String taskId, User user);
}
