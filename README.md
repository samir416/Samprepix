# Samprepix — AI Placement & Interview Preparation Platform

> A full-stack AI-powered platform for placement preparation, coding practice, aptitude, mock interviews, resume analysis, performance tracking, and career personalization.

---

## 📌 Overview

**Samprepix** is a unified placement-preparation platform built for students, freshers, and working professionals. It combines authentication, personalized onboarding, AI-assisted preparation, coding practice, aptitude practice, mock interviews, resume analysis, GitHub integration, performance tracking, achievements, and subscription management in one application.

### Candidate Journey

```text
Landing Page
    ↓
Register / Login / Google / GitHub
    ↓
Email Verification / OAuth
    ↓
Personalized Onboarding
    ↓
Dashboard
    ↓
Resume • Mock Interview • Coding • Aptitude
    ↓
Performance & Progress Tracking
```

---

# ✨ Features

## 🔐 Authentication & Account Management

- Email/password registration
- 6-digit OTP email verification
- BCrypt password hashing
- JWT authentication
- Google OAuth2 login/signup
- GitHub OAuth2 login/signup
- Forgot-password flow
- Secure password reset
- Account status management
- Authentication providers:
  - `EMAIL`
  - `GOOGLE`
  - `GITHUB`
- Protected frontend routes
- Backend authorization with Spring Security
- Role-based administrative access

### Authentication Flow

```text
Credentials / OAuth
       ↓
Authentication
       ↓
JWT
       ↓
Frontend Token Storage
       ↓
Authorization: Bearer <JWT>
       ↓
JwtFilter
       ↓
SecurityContext
       ↓
Protected API
```

---

# 👤 Personalized Onboarding

New users complete a guided profile before entering their personalized workspace.

The onboarding can capture:

- Journey type
  - Student
  - Working Professional
- Target role
- Experience level
- Career goal
- Skills
- Education details
- Professional information
- Profile photo
- GitHub-related information where applicable

### Completion Flow

```text
New User
   ↓
/onboarding
   ↓
Step 1
   ↓
Step 2
   ↓
Step 3
   ↓
Complete Profile
   ↓
PUT /api/profile
   ↓
HTTP 200
   ↓
profileCompleted = true
   ↓
/dashboard
```

The backend profile state should remain authoritative for determining whether onboarding is complete.

---

# 🧠 AI Features

## 🤖 AI Resume Analyzer

Candidates can upload supported resume documents for AI-powered analysis.

The system is designed to provide:

- Resume quality insights
- Strength identification
- Improvement recommendations
- Skill relevance analysis
- Placement-oriented feedback
- Career preparation suggestions

Configured AI providers may include **Groq** and **Google Gemini**.

---

## 🎤 AI Mock Interview

The mock interview module provides an interview-style preparation experience.

Capabilities include:

- Interview session creation
- AI-generated questions
- Answer submission
- AI evaluation
- Interview feedback
- Session completion
- Result presentation
- Voice-related helper functionality where configured

Typical flow:

```text
Start Interview
      ↓
AI Question
      ↓
Candidate Answer
      ↓
AI Evaluation
      ↓
Feedback
      ↓
Interview Result
```

---

# 💻 Coding Arena

Coding Arena is the programming-practice environment of Samprepix.

## Problem Bank

Current verified project data includes:

- **6,260 coding problems**
- Approximately **22,640 test cases**
- Public and hidden tests
- DSA problems
- SQL/database problems
- Legacy problem compatibility
- Persistent completion/progress tracking

## Coding Flow

```text
Select Problem
     ↓
Read Statement
     ↓
Monaco Editor
     ↓
Run
     ↓
Public Tests
     ↓
Submit
     ↓
Public + Hidden Tests
     ↓
Result
     ↓
Progress Saved
```

## Code Execution

The platform uses a Piston-compatible execution service.

Default local endpoint:

```text
http://localhost:2000/api/v2/execute
```

Docker may be required for the local execution environment.

### Additional Features

- Monaco Editor
- Run Code
- Submit Solution
- Public/Hidden Test Cases
- Progress Persistence
- Problem Completion
- GitHub Repository Integration
- Optional Solution Push
- AI Coding Hints where configured
- Multi-language runtime support

The execution layer is intended to support the programming languages exposed by the frontend.

---

# 📚 Aptitude

The platform contains a large database-backed aptitude question bank.

## Current Dataset

**22,060 questions**

| Category | Questions |
|---|---:|
| Quantitative Aptitude | 7,500 |
| Logical Reasoning | 5,760 |
| Verbal Ability | 5,200 |
| Data Interpretation | 3,600 |
| **Total** | **22,060** |

