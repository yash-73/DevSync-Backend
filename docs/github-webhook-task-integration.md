# GitHub Pull Request and Task Integration

## Overview
This document outlines the implementation of GitHub pull request integration with task management using a polling approach. The system allows users to link pull requests to tasks and automatically updates task status by periodically checking pull request status.

## Backend Implementation

### 1. Polling Service
```java
@Service
public class PullRequestPollingService {
    @Scheduled(fixedRate = 10000) // Run every 10 seconds
    public void checkPullRequests() {
        // Get tasks with REQUEST_COMPLETE status
        // Check PR status for each task
        // Update task status based on PR state
    }
}
```

### 2. Task Status Flow
1. **Task Assignment**
   - Creator assigns task (status: `REQUESTED`)
   - User accepts task (status: `PENDING`)

2. **Pull Request Creation**
   - User creates pull request on GitHub
   - User updates task with PR URL (status: `REQUEST_COMPLETE`)
   - PR URL stored in Firebase task document

3. **Pull Request Status Check**
   - Polling service runs every 10 seconds
   - Checks PR status for all tasks in `REQUEST_COMPLETE` state
   - Updates task status to `COMPLETED` if PR is merged
   - Updates task status to `REQUEST_REJECTED` if PR is closed without merge

### 3. Firebase Task Document Structure
```json
{
  "id": "task_id",
  "assignedTo": "user_id",
  "projectId": "project_id",
  "details": "task_details",
  "status": "REQUEST_COMPLETE",
  "pullRequestUrl": "https://github.com/.../pull/123",
  "lastChecked": "timestamp",
  "createdAt": "timestamp"
}
```

### 4. Security Considerations
- GitHub API rate limiting
- Authentication for all API endpoints
- Input validation
- Error handling

### 5. Error Handling
- Invalid pull request URLs
- Task not found in Firebase
- Invalid task status transitions
- Network issues with Firebase
- GitHub API rate limits
- PR parsing errors

## Frontend Implementation

### 1. Task Update Form
```typescript
interface TaskUpdateForm {
  taskId: string;
  pullRequestUrl: string;
  status: 'REQUEST_COMPLETE';
}
```

### 2. API Endpoints
```typescript
// Update task with pull request URL
PUT /api/task/status
Body: {
  taskId: string;
  pullRequestUrl: string;
  status: 'REQUEST_COMPLETE';
}

// Get task details
GET /api/task/{taskId}
Response: {
  id: string;
  assignedTo: string;
  projectId: string;
  details: string;
  status: string;
  pullRequestUrl?: string;
  lastChecked: string;
  createdAt: string;
}
```

### 3. UI Components
1. **Task Details View**
   - Display current task status
   - Show pull request URL if available
   - Status update button/interface
   - Last checked timestamp

2. **Pull Request URL Input**
   - Input field for PR URL
   - URL validation
   - Submit button

3. **Status Indicators**
   - Visual indicators for different task statuses
   - Status transition history
   - Last checked time

### 4. User Flow
1. User accepts task
2. User creates pull request on GitHub
3. User copies PR URL
4. User updates task with PR URL
5. System automatically checks PR status every 10 seconds
6. System updates task status based on PR state

### 5. Error Handling
- Invalid PR URL format
- Network errors
- Task update failures
- Status transition validation
- Rate limit warnings

## Frontend Polling Implementation (React)

### 1. Polling Service
```typescript
// pollingService.ts
import { useEffect, useState } from 'react';
import axios from 'axios';

export const useTaskPolling = (interval = 10000) => {
  const [tasks, setTasks] = useState<any[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchTasks = async () => {
      try {
        const response = await axios.get('/api/tasks/status-check');
        setTasks(response.data);
        setError(null);
      } catch (err) {
        setError('Failed to fetch tasks');
        console.error('Polling error:', err);
      }
    };

    // Initial fetch
    fetchTasks();

    // Set up polling
    const intervalId = setInterval(fetchTasks, interval);

    // Cleanup
    return () => clearInterval(intervalId);
  }, [interval]);

  return { tasks, error };
};
```

### 2. Task Status API Endpoints

#### Get Tasks by Status
```typescript
// GET /api/tasks/status/{status}
interface TaskResponse {
  id: string;
  assignedTo: number;  // Firebase stores as number
  projectId: number;   // Firebase stores as number
  details: string;
  status: string;
  pullRequestUrl?: string;
  lastChecked: string;
  createdAt: string;
}

// Example usage
const fetchTasksByStatus = async (status: string) => {
  const response = await axios.get<TaskResponse[]>(`/api/tasks/status/${status}`);
  return response.data;
};
```

#### Update Task Status
```typescript
// PUT /api/task/status
interface TaskStatusUpdate {
  taskId: string;
  status: 'REQUEST_COMPLETE' | 'COMPLETED' | 'REQUEST_REJECTED';
  pullRequestUrl?: string;
}

// Example usage
const updateTaskStatus = async (updateData: TaskStatusUpdate) => {
  const response = await axios.put('/api/task/status', updateData);
  return response.data;
};
```

#### Get Task by ID
```typescript
// GET /api/task/{taskId}
interface TaskDetails {
  id: string;
  assignedTo: number;  // Firebase stores as number
  projectId: number;   // Firebase stores as number
  details: string;
  status: string;
  pullRequestUrl?: string;
  lastChecked: string;
  createdAt: string;
}

// Example usage
const fetchTaskById = async (taskId: string) => {
  const response = await axios.get<TaskDetails>(`/api/task/${taskId}`);
  return response.data;
};
```

### 3. React Components

