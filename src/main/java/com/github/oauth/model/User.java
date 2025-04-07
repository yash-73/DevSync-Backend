package com.github.oauth.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String githubId;

    @Column(nullable = false)
    private String login;

    private String name;

    private String email;

    private String avatarUrl;

    @Column(length = 1000)
    private String accessToken;

    private LocalDateTime tokenExpiryDate;

    private String refreshToken;

    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @ManyToMany( cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE} , fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_tech_stack",
            joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "tech_id"))
    private Set<Tech> techStack = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "creator" , cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private Set<Project> createdProjects = new HashSet<>();


    @JsonIgnore
    @ManyToMany(mappedBy = "members")
    private Set<Project> projects = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastLoginAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastLoginAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", techStack=" + techStack +
                ", roles=" + roles +
                '}';
    }
}
