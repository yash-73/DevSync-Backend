# API Documentation

## Authentication
- All endpoints except those marked as `public` require GitHub OAuth authentication
- The OAuth flow is handled by Spring Security
- After successful authentication, the user's session is maintained via cookies

## Base URL
```
http://localhost:8080
```

## Endpoints

### User Management

#### Get User Profile
```http
GET /api/user/profile
```
- Returns the authenticated user's profile
- Requires authentication
- Response:
```json
{
    "id": "user_id",
    "name": "user_name",
    "email": "user_email",
    "githubId": "github_id"
}
```

#### Update User Email
```http
PUT /api/user/email
Content-Type: application/json

{
    "email": "new_email@example.com"
}
```
- Updates the authenticated user's email
- Requires authentication
- Returns success message

### Project Management

#### Create Project
```http
POST /api/project
Content-Type: application/json

{
    "projectName": "Project Name",
    "description": "Project Description",
    "githubRepository": "github_repo_url",
    "techStack": ["tech1", "tech2"],
    "projectStatus": "ACTIVE"
}
```
- Creates a new project
- Requires authentication
- Returns created project details

#### Update Project
```http
PUT /api/project/{projectId}
Content-Type: application/json

{
    "projectName": "Updated Name",
    "description": "Updated Description",
    "githubRepository": "updated_repo_url",
    "techStack": ["tech1", "tech2", "tech3"],
    "projectStatus": "COMPLETED"
}
```
- Updates an existing project
- Requires authentication
- Only project creator can update
- Returns updated project details

#### Delete Project
```http
DELETE /api/project/{projectId}
```
- Deletes a project
- Requires authentication
- Only project creator can delete
- Returns success message

#### Search Projects by Tech Stack
```http
POST /api/project/search
Content-Type: application/json

{
    "techStack": ["tech1", "tech2"]
}
```
- Searches projects by tech stack
- Returns projects ordered by number of matching tech stack items
- Response:
```json
[
    {
        "projectId": "id",
        "projectName": "name",
        "description": "description",
        "githubRepository": "repo_url",
        "techStack": ["tech1", "tech2"],
        "projectStatus": "ACTIVE",
        "creatorId": "user_id"
    }
]
```

### Task Management

#### Create Task
```http
POST /api/task
Content-Type: application/json

{
    "title": "Task Title",
    "description": "Task Description",
    "projectId": "project_id",
    "status": "TODO"
}
```
- Creates a new task
- Requires authentication
- Returns created task details

#### Update Task
```http
PUT /api/task/{taskId}
Content-Type: application/json

{
    "title": "Updated Title",
    "description": "Updated Description",
    "status": "IN_PROGRESS"
}
```
- Updates an existing task
- Requires authentication
- Returns updated task details

#### Delete Task
```http
DELETE /api/task/{taskId}
```
- Deletes a task
- Requires authentication
- Returns success message

### Message Management

#### Send Message
```http
POST /api/messages
Content-Type: application/json

{
    "message": "Message content",
    "projectId": "project_id"
}
```
- Sends a message to a project
- Requires authentication
- Returns success message with Firestore document ID

#### Delete Message
```http
DELETE /api/messages/{messageId}
```
- Deletes a message
- Requires authentication
- Only message sender can delete
- Returns success message

### Notification Management

#### Send Join Request
```http
POST /api/notifications/request
Content-Type: application/json

{
    "projectId": "project_id"
}
```
- Sends a request to join a project
- Requires authentication
- Returns success message

#### Update Join Request
```http
PUT /api/notifications/request
Content-Type: application/json

{
    "projectId": "project_id",
    "userId": "user_id",
    "status": "ACCEPTED" // or "REJECTED"
}
```
- Updates a join request status
- Requires authentication
- Only project creator can update
- Returns success message

#### Delete Join Request
```http
DELETE /api/notifications/request/{userId}/{projectId}
```
- Deletes a join request
- Requires authentication
- Only request sender can delete
- Returns success message

## Error Responses

### 401 Unauthorized
```json
{
    "error": "Authentication required"
}
```

### 403 Forbidden
```json
{
    "error": "You don't have permission to perform this action"
}
```

### 404 Not Found
```json
{
    "error": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
    "error": "Error message details"
}
```

## Firestore Collections

### Messages
- Document ID format: `<userId>_<message>_<projectId>`
- Fields:
  - senderId: string
  - projectId: string
  - message: string
  - timestamp: timestamp

### ProjectJoinRequests
- Document ID format: `<userId>_<projectId>`
- Fields:
  - userId: string
  - projectId: string
  - status: string ("PENDING", "ACCEPTED", "REJECTED")
  - timestamp: timestamp

## Notes
- All timestamps are in UTC
- All IDs are strings
- Authentication is handled via GitHub OAuth
- CORS is configured for `http://localhost:5173` (Vite default port) 