#### Task Status Component
```typescript
// TaskStatus.tsx
import React from 'react';
import { useTaskPolling } from './pollingService';

export const TaskStatus: React.FC = () => {
  const { tasks, error } = useTaskPolling();

  if (error) {
    return <div className="error">{error}</div>;
  }

  return (
    <div className="task-list">
      {tasks.map(task => (
        <div key={task.id} className="task-item">
          <h3>Task: {task.details}</h3>
          <p>Status: {task.status}</p>
          {task.pullRequestUrl && (
            <p>
              PR URL: <a href={task.pullRequestUrl} target="_blank" rel="noopener noreferrer">
                {task.pullRequestUrl}
              </a>
            </p>
          )}
          <p>Last Checked: {new Date(task.lastChecked).toLocaleString()}</p>
        </div>
      ))}
    </div>
  );
};
```

#### Task Update Form Component
```typescript
// TaskUpdateForm.tsx
import React, { useState } from 'react';
import axios from 'axios';

export const TaskUpdateForm: React.FC = () => {
  const [formData, setFormData] = useState({
    taskId: '',
    pullRequestUrl: ''
  });
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);

    try {
      await axios.put('/api/task/status', {
        taskId: formData.taskId,
        status: 'REQUEST_COMPLETE',
        pullRequestUrl: formData.pullRequestUrl
      });
      setSuccess(true);
      setFormData({ taskId: '', pullRequestUrl: '' });
    } catch (err) {
      setError('Failed to update task');
      console.error('Update error:', err);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label htmlFor="taskId">Task ID:</label>
        <input
          type="text"
          id="taskId"
          name="taskId"
          value={formData.taskId}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <label htmlFor="pullRequestUrl">Pull Request URL:</label>
        <input
          type="url"
          id="pullRequestUrl"
          name="pullRequestUrl"
          value={formData.pullRequestUrl}
          onChange={handleChange}
          pattern="^https://github.com/.*/pull/\d+$"
          required
        />
      </div>
      {error && <div className="error">{error}</div>}
      {success && <div className="success">Task updated successfully!</div>}
      <button type="submit">Update Task</button>
    </form>
  );
};
```

### 4. Error Handling

```typescript
// errorHandler.ts
export const handleTaskError = (error: any): string => {
  if (axios.isAxiosError(error)) {
    switch (error.response?.status) {
      case 400:
        return 'Invalid task data provided';
      case 404:
        return 'Task not found';
      case 409:
        return 'Invalid status transition';
      case 500:
        return 'Server error occurred';
      default:
        return 'An unexpected error occurred';
    }
  }
  return 'Network error occurred';
};
```

### 5. Usage Example

```typescript
// App.tsx
import React from 'react';
import { TaskStatus } from './TaskStatus';
import { TaskUpdateForm } from './TaskUpdateForm';

export const App: React.FC = () => {
  return (
    <div className="app">
      <h1>Task Management</h1>
      <TaskStatus />
      <TaskUpdateForm />
    </div>
  );
};
```

### 6. Environment Configuration

```typescript
// config.ts
export const config = {
  apiUrl: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  pollingInterval: parseInt(process.env.REACT_APP_POLLING_INTERVAL || '10000', 10)
};
```

## Testing

### 1. Backend Tests
- PR status checking
- Task status updates
- Firebase operations
- Error scenarios
- Rate limit handling

### 2. Frontend Tests
- Form validation
- API integration
- UI state management
- Error handling

### 3. Integration Tests
- End-to-end workflow
- PR status checking scenarios
- Status transition scenarios
- Rate limit scenarios

## Monitoring and Logging

### 1. Backend Logging
- PR status checks
- Task status updates
- Error scenarios
- Rate limit warnings
- Performance metrics

### 2. Frontend Logging
- User actions
- API calls
- Error scenarios
- Performance metrics

## Deployment

### 1. Backend Requirements
- Spring Boot application
- Firebase configuration
- GitHub API token
- Scheduled task configuration

### 2. Frontend Requirements
- React/Angular application
- Environment configuration
- API endpoint configuration

## Future Enhancements
1. Configurable polling interval
2. PR status tracking
3. Task-PR linking history
4. Notification system
5. Analytics dashboard
6. Rate limit optimization
7. Batch processing for multiple tasks

## Implementation Details

### 1. Polling Service Configuration
```java
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Additional scheduling configuration if needed
}
```

### 2. PR Status Checking
```java
@Service
public class GitHubService {
    public boolean isPullRequestMerged(String owner, String repo, int prNumber) {
        // Check if PR is merged
    }

    public boolean isPullRequestClosed(String owner, String repo, int prNumber) {
        // Check if PR is closed
    }
}
```

### 3. Task Status Updates
```java
@Service
public class TaskService {
    public List<Task> getTasksByStatus(String status) {
        // Get tasks with specific status
    }

    public void updateLastChecked(String taskId) {
        // Update last checked timestamp
    }

    public Task updateTaskStatusById(String taskId, String status) {
        // Update task status
    }
}
```

### 4. Error Handling
```java
try {
    // PR status checking logic
} catch (RateLimitExceededException e) {
    logger.warn("GitHub API rate limit exceeded");
    // Implement backoff strategy
} catch (Exception e) {
    logger.error("Error checking PR status", e);
    // Handle other errors
}
```

### 5. Rate Limit Management
- Implement exponential backoff
- Track API usage
- Cache results when possible
- Batch requests when possible

## Troubleshooting
1. Check application logs for:
   - PR status check failures
   - Rate limit warnings
   - Task update errors
   - Network issues

2. Common issues:
   - Rate limiting: Implement backoff strategy
   - Invalid PR URLs: Validate URL format
   - Network issues: Implement retry logic
   - Task update failures: Check Firebase connection 