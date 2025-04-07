package com.github.oauth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectJoinRequest {

    private Long projectId;
    private String status;
    private Long userId;
    private Date timeStamp;

    public ProjectJoinRequest(Long projectId){
        this.projectId = projectId;
    }

}
