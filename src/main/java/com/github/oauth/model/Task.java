package com.github.oauth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private Long assignedTo;
    private Long projectId;
    private String details;
    private String status;
    private String id;

    public Task(Long assignedTo, Long projectId, String details) {
        this.assignedTo = assignedTo;
        this.projectId = projectId;
        this.details = details;
        this.status = "REQUESTED";
        this.id = assignedTo + "_" + details + "_" + projectId;
    }
}