### Difficulty

| Difficulty | Questions |
|---|---:|
| Easy | 5,151 |
| Medium | 9,194 |
| Hard | 7,715 |

### Practice Features

- Topic-based practice
- Difficulty selection
- MCQ questions
- Answer checking
- Step-by-step derivations where available
- Previous/next navigation
- Timed practice
- Attempt tracking
- Performance data

---

# 📊 Dashboard & Performance

The dashboard is the central candidate workspace.

It brings together:

- Profile information
- Preparation progress
- Resume activity
- Interview activity
- Coding progress
- Aptitude activity
- Notifications
- Career information
- Preparation navigation

Performance functionality provides a consolidated view of the candidate's preparation journey.

---

# 🏆 Achievements

Achievement functionality is designed to recognize preparation milestones.

Possible areas include:

- Coding milestones
- Aptitude activity
- Interview activity
- Preparation progress
- Consistency milestones

---

# 🐙 GitHub Integration

GitHub is integrated into developer-focused workflows.

Capabilities include:

- GitHub OAuth authentication
- GitHub account connection
- Repository access where authorized
- Repository selection
- Coding solution synchronization
- Optional solution push workflows

OAuth tokens and credentials must remain protected and must not be exposed unnecessarily to the frontend.

---

# 🛡️ Admin & Authorization

Administrative functionality is protected by backend authorization.

Administrative areas include:

- Overview
- Users
- Plans
- Subscriptions
- Entitlements

Security model:

```text
ADMIN
  ↓
Administrative APIs

USER
  ↓
Candidate APIs
  ↓
No administrative access
```

Frontend role information must never be treated as sufficient authorization. Backend Spring Security rules are authoritative.

---

# 💳 Subscription & Payment

> Payment/Razorpay functionality is intentionally documented separately from the core preparation modules.

The project contains plan, subscription, entitlement, payment, and invoice functionality.

## Current Development/Test Pricing

| Plan | Test Price |
|---|---:|
| Pro | ₹1 |
| Elite | ₹2 |

These are **testing prices** and should not be confused with intended production pricing.

## Intended Production Pricing

| Plan | India | International |
|---|---:|---:|
| Starter | ₹0 | $0 |
| Pro | ₹399/month | $9/month |
| Elite | ₹799/month | $19/month |

## Referral

Referral code:

```text
SAMIR100
```

Intended discount:

- India: ₹100 flat discount
- International: $1 flat discount

Result:

```text
India:
Pro   ₹399 → ₹299
Elite ₹799 → ₹699

International:
Pro   $9 → $8
Elite $19 → $18
```

## Payment Security

The backend must be authoritative for:

- Plan
- Final amount
- Referral discount
- Payment status
- Subscription status
- Entitlements

Never trust a frontend-only payment-success flag.

Payment security should include:

- Signature verification
- Webhook verification
- Ownership checks
- Idempotency
- Duplicate webhook protection
- Duplicate payment protection
- Secure secret management

Environment variables:

```env
RAZORPAY_KEY_ID=
RAZORPAY_KEY_SECRET=
RAZORPAY_WEBHOOK_SECRET=
```

Never commit real payment credentials.

---

# 🏗️ Architecture

```text
┌─────────────────────────────────────────────┐
│                  FRONTEND                   │
│                                             │
│ React 19 + Vite + React Router              │
│ Bootstrap 5 + Custom CSS + Axios            │
│ Monaco Editor + UI Components               │
└──────────────────────┬──────────────────────┘
                       │ REST / OAuth
                       ▼
┌─────────────────────────────────────────────┐
│                  BACKEND                    │
│                                             │
│ Spring Boot 4 + Java 17                    │
│ Spring Security + JWT + OAuth2             │
│ REST + JPA + Hibernate                     │
└───────────────┬──────────────┬──────────────┘
                │              │
                ▼              ▼
       ┌──────────────┐  ┌────────────────┐
       │    MySQL     │  │   AI Services  │
       │              │  │                │
       │ Users        │  │ Groq           │
       │ Profiles     │  │ Gemini         │
       │ Problems     │  │ Ollama         │
       │ Attempts     │  └────────────────┘
       │ Progress     │
       └──────────────┘
                │
                ▼
       ┌──────────────────┐
       │ Code Execution   │
       │ Piston / Docker  │
       └──────────────────┘
```

---

# 🧰 Technology Stack

## Frontend

