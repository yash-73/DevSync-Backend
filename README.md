# 🚀 DevSync - A Project Collaboration Platform

A full-stack platform where **developers can discover projects to contribute to** and **project creators can find collaborators** — all matched by tech stack preferences. With **GitHub OAuth**, **real-time updates**, and **seamless task management**, it brings together contributors and maintainers under one intuitive interface.

---

## 🧠 Overview

This platform is designed to bridge the gap between developers and open-source/project maintainers by providing:

- A way for users to host projects, specifying the tech stack.
- A discovery system for contributors to find projects matching their tech expertise.
- Real-time collaboration, messaging, and task assignment.
- GitHub-powered version control with pull request tracking.

---

## ✨ Features

### 🔍 Project Discovery
- Users can search for projects based on their preferred **tech stack**.
- Recommendations are driven by **stack compatibility**.

### 🧑‍💻 Contributor Management
- Project creators can:
  - Accept or reject collaboration requests.
  - Automatically add contributors as GitHub collaborators.
  - Assign tasks with real-time updates via Firebase.
- Contributors can:
  - Request to join projects.
  - Chat and coordinate within project pages.
  - Submit pull requests and link them to assigned tasks.

### 🔒 Authentication
- **GitHub OAuth Only** — login and authorization are handled entirely through GitHub.
- We request **read and write permissions** to:
  - Create private repositories.
  - Add collaborators.
  - Track pull request statuses.

### 🔄 Real-Time Collaboration
- Messages, join requests, and task updates are managed via **Firebase Firestore**.
- Tasks reflect the actual status of GitHub pull requests using a **scheduled backend job**.

---

## 🛠️ Tech Stack

| Layer         | Tech                           |
|---------------|--------------------------------|
| Frontend      | React                          |
| Backend       | Spring Boot                    |
| Database      | MySQL                          |
| Realtime DB   | Firebase Firestore             |
| Auth          | GitHub OAuth (read + write)    |
| GitHub API    | Repo creation, collaborator mgmt, PR tracking |

---

## 🔁 Project Flow

1. **User signs in** using GitHub OAuth. The access token is stored securely in MySQL.
2. **User creates a project**:
   - Provides name, description, and tech stack.
   - A **private GitHub repository** is created automatically.
3. **Other users search** for projects based on matching tech stacks.
4. **Contributor requests** to join a project:
   - Request is stored in **Firestore** for real-time updates.
5. **Project owner accepts/rejects** the request:
   - If accepted, the contributor is added to the GitHub repo.
6. **Messaging and task assignment**:
   - Collaborators chat within the project.
   - Tasks are assigned via Firestore.
7. **Pull request submission**:
   - Contributors link PR URLs to tasks and mark them as `request_complete`.
8. **Task validation**:
   - A **scheduled backend function** checks GitHub API for PR status.
   - Tasks are marked `completed` or `rejected` based on PR merge status.
9. **Profile Highlighting**:
   - Shows user's active and completed projects.
   - Highlights completed tasks and contributions.

---

## ✅ Why This Platform?

- Encourages **authentic collaboration** by linking task completion with actual PRs.
- Promotes **skill-based discovery** using tech stack filters.
- Enables **real-time communication and updates** without third-party tools.
- Ensures **transparency and legitimacy** through GitHub-based task verification.

---

## 🔐 Permissions

The GitHub integration requires:
- Read access to user repositories.
- Write access to create repositories and manage collaborators.
- PR read access to validate task completion.

---

## 🧪 Future Improvements

- Project recommendation system based on user history.
- Notification system for PR status changes.
- Integration with CI/CD tools for build status on tasks.
- Support for public projects and forks.
- Use of GitHub actions or Github app to handle current github api calls 

---

## 🖼️ Image Gallery

Visuals can help convey the platform’s flow and UX more effectively. Here’s where you can showcase screenshots or diagrams:

### 📌 Suggested Images to Add:
- **Landing Page UI** – Overview of the homepage and call-to-action for signing in with GitHub.
  ![Screenshot 2025-06-12 181658](https://github.com/user-attachments/assets/f8a95963-3e51-4ab2-b85e-6e925487b7ae)

- **Project Creation Flow** – Steps for creating a new project and linking a GitHub repo.
  ![Screenshot 2025-06-12 183116](https://github.com/user-attachments/assets/737f1fa2-6e8c-4494-9a88-0b4068960be1)
  ![Screenshot 2025-06-12 183152](https://github.com/user-attachments/assets/812f9306-ea1c-448f-9fc5-138d5a9be9c1)


- **Contributor Dashboard** – Interface where contributors browse and request to join projects.
- ![Screenshot 2025-06-12 182041](https://github.com/user-attachments/assets/22b79557-a3df-42b7-aa6a-2fc4741ac5b9)

- **Task Assignment Panel** – Real-time task management UI using Firebase.
- ![Screenshot 2025-06-12 182144](https://github.com/user-attachments/assets/fa4514d6-336a-4a57-9f22-2006c55933a7)
- ![Screenshot 2025-06-12 182231](https://github.com/user-attachments/assets/04d1fc00-c520-4bc2-8638-75d5910c66b5)


- **Pull Request Linking** – A screenshot showing how users can link PRs to tasks.
- **Messaging System** – In-app chat interface between collaborators.
- ![Screenshot 2025-06-12 182055](https://github.com/user-attachments/assets/40ac9d7a-b088-4fa9-b58f-8c5caed1c9d6)

- **Profile Page** – Highlights completed projects and tasks.
- ![Screenshot 2025-06-12 181739](https://github.com/user-attachments/assets/527a8191-e959-4ccf-a9d5-998e1604f52d)




---
