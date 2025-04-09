package com.github.oauth.service;

import com.github.oauth.model.User;
import com.github.oauth.repository.UserRepository;

import org.kohsuke.github.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class GitHubService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);
    private final UserRepository userRepository;

    public GitHubService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private GitHub connectToGitHub() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String githubId = authentication.getName();
        logger.info("Connecting to GitHub for user: {}", githubId);

        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> {
                    logger.error("User not found for githubId: {}", githubId);
                    return new RuntimeException("User not found");
                });

        if (user.getAccessToken() == null || user.getAccessToken().isEmpty()) {
            logger.error("Access token is null or empty for user: {}", githubId);
            throw new RuntimeException("Access token not found for user");
        }

        try {
            GitHub github = new GitHubBuilder().withOAuthToken(user.getAccessToken()).build();
            // Test the connection
            github.getMyself();
            return github;
        } catch (IOException e) {
            logger.error("Failed to connect to GitHub for user: {}", githubId, e);
            throw new RuntimeException("Failed to connect to GitHub: " + e.getMessage(), e);
        }
    }

    public List<GHRepository> getUserRepositories() {
        try {
            GitHub github = connectToGitHub();
            GHMyself myself = github.getMyself();

            return StreamSupport.stream(myself.listRepositories().spliterator(), false)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to get user repositories", e);
        }
    }

    public void createCommit(String repoName, String branchName, String path,
            String commitMessage, String content) {
        try {
            GitHub github = connectToGitHub();
            GHMyself myself = github.getMyself();
            GHRepository repository = myself.getRepository(repoName);

            // First get the reference
            GHRef ref = repository.getRef("heads/" + branchName);

            // Get the current commit that this reference points to
            GHCommit latestCommit = repository.getCommit(ref.getObject().getSha());

            // Create a tree with the new content
            GHTreeBuilder treeBuilder = repository.createTree().baseTree(latestCommit.getTree().getSha());
            treeBuilder.add(path, content, false);
            GHTree tree = treeBuilder.create();

            // Create a commit
            GHCommit commit = repository.createCommit()
                    .parent(latestCommit.getSHA1())
                    .tree(tree.getSha())
                    .message(commitMessage)
                    .create();

            // Update the reference
            ref.updateTo(commit.getSHA1(), false);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create commit", e);
        }
    }

    public List<GHPullRequest> getPullRequests(String repoName) {
        try {
            GitHub github = connectToGitHub();
            GHMyself myself = github.getMyself();
            GHRepository repository = myself.getRepository(repoName);

            return repository.getPullRequests(GHIssueState.OPEN);
        } catch (IOException e) {
            throw new RuntimeException("Failed to get pull requests", e);
        }
    }

    public void mergePullRequest(String repoName, int prNumber, String commitMessage) {
        try {
            GitHub github = connectToGitHub();
            GHMyself myself = github.getMyself();
            GHRepository repository = myself.getRepository(repoName);
            GHPullRequest pullRequest = repository.getPullRequest(prNumber);

            pullRequest.merge(commitMessage);
        } catch (IOException e) {
            throw new RuntimeException("Failed to merge pull request", e);
        }
    }

    public GHRepository createRepository(String name, String description, boolean isPrivate) {
        try {
            logger.info("Creating repository: {} (private: {})", name, isPrivate);
            GitHub github = connectToGitHub();

            // Check if repository exists
            try {
                GHMyself myself = github.getMyself();
                if (myself.getRepository(name) != null) {
                    // Repository exists, append timestamp to make it unique
                    String uniqueName = name + "-" + System.currentTimeMillis();
                    logger.info("Repository {} already exists, using unique name: {}", name, uniqueName);
                    name = uniqueName;
                }
            } catch (IOException e) {
                // Repository doesn't exist, continue with original name
                logger.debug("Repository {} doesn't exist, proceeding with creation", name);
            }

            GHCreateRepositoryBuilder builder = github.createRepository(name)
                    .description(description)
                    .private_(isPrivate);

            GHRepository repository = builder.create();
            logger.info("Successfully created repository: {}", repository.getHtmlUrl());
            return repository;
        } catch (IOException e) {
            logger.error("Failed to create repository: {}", name, e);
            throw new RuntimeException("Failed to create repository: " + e.getMessage(), e);
        }
    }

    public void deleteRepository(String repoName) {
        try {
            GitHub github = connectToGitHub();
            GHMyself myself = github.getMyself();
            GHRepository repository = myself.getRepository(repoName);
            repository.delete();
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete repository", e);
        }
    }
}