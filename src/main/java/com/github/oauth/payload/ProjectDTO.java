package com.github.oauth.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDTO {
    private Long projectId;
    private String projectName;
    private String description;
    private Set<String> techStack = new HashSet<>();
    private String githubRepository;
    private Long creatorId;

    @Override
    public String toString() {
        return "ProjectDTO{" +
                "projectId='" + projectId + '\'' +
                ", projectName='" + projectName + '\'' +
                ", description='" + description + '\'' +
                ", techStack=" + techStack +
                ", githubRepository='" + githubRepository + '\'' +
                ", creatorId=" + creatorId +
                '}';
    }

    public ProjectDTO(String projectName, String description, Set<String> techStack) {
        this.projectName = projectName;
        this.description = description;
        this.techStack = techStack;
    }
}
