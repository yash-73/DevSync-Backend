package com.github.oauth.model;


    
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;

    @NotBlank(message = "Project name cannot be blank")
    @Column(name = "project_name", nullable = false, length = 255)
    private String projectName;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Column(name = "github_repository", nullable = false)
    private String githubRepository;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "project_status" , nullable = false)
    private ProjectStatus projectStatus;


    @ManyToOne
    @JoinColumn(name = "creator_id" , nullable = false)
    private User creator;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members;


    @ManyToMany(
            cascade = {CascadeType.MERGE, CascadeType.PERSIST},
            fetch = FetchType.EAGER
    )
    @JoinTable(
            name = "project_tech_stack",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "tech_id")
    )
    private Set<Tech> techStack = new HashSet<>();

    @PrePersist
    protected  void onCreate(){
        this.createdAt = new Date();
    }

    public Project(String projectName, String description, Set<Tech> techStack, User creator, String githubRepository){
        this.projectName = projectName;
        this.description = description;
        this.techStack = techStack;
        this.creator = creator;
        this.githubRepository = githubRepository;
    }



}


