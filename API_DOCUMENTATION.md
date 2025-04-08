# API Documentation

## Authentication

- All endpoints except those marked as `public` require GitHub OAuth authentication
- The OAuth flow is handled by Spring Security
- After successful authentication, the user's session is maintained via cookies

## Base URL

```
http://localhost:8080
```

## Authentication Endpoints

### Get Current User

- **Endpoint**: `GET /api/auth/me`
- **Description**: Get the current authenticated user's information
- **Authentication**: Required
- **Response**:
  ```json
  {
    "authenticated": true,
    "user": {
      "id": 123456,
      "login": "username",
      "name": "User Name",
      "email": "user@example.com",
      "avatar_url": "https://..."
      // other GitHub user attributes
    }
  }
  ```
  or
  ```json
  {
    "authenticated": false
  }
  ```

### Logout

- **Endpoint**: `GET /api/auth/logout`
- **Description**: Logout the current user and clear session
- **Authentication**: Required
- **Response**:
  ```json
  {
    "message": "Logged out successfully"
  }
  ```

## User Management Endpoints

### Get User Profile

- **Endpoint**: `GET /api/user/profile`
- **Description**: Get the current user's profile information
- **Authentication**: Required
- **Response**: User object with profile details

### Update Email

- **Endpoint**: `PUT /api/user/email`
- **Description**: Update the user's email address
- **Authentication**: Required
- **Request Body**:
  ```json
  {
    "email": "new.email@example.com"
  }
  ```
- **Response**: Success message

### Add Roles

- **Endpoint**: `POST /api/user/role`
- **Description**: Add one or more roles to the user
- **Authentication**: Required
- **Request Body**:
  ```json
  {
    "roles": ["ADMIN", "USER"]
  }
  ```
- **Response**: Success message
- **Note**: Valid roles are ADMIN and USER

### Add Tech Stack

- **Endpoint**: `POST /api/user/tech`
- **Description**: Add technologies to user's tech stack
- **Authentication**: Required
- **Request Body**:
  ```json
  {
    "techStack": ["Java", "Spring Boot", "React"]
  }
  ```
- **Response**: Updated tech stack

### Remove Technology

- **Endpoint**: `DELETE /api/user/tech/{technology}`
- **Description**: Remove a technology from user's tech stack
- **Authentication**: Required
- **Path Parameter**: `technology` - Name of the technology to remove
- **Response**: Updated tech stack

### Get Created Projects

- **Endpoint**: `GET /api/user/projects`
- **Description**: Get all projects created by the user
- **Authentication**: Required
- **Response**: List of ProjectDTO objects

### Get Tech Stack

- **Endpoint**: `GET /api/user/tech`
- **Description**: Get the current user's tech stack
- **Authentication**: Required
- **Response**: Set of Tech objects
  ```json
  [
    {
      "id": 1,
      "techName": "Java"
    },
    {
      "id": 2,
      "techName": "Spring Boot"
    }
  ]
  ```

### Get Joined Projects

- **Endpoint**: `GET /api/user/joined-projects`
- **Description**: Get all projects the user has joined
- **Authentication**: Required
- **Response**: List of ProjectDTO objects
  ```json
  [
    {
      "id": 1,
      "name": "Project Name",
      "description": "Project Description",
      "techStack": ["Java", "Spring Boot"],
      "status": "ACTIVE",
      "creatorId": 123
    }
  ]
  ```

### Get User DTO

- **Endpoint**: `GET /api/user/dto`
- **Description**: Get the current user's data in DTO format
- **Authentication**: Required
- **Response**: UserDTO object
  ```json
  {
    "id": 123,
    "login": "username",
    "name": "User Name",
    "email": "user@example.com",
    "avatarUrl": "https://...",
    "roles": ["ADMIN", "USER"],
    "techStack": ["Java", "Spring Boot", "React"]
  }
  ```

## Project Management Endpoints

### Create Project

- **Endpoint**: `POST /api/project`
- **Description**: Create a new project
- **Authentication**: Required
- **Request Body**:
  ```json
  {
    "name": "Project Name",
    "description": "Project Description",
    "techStack": ["Java", "Spring Boot"]
  }
  ```
- **Response**: Created ProjectDTO

### Get Project

- **Endpoint**: `GET /api/project/{id}`
- **Description**: Get project details by ID
- **Authentication**: Required
- **Path Parameter**: `id` - Project ID
- **Response**: ProjectDTO

### Update Project

- **Endpoint**: `PUT /api/project/{id}`
- **Description**: Update project details
- **Authentication**: Required
- **Path Parameter**: `id` - Project ID
- **Request Body**: ProjectDTO
- **Response**: Updated ProjectDTO

### Delete Project

- **Endpoint**: `DELETE /api/project/{id}`
- **Description**: Delete a project
- **Authentication**: Required
- **Path Parameter**: `id` - Project ID
- **Response**: Success message

### Search Projects by Tech Stack

- **Endpoint**: `POST /api/project/search`
- **Description**: Search projects by tech stack
- **Authentication**: Required
- **Request Body**:
  ```json
  {
    "techStack": ["Java", "Spring Boot"]
  }
  ```
- **Response**: List of ProjectDTO objects ordered by tech stack match count

## Task Management Endpoints

### Create Task

- **Endpoint**: `POST /api/task`
- **Description**: Create a new task
- **Authentication**: Required
- **Request Body**:
  ```json
  {
    "title": "Task Title",
    "description": "Task Description",
    "projectId": 1,
    "assigneeId": 2
  }
  ```
- **Response**: Created TaskDTO

### Get Task

- **Endpoint**: `GET /api/task/{id}`
- **Description**: Get task details by ID
- **Authentication**: Required
- **Path Parameter**: `id` - Task ID
- **Response**: TaskDTO

### Update Task

- **Endpoint**: `PUT /api/task/{id}`
- **Description**: Update task details
- **Authentication**: Required
- **Path Parameter**: `id` - Task ID
- **Request Body**: TaskDTO
- **Response**: Updated TaskDTO

### Delete Task

- **Endpoint**: `DELETE /api/task/{id}`
- **Description**: Delete a task
- **Authentication**: Required
- **Path Parameter**: `id` - Task ID
- **Response**: Success message

### Get Project Tasks

- **Endpoint**: `GET /api/task/project/{projectId}`
- **Description**: Get all tasks for a project
- **Authentication**: Required
- **Path Parameter**: `projectId` - Project ID
- **Response**: List of TaskDTO objects

### Get User Tasks

- **Endpoint**: `GET /api/task/user/{userId}`
- **Description**: Get all tasks assigned to a user
- **Authentication**: Required
- **Path Parameter**: `userId` - User ID
- **Response**: List of TaskDTO objects

## Error Responses

All endpoints may return the following error responses:

- **400 Bad Request**: Invalid input data
- **401 Unauthorized**: Not authenticated
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error

Error response format:

```json
{
  "error": "Error message description"
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
