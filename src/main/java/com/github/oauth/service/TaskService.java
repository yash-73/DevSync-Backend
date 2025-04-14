package com.github.oauth.service;

import com.github.oauth.model.Task;
import com.github.oauth.model.User;
import java.util.List;

public interface TaskService {
    Task assignTask(Task task, User creator);

    Task updateTaskStatus(Task task, User assignedUser);

    Task updateTaskCompletion(Task task, User creator);

    void deleteTask(String taskId, User user);

    List<Task> getTasksByStatus(String status);

    void updateLastChecked(String taskId);

    Task getTaskById(String taskId);

    Task updateTaskStatusById(String taskId, String status);

    Task findById(String id);

    String getTaskCreatorAccessToken(String taskId);
}
