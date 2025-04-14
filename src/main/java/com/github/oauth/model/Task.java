package com.github.oauth.model;

import com.google.cloud.Timestamp;
import lombok.Data;


@Data
public class Task {
    private String id;
    private Long assignedTo;
    private Long projectId;
    private String details;
    private String status;
    private String pullRequestUrl;
    private Timestamp lastChecked;
    private Timestamp createdAt;

    public Task() {
        this.status = "REQUESTED";
        this.createdAt = Timestamp.now();
    }
}

