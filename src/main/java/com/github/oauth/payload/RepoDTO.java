package com.github.oauth.payload;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RepoDTO implements Serializable {
    private long id;
    private String name;
    private String fullName;
    private String description;
    private String url;
    private boolean isPrivate;
    private String defaultBranch;
    private String error; // optional, only set on failure
}
