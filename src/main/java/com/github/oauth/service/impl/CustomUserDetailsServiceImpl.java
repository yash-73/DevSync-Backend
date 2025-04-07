package com.github.oauth.service.impl;




import com.github.oauth.model.User;
import com.github.oauth.repository.UserRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service

public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }


    @Override
    @Transactional
    public UserDetails loadUserByUsername(String githubId) throws UsernameNotFoundException {
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with GitHub ID: " + githubId));

        return new org.springframework.security.core.userdetails.User(
                user.getGithubId(),
                "", // No password since we use OAuth
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}