- React 19
- Vite
- JSX
- React Router 7
- Axios
- Bootstrap 5
- Custom CSS
- React Icons
- Monaco Editor
- Recharts
- jsPDF
- html2canvas
- Lottie React

## Backend

- Java 17+
- Spring Boot 4.0.6
- Spring Security
- Spring Data JPA
- Hibernate
- Maven
- JJWT
- OAuth2 Client
- BCrypt
- MySQL

## AI / External Services

- Groq
- Google Gemini
- Ollama
- Google OAuth
- GitHub OAuth
- GitHub API/integration
- Piston-compatible code execution
- Razorpay

---

# 📁 Project Structure

```text
AI-Placement-Platform/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/aiinterview/backend/
│   │   │   │       ├── config/
│   │   │   │       ├── controller/
│   │   │   │       ├── entity/
│   │   │   │       ├── repository/
│   │   │   │       ├── security/
│   │   │   │       └── service/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── Components/
│   │   ├── Pages/
│   │   ├── services/
│   │   ├── routes/
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
└── README.md
```

---

# 🔑 Configuration

Sensitive values should be supplied through environment variables or an ignored local configuration file.

Typical configuration categories:

```env
# Database
DB_URL=
DB_USERNAME=
DB_PASSWORD=

# JWT
JWT_SECRET=

# Google OAuth
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

# GitHub OAuth
GITHUB_CLIENT_ID=
GITHUB_CLIENT_SECRET=

# AI
GROQ_API_KEY=
GEMINI_API_KEY=

# Payment
RAZORPAY_KEY_ID=
RAZORPAY_KEY_SECRET=
RAZORPAY_WEBHOOK_SECRET=
```

Use the exact property names required by the current backend configuration.

**Never commit secrets, OAuth credentials, JWT secrets, AI keys, or payment secrets.**

---

# ▶️ Local Setup

## Prerequisites

Install:

- Java 17+
- Maven
- Node.js
- npm
- MySQL
- Git
- Docker (for the configured local code-execution environment)

Verify:

```bash
java -version
mvn -version
node -v
npm -v
mysql --version
git --version
docker --version
```

## Clone

```bash
git clone <repository-url>
cd AI-Placement-Platform
```

## Database

Create/configure the MySQL database according to the backend configuration.

Example:

```sql
CREATE DATABASE ai_platform;
```

Do not commit database credentials.

---

# ▶️ Start Backend

```bash
cd backend
mvn spring-boot:run
```

Default:

```text
http://localhost:8080
```

Local health/test endpoint:

```bash
curl http://localhost:8080/test
```

---

# ▶️ Start Frontend

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

Default:

```text
http://localhost:5173
```

---

# 🧪 Build

## Frontend

```bash
cd frontend
npm run build
```

## Backend

```bash
cd backend
mvn clean package -DskipTests
```

---

# 🧪 Authentication Smoke Test

## Email

```text
Register
  ↓
OTP
  ↓
Verify
  ↓
Login
  ↓
JWT
  ↓
/me
```

## Google / GitHub

```text
OAuth Provider
      ↓
Callback
      ↓
Account Lookup / Upsert
      ↓
JWT
      ↓
/me
```

## Onboarding

```text
New User
  ↓
Onboarding
  ↓
Complete Profile
  ↓
PUT /api/profile → 200
  ↓
profileCompleted = true
  ↓
Dashboard
```

---

# 🔐 Security Guidelines

1. Backend authorization is authoritative.
2. JWT validation occurs server-side.
3. Protected APIs must not be made public to bypass errors.
4. Frontend role values must never determine privileges.
5. Passwords must be BCrypt-hashed.
6. OAuth secrets remain server-side.
7. Payment signatures are verified server-side.
8. Webhook processing must be idempotent.
9. User data must be scoped to the authenticated user.
10. Secrets must never be committed to Git.
11. Database deletion must be treated as a destructive operation.
12. Authentication errors should be diagnosed from actual requests, responses, and backend logs.

---

# 🌐 Development Ports

| Service | Port |
|---|---:|
| React/Vite | `5173` |
| Spring Boot | `8080` |
| Piston | `2000` |
| MySQL | `3306` |

---

# 🐛 Troubleshooting

## Port 8080 Already in Use

PowerShell:

```powershell
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue |
Select-Object LocalAddress, LocalPort, OwningProcess, State
```

Then terminate only the process actually using the port:

```powershell
Stop-Process -Id <PID> -Force
```

## Port 5173 Already in Use

```powershell
Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue |
Select-Object LocalAddress, LocalPort, OwningProcess, State
```

## Authentication 401

Trace:

