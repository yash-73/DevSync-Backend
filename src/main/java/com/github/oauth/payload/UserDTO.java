package com.github.oauth.payload;

import java.util.Set;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;
    private String login;
    private String name;
    private String email;
    private String avatarUrl;
    private String githubId;
    private Set<String> roles;
    private Set<String> techStack;
    
    
    
}
