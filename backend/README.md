# AI Interview & Placement Platform

AI-powered interview and placement preparation platform built for students and job seekers.

The platform provides coding practice, coding problem management, AI-powered assistance, mock interviews, resume analysis, authentication, GitHub integration, progress tracking, and performance monitoring.

---

## Overview

The AI Interview & Placement Platform is a full-stack application designed to provide an integrated environment for technical interview and placement preparation.

The platform combines:

- Coding problem practice
- Multi-language code execution
- Coding progress tracking
- AI-powered coding assistance
- Mock interviews
- Resume analysis
- GitHub solution integration
- User authentication
- Performance tracking

The Coding Arena is designed around a backend-driven problem system where coding problems are stored in the database and can be imported from supported GitHub repositories.

---

## Features

### Authentication

- Email and password authentication
- JWT-based authentication
- BCrypt password encryption
- Google OAuth2
- GitHub OAuth2
- Protected backend APIs
- Persistent authenticated sessions

### Coding Arena

- Coding problem browser
- Problem difficulty filtering
- Experience-level filtering
- Problem search
- Scrollable problem selection
- Problem descriptions
- Examples
- Constraints
- Tags
- Starter code
- Language selection
- Code editor
- Code execution
- Code submission
- Test-case validation
- Coding progress tracking
- Completed problem tracking
- Submission statistics
- Last selected problem persistence

### AI Coding Assistance

- AI-powered coding hints
- Problem-aware suggestions
- Language-aware assistance
- Code-aware hints
- Context-based AI responses

### Coding Problem Management

- Database-backed coding problems
- Problem metadata
- Difficulty
- Tags
- Constraints
- Function information
- Starter code
- Language configurations
- Minimum experience level
- Active/inactive problem status

### GitHub Problem Import

- GitHub repository integration
- Recursive repository scanning
- Problem discovery through `problem.json`
- Automatic test-case discovery
- Single problem import
- Bulk repository import
- Duplicate problem detection
- Existing problem updates
- Hidden test-case support

### GitHub Solution Integration

- GitHub account connection
- Repository connection
- Automatic solution commits after successful submission
- Problem-based solution directories
- Language-specific solution filenames
- Commit messages generated from problem titles

### Mock Interview

- AI-generated interview questions
- Interview type selection
- Progressive question difficulty
- Candidate answer evaluation
- Feedback generation
- Strength identification
- Improvement suggestions

### Resume Analyzer

- Resume upload
- AI-powered resume analysis
- ATS score
- Detected skills
- Missing skills
- Strengths
- Weaknesses
- Improvement suggestions
- Recommended job role

### Performance Tracking

- Coding progress
- Submission statistics
- Successful submissions
- Completed problems
- Mock interview performance
- User progress persistence

---

# Technology Stack

## Frontend

- React
- Vite
- JavaScript
- Axios
- React Router
- Monaco Editor
- Recharts
- CSS
- Lucide Icons

## Backend

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- WebFlux WebClient
- JWT
- OAuth2
- Lombok

## Database

- MySQL
- Hibernate / JPA

## AI

- Groq API
- Gemini API

## Code Execution

- Piston API
- Docker
- Docker Desktop
- WSL2

## External Integrations

- GitHub API
- Google OAuth2
- GitHub OAuth2
- Gmail SMTP

---

# System Architecture

```text
                         AI Interview
                         & Placement
                           Platform
                               |
             +-----------------+-----------------+
             |                                   |
         Frontend                            Backend
             |                                   |
          React                              Spring Boot
             |                                   |
       Axios REST API                    Spring Security
             |                                   |
             |                    +--------------+--------------+
             |                    |              |              |
             |                 MySQL          AI APIs        GitHub
             |                    |              |              |
             |                    |           Groq/Gemini      |
             |                    |
             |               Coding Engine
             |                    |
             |                  Piston
             |                    |
             |                 Docker
             |
         Coding Arena