```text
Frontend Token
      ↓
Authorization Header
      ↓
JwtFilter
      ↓
JwtUtil
      ↓
UserDetails
      ↓
SecurityContext
      ↓
Controller
```

For profile completion:

```http
PUT /api/profile
Authorization: Bearer <valid-jwt>
Content-Type: application/json
```

Do not solve a 401 by making the endpoint public.

## OAuth Issues

Check:

- OAuth Client ID
- OAuth Client Secret
- Redirect URI
- CORS
- Callback route
- Spring Security OAuth configuration
- Authorization-request persistence
- Browser cookie/session behavior
- Provider permissions

Always inspect the actual browser Network request and backend logs before changing authentication architecture.

---

# 📈 Verified Project Scale

Current verified project metrics include:

- **22,060 aptitude questions**
- **6,260 coding problems**
- **~22,640 coding test cases**
- Multiple coding runtimes
- Public and hidden coding tests
- Persistent coding progress
- Persistent aptitude attempts
- Persistent interview sessions
- Persistent user profiles

---

# 🧩 Major Backend Domains

The backend is organized around domains including:

- Authentication
- User profiles
- OAuth
- Mock interviews
- Interview feedback
- Coding problems
- Coding submissions
- Code execution
- Coding progress
- GitHub integration
- Aptitude
- AI services
- Resume analysis
- Performance
- Notifications
- Runtime configuration
- Subscriptions
- Payments
- Entitlements

---

# 🎨 Major Frontend Areas

The application includes areas such as:

- Home
- Features
- Pricing
- Blog
- Documentation
- Roadmap
- Changelog
- About
- Contact
- Legal
- Accessibility
- Security
- Login
- Registration
- Forgot Password
- Reset Password
- Onboarding
- Dashboard
- Profile
- Resume Analyzer
- Mock Interview
- Interview Results
- Coding Arena
- Aptitude
- Performance
- Subscription/Billing
- Administrative functionality

---

# 🧱 Engineering Principles

### Candidate First
Keep the preparation journey focused and simple.

### Database Backed
Important user and progress data should persist server-side.

### Secure by Default
Authentication, authorization, ownership, and payment verification belong on the backend.

### Modular
Each major product area has separated frontend/backend responsibilities.

### AI Assisted
AI enhances preparation while deterministic application logic remains responsible for core workflows.

### Scalable
The architecture separates UI, APIs, persistence, AI, authentication, code execution, and payments.

---

# 🚀 Future Expansion

Potential future directions:

- Additional placement question banks
- More coding languages/runtimes
- Advanced AI interview modes
- Personalized study plans
- Company-specific preparation tracks
- Advanced analytics
- Resume version management
- GitHub portfolio analysis
- Expanded achievement systems
- Production payment rollout
- Cloud deployment
- Centralized observability
- Automated integration testing
- Performance optimization

---

# 📝 Development Rules

## Do

- Inspect existing code before modifying it
- Reuse existing services and controllers
- Preserve API contracts
- Test frontend and backend builds
- Test real browser flows
- Inspect backend logs
- Use Git before major changes
- Keep secrets outside source control
- Keep payment isolated from unrelated changes

## Do Not

- Rewrite working authentication without evidence
- Make protected APIs `permitAll` to hide errors
- Trust frontend payment status
- Trust frontend admin roles
- Delete database records casually
- Hardcode credentials
- Commit `.env` secrets
- Create duplicate authentication systems
- Claim a feature is fixed based only on compilation

---

# 🔄 Git Workflow

Before significant changes:

```bash
git status
git diff
git log --oneline -10
```

After changes:

```bash
git diff --stat
git diff
git status
```

Example focused commits:

```text
fix(auth): restore JWT profile authentication
fix(onboarding): redirect completed profiles to dashboard
feat(coding): add runtime support
fix(aptitude): correct attempt persistence
```

---

# 📄 License

Add the intended project license before public distribution.

Example:

```text
Copyright © Samprepix.
All rights reserved.
```

---

# 👨‍💻 Samprepix

**AI Placement & Interview Preparation Platform**

> **Learn → Practice → Interview → Analyze → Improve → Get Placement Ready**

---

## ⚠️ Production Checklist

Before public deployment, review:

- Environment variables
- OAuth redirect URIs
- CORS origins
- Database credentials
- JWT secret
- AI API keys
- Code-execution isolation
- Payment credentials
- Payment webhooks
- Rate limiting
- Logging
- Error handling
- Monitoring
- Backup and recovery
- HTTPS/TLS configuration

This README documents the project's architecture and intended workflows. Production deployment should always be validated against the actual current source configuration.
