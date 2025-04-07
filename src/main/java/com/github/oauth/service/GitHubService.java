package com.github.oauth.service;



import com.github.oauth.model.User;
import com.github.oauth.repository.UserRepository;

import org.kohsuke.github.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service

public class GitHubService {

    private final UserRepository userRepository;

    public GitHubService(UserRepository userRepository){
        this.userRepository = userRepository;
    }


    private GitHub connectToGitHub() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String githubId = authentication.getName();

        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            return new GitHubBuilder().withOAuthToken(user.getAccessToken()).build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to GitHub", e);
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
            GitHub github = connectToGitHub();
            GHCreateRepositoryBuilder builder = github.createRepository(name)
                    .description(description)
                    .private_(isPrivate);

            return builder.create();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create repository", e);
        }
    }
}