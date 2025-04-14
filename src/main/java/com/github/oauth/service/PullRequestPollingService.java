package com.github.oauth.service;

import com.github.oauth.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PullRequestPollingService {
    private static final Logger logger = LoggerFactory.getLogger(PullRequestPollingService.class);
    private final GitHubService githubService;
    private final TaskService taskService;

    public PullRequestPollingService(GitHubService githubService, TaskService taskService) {
        this.githubService = githubService;
        this.taskService = taskService;
    }

    @Scheduled(fixedRate = 10000) // Run every 10 seconds
    public void checkPullRequests() {
        try {
            logger.info("Starting pull request status check");
            // Get all tasks with REQUEST_COMPLETE status
            List<Task> pendingTasks = taskService.getTasksByStatus("REQUEST_COMPLETE");
            logger.info("Found {} tasks in REQUEST_COMPLETE status", pendingTasks.size());

            for (Task task : pendingTasks) {
                try {
                    logger.debug("Processing task: {}", task.getId());
                    String prUrl = task.getPullRequestUrl();
                    if (prUrl == null) {
                        logger.warn("Task {} has no PR URL", task.getId());
                        continue;
                    }

                    // Get the task creator's access token
                    String accessToken = taskService.getTaskCreatorAccessToken(task.getId());
                    if (accessToken == null) {
                        logger.error("No access token found for task creator: {}", task.getId());
                        continue;
                    }

                    // Extract PR information from URL
                    PRInfo prInfo = extractPRInfo(prUrl);
                    if (prInfo == null) {
                        logger.error("Invalid PR URL for task {}: {}", task.getId(), prUrl);
                        continue;
                    }
                    logger.debug("Extracted PR info - owner: {}, repo: {}, prNumber: {}", 
                        prInfo.owner, prInfo.repo, prInfo.prNumber);

                    // Check if PR is merged
                    boolean isMerged = githubService.isPullRequestMerged(
                        prInfo.owner,
                        prInfo.repo,
                        prInfo.prNumber,
                        accessToken
                    );
                    logger.debug("PR merge status for task {}: {}", task.getId(), isMerged);

                    if (isMerged) {
                        logger.info("PR merged for task {}, updating status to COMPLETED", task.getId());
                        taskService.updateTaskStatusById(task.getId(), "COMPLETED");
                    } else {
                        // Check if PR is closed but not merged
                        boolean isClosed = githubService.isPullRequestClosed(
                            prInfo.owner,
                            prInfo.repo,
                            prInfo.prNumber,
                            accessToken
                        );
                        logger.debug("PR closed status for task {}: {}", task.getId(), isClosed);
                        
                        if (isClosed) {
                            logger.info("PR closed without merge for task {}, updating status to REQUEST_REJECTED", task.getId());
                            taskService.updateTaskStatusById(task.getId(), "REQUEST_REJECTED");
                        }
                    }

                    // Update last checked timestamp
                    taskService.updateLastChecked(task.getId());
                    logger.debug("Updated last checked timestamp for task {}", task.getId());

                } catch (Exception e) {
                    logger.error("Error checking PR status for task {}: {}", task.getId(), e.getMessage(), e);
                    logger.error("Stack trace: ", e);
                }
            }
            logger.info("Completed pull request status check");
        } catch (Exception e) {
            logger.error("Error in pull request polling service: {}", e.getMessage(), e);
            logger.error("Stack trace: ", e);
        }
    }

    private PRInfo extractPRInfo(String prUrl) {
        // Parse GitHub PR URL to extract owner, repo, and PR number
        // Example: https://github.com/owner/repo/pull/123
        Pattern pattern = Pattern.compile("github.com/([^/]+)/([^/]+)/pull/(\\d+)");
        Matcher matcher = pattern.matcher(prUrl);
        if (matcher.find()) {
            return new PRInfo(
                matcher.group(1), // owner
                matcher.group(2), // repo
                Integer.parseInt(matcher.group(3)) // pr number
            );
        }
        return null;
    }

    private static class PRInfo {
        final String owner;
        final String repo;
        final int prNumber;

        PRInfo(String owner, String repo, int prNumber) {
            this.owner = owner;
            this.repo = repo;
            this.prNumber = prNumber;
        }
    }
} 