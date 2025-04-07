package com.github.oauth.controller;


import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.oauth.service.GitHubService;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService){
        this.gitHubService = gitHubService;
    }

    @GetMapping("/repositories")
    public ResponseEntity<?> getUserRepositories() {
        List<GHRepository> repositories = gitHubService.getUserRepositories();

        List<? extends Map<String, ? extends Serializable>> repos = repositories.stream()
                .map(repo -> {
                    try {
                        return Map.of(
                                "id", repo.getId(),
                                "name", repo.getName(),
                                "fullName", repo.getFullName(),
                                "description", repo.getDescription() != null ? repo.getDescription() : "",
                                "url", repo.getHtmlUrl().toString(),
                                "isPrivate", repo.isPrivate(),
                                "defaultBranch", repo.getDefaultBranch()
                        );
                    } catch (Exception e) {
                        return Map.of(
                                "id", repo.getId(),
                                "name", repo.getName(),
                                "error", "Error fetching complete details"
                        );
                    }
                })
                .collect(Collectors.toList());

        Collections.reverse(repos);
        return ResponseEntity.ok(repos);
    }

    @PostMapping("/repositories")
    public ResponseEntity<?> createRepository(
            @RequestBody Map<String, Object> request) {

        String name = (String) request.get("name");
        String description = (String) request.getOrDefault("description", "");
        boolean isPrivate = (boolean) request.getOrDefault("isPrivate", false);

        GHRepository repository = gitHubService.createRepository(name, description, isPrivate);

        return ResponseEntity.ok(Map.of(
                "id", repository.getId(),
                "name", repository.getName(),
                "fullName", repository.getFullName(),
                "url", repository.getHtmlUrl().toString(),
                "isPrivate", repository.isPrivate()
        ));
    }

    @PostMapping("/repositories/{repoName}/commits")
    public ResponseEntity<?> createCommit(
            @PathVariable String repoName,
            @RequestBody Map<String, String> request) {

        String branchName = request.getOrDefault("branch", "main");
        String path = request.get("path");
        String commitMessage = request.get("message");
        String content = request.get("content");

        gitHubService.createCommit(repoName, branchName, path, commitMessage, content);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Commit created successfully"
        ));
    }

    @GetMapping("/repositories/{repoName}/pulls")
    public ResponseEntity<?> getPullRequests(@PathVariable String repoName) {
        List<GHPullRequest> pullRequests = gitHubService.getPullRequests(repoName);

        List<Map<String, Object>> prs = pullRequests.stream()
                .map(pr -> {
                    Map<String, Object> prMap = new HashMap<>();
                    try {
                        prMap.put("id", pr.getId());
                        prMap.put("number", pr.getNumber());
                        prMap.put("title", pr.getTitle());
                        prMap.put("body", pr.getBody() != null ? pr.getBody() : "");
                        prMap.put("author", pr.getUser().getLogin());
                        prMap.put("url", pr.getHtmlUrl().toString());
                        prMap.put("state", pr.getState().name());
                    } catch (Exception e) {
                        prMap.put("id", pr.getId());
                        prMap.put("number", pr.getNumber());
                        prMap.put("error", "Error fetching complete details");
                    }
                    return prMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(prs);
    }


    @PostMapping("/repositories/{repoName}/pulls/{prNumber}/merge")
    public ResponseEntity<?> mergePullRequest(
            @PathVariable String repoName,
            @PathVariable int prNumber,
            @RequestBody Map<String, String> request) {

        String commitMessage = request.getOrDefault("message", "Merge pull request #" + prNumber);

        gitHubService.mergePullRequest(repoName, prNumber, commitMessage);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Pull request merged successfully"
        ));
    }
}