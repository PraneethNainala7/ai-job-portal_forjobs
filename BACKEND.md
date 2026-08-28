# AI Job Portal — Backend Development Specification (Java + Spring Boot)

**Source:** AI Job Portal Software Requirements Specification v1.0  
**Project Type:** AI-Powered Recruitment Platform  
**Application Type:** Web Application

---

# 1. Purpose

This document defines the backend requirements for the AI Job Portal.

The platform connects and supports:

- **Admins**, who manage users, moderate jobs, monitor platform activity, and view platform statistics.
- **Candidates**, who create profiles, upload resumes, search jobs, receive AI recommendations, apply for jobs, and track applications.
- **Employers**, who manage company information and job postings, review applicants, use AI-assisted match information, shortlist/reject candidates, and generate interview questions.

The backend is responsible for:

- Authentication
- Authorization
- Business rules
- Database persistence
- Resume file handling
- Job and application management
- AI orchestration
- Match score calculation
- Candidate ranking
- Interview question generation

---

# 2. Recommended Backend Architecture

A modular architecture is recommended:

```text
Client / Frontend
       |
       v
REST API
       |
       +---------------------------+
       | Authentication Module     |
       | Candidate Module          |
       | Employer Module           |
       | Job Module                |
       | Application Module        |
       | AI Module                 |
       +---------------------------+
       |
       v
Business / Service Layer
       |
       +---------------------------+
       | Matching Engine           |
       | Resume Analysis           |
       | Job Analysis              |
       | Interview Question AI     |
       +---------------------------+
       |
       v
Database + File Storage
```

AI operations that may take longer should support asynchronous processing.


---

# Java + Spring Boot Technology Baseline

## Final Backend Stack

| Area | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Build Tool | Maven |
| REST API | Spring Web |
| Security | Spring Security |
| Authentication | JWT |
| Password Hashing | BCrypt |
| Persistence | Spring Data JPA |
| ORM | Hibernate |
| Database | PostgreSQL |
| Database Migration | Flyway |
| Validation | Jakarta Bean Validation |
| API Documentation | OpenAPI / Swagger |
| Mapping | MapStruct recommended |
| Logging | SLF4J + Logback |
| Testing | JUnit 5 + Mockito + Spring Boot Test |
| File Upload | Spring MultipartFile |
| AI Integration | Dedicated external AI client/service |

## Mandatory Spring Architecture

```text
Next.js
   ↓ REST API
Spring Security + JWT
   ↓
Controllers (thin)
   ↓
Services / Use Cases
   ↓
Spring Data JPA Repositories
   ↓
PostgreSQL + Flyway
```

## Java Implementation Rules

1. Use feature-oriented packages.
2. Controllers must remain thin and contain no complex business workflows.
3. Use request and response DTOs; do not expose JPA entities directly as API contracts.
4. Use Jakarta Bean Validation on request DTOs.
5. Put business rules and status transitions in services/use cases.
6. Use Spring Data JPA repositories for persistence.
7. Use `@Transactional` for multi-record workflows such as employer registration, approval, rejection, hold/release, and audit logging.
8. Centralize exception handling with `@RestControllerAdvice`.
9. Centralize account-status transition validation.
10. Use Flyway for all database schema changes.
11. Use Spring Security for role-based authorization and resource ownership checks in the application/service layer where necessary.
12. No normal production source file should exceed approximately 300 lines; split by responsibility when approaching the limit.

## Recommended Package Structure

```text
src/main/java/com/aijobportal/
├── config/
│   ├── security/
│   └── openapi/
├── common/
│   ├── exception/
│   ├── response/
│   └── util/
├── auth/
│   ├── controller/
│   ├── service/
│   ├── dto/
│   └── security/
├── admin/
├── candidate/
├── employer/
├── job/
├── application/
├── ai/
│   ├── service/
│   ├── client/
│   └── dto/
└── audit/
```

## Java-Specific Replacements for Earlier Generic/Node Terminology

- Middleware → Spring Security filters/interceptors where appropriate.
- Controller/service/routes → Spring `@RestController`, service classes, and annotation-based mappings.
- Validation schemas → request DTOs + Jakarta Bean Validation + service-level business validation.
- Repository/data access → Spring Data JPA repositories.
- ORM-specific implementation → Hibernate/JPA.
- Manual database migrations → Flyway versioned migrations.
- Route files → controller request mappings.

The functional requirements, employer approval workflow, mandatory CIN, account hold rules, audit logging, AI rules, APIs, and approximately 300-line maintainability rule remain unchanged; only the backend implementation stack is Java + Spring Boot.

---

# 3. User Roles

```text
ADMIN
CANDIDATE
EMPLOYER
```


## Admin permissions

An Admin can:

- Access the administrative dashboard
- View platform-level statistics
- View and search candidate accounts
- View and search employer accounts
- Activate/deactivate user accounts
- View all jobs
- Moderate jobs by closing/deactivating jobs according to business rules
- Delete users or jobs only if deletion is included in the final business rules
- View overall application activity for platform monitoring
- Monitor AI processing activity or failures if the feature is implemented

An Admin should not normally:

- Apply for jobs as an Admin
- Create jobs as an Admin
- Edit employer-authored job content as part of moderation
- Shortlist candidates
- Reject candidates
- Make automated hiring decisions

Administrative actions must be audited.

## Candidate permissions

A candidate can:

- Register
- Log in
- Manage own profile
- Upload own resume
- Search jobs
- View job details
- View recommendations
- View match information
- Apply for jobs
- View own applications

A candidate cannot:

- Create employer jobs
- Modify employer jobs
- View other candidates' private information

## Employer permissions

An employer can:

- Register
- Log in
- Manage employer/company information
- Create jobs
- Edit own jobs
- Close own jobs
- View applicants for own jobs
- View permitted applicant information
- View AI match information
- Shortlist candidates
- Reject candidates
- Generate interview questions

An employer cannot:

- Modify another employer's jobs
- View unrelated private candidate information

---

# 4. Authentication

## 4.1 Registration

### Candidate registration

Required data:

- Name
- Email
- Password
- Role = CANDIDATE

### Employer registration

Required data:

- Recruiter name
- Email
- Password
- Company name
- Company information

The system should create the user and the appropriate role-related profile/company data according to the implementation design.

---

## 4.2 Login

Input:

- Email
- Password

On successful authentication, return authentication information and the user's role.

The role determines the frontend dashboard:

```text
CANDIDATE → Candidate Dashboard
EMPLOYER  → Employer Dashboard
```

---

## 4.3 Logout

Invalidate the authenticated session/token according to the chosen authentication architecture.

The API contract includes:

```text
POST /api/auth/logout
```

---

# 5. Security Requirements

The backend should implement:

- HTTPS
- Password hashing
- Authentication
- Authorization
- Role-based access control
- Input validation
- File validation
- Rate limiting
- Secure API endpoints

Never rely on frontend route protection as the authorization boundary.

---

# 6. Database Model

The SRS defines the following initial data entities.

## 6.1 Users

```text
Users
- id
- name
- email
- password_hash
- role
- created_at
- updated_at
```

Role values:

- ADMIN
- CANDIDATE
- EMPLOYER

For account administration, the implementation should also support an account status field or equivalent state, for example:

- ACTIVE
- INACTIVE

---

## 6.2 Candidate Profiles

```text
CandidateProfiles
- id
- user_id
- phone
- location
- title
- experience
- skills
- education
- certifications
- portfolio_url
- linkedin_url
```

The exact normalization strategy for skills, education, and certifications may be selected during implementation.

---

## 6.3 Resumes

```text
Resumes
- id
- candidate_id
- file_url
- file_type
- parsed_text
- parsed_data
- created_at
```

`parsed_data` should contain structured AI extraction results.

---

## 6.4 Companies

```text
Companies
- id
- employer_id
- company_name
- description
- website
- location
```

---

## 6.5 Jobs

```text
Jobs
- id
- employer_id
- role
- experience
- skills
- location
- salary
- job_type
- description
- status
- created_at
- updated_at
```

---

## 6.6 Applications

```text
Applications
- id
- job_id
- candidate_id
- status
- applied_at
- updated_at
```

Supported statuses:

- Applied
- Under Review
- Shortlisted
- Rejected
- Interview
- Selected

Implementation may use normalized enum values while exposing consistent API values.

---

## 6.7 AI Match Results

```text
AI Match Results
- id
- job_id
- candidate_id
- match_score
- strong_areas
- gaps
- explanation
- created_at
```

---

## 6.8 Interview Questions

```text
Interview Questions
- id
- job_id
- candidate_id
- questions
- generated_at
```

---

# 7. Admin Management

## 7.1 Admin Dashboard Statistics

The backend should provide platform-level summary data such as:

- Total users
- Total candidates
- Total employers
- Total jobs
- Active jobs
- Closed jobs
- Total applications

Optional data:

- Recent registrations
- Recent job postings
- Recent application activity

## 7.2 Candidate Administration

Admin should be able to retrieve and search candidate accounts.

Potential administrative actions:

- View candidate account/profile
- Activate account
- Deactivate account
- Delete account if deletion is included in the final business rules

Sensitive resume access must follow privacy rules and should not be granted merely because a user has a generic administrative listing permission.

## 7.3 Employer Administration

Admin should be able to retrieve and search employer accounts.

Potential actions:

- View employer account
- View company information
- Activate account
- Deactivate account
- Delete account if deletion is included in the final business rules

## 7.4 Job Moderation

Admin should be able to:

- View all jobs
- Search and filter jobs
- View job details
- Close/deactivate jobs for moderation
- Delete jobs if deletion is included in the final business rules

Admin moderation should be separate from employer job ownership. Employers remain the normal authors and editors of their jobs.

## 7.5 Application Monitoring

Admin may retrieve overall application activity for platform monitoring.

Admin application monitoring should not grant authority to shortlist or reject candidates unless a separate future business requirement explicitly introduces that authority.

## 7.6 Administrative Audit Logging

Administrative actions should be recorded.

Recommended audit information:

```text
AuditLogs
- id
- admin_id
- action
- target_type
- target_id
- previous_value (optional)
- new_value (optional)
- created_at
```

Examples of auditable actions:

- User activated
- User deactivated
- User deleted
- Job closed
- Job deleted

---

# 8. Resume Upload and Processing

## 7.1 Supported formats

- PDF
- DOC
- DOCX

## 7.2 Required validation

Validate:

- File type
- File size
- Upload ownership
- Malformed or unsupported files according to implementation capabilities

## 7.3 Processing flow

```text
Candidate
   ↓
Upload Resume
   ↓
File Validation
   ↓
Secure File Storage
   ↓
Text Extraction
   ↓
AI Resume Analysis
   ↓
Structured Candidate Data
   ↓
Store Resume + Parsed Results
```

The extracted information may include:

- Skills
- Experience
- Job titles
- Technologies
- Education
- Certifications
- Projects
- Industries
- Seniority

---

# 8. Job Management

## 8.1 Create Job

Required fields:

- Role
- Experience
- Skills
- Location
- Salary
- Job type
- Description

Only authenticated employers may create jobs.

The job must be associated with the authenticated employer.

---

## 8.2 Edit Job

Only the employer that owns the job may edit it.

Authorization must verify ownership server-side.

---

## 8.3 Close Job

The employer should be able to close their own job.

Closed jobs should be handled consistently by search and application logic according to the final business rules.

---

## 8.4 View Employer Jobs

An employer should retrieve only jobs belonging to that employer.

The response should support applicant count where required.

---

# 9. Job Search

Candidates should be able to search using:

- Role
- Skills
- Location
- Experience
- Salary
- Job type

The API should support pagination and sorting as implementation improvements where appropriate.

Only jobs intended to be searchable should be returned.

---

# 10. Applications

## 10.1 Apply for Job

Required flow:

```text
Candidate
   ↓
Select Job
   ↓
View Match Information
   ↓
Apply
   ↓
Application Created
   ↓
Employer Applicant List
```

The backend should verify:

- User is a candidate
- Job exists
- Job is available for application according to status rules
- Candidate authorization

Duplicate application handling should be explicitly implemented according to the chosen business rule.

---

## 10.2 Candidate Application List

A candidate can retrieve only their own applications.

Response should include sufficient job information for display, such as:

- Job role
- Company
- Applied date
- Status

---

## 10.3 Employer Applicant List

An employer can retrieve applicants only for jobs owned by that employer.

Applicants should be sortable by:

- Match score
- Experience
- Application date

---

# 11. Shortlisting and Rejection

## Shortlist

```text
Applicant
   ↓
Employer Review
   ↓
Shortlist
   ↓
Status = Shortlisted
```

The backend must verify that the employer owns the job related to the application.

## Reject

```text
Applicant
   ↓
Employer Review
   ↓
Reject
   ↓
Status = Rejected
```

A rejection reason may optionally be stored if implemented.

Candidates should see updated application status when retrieving their applications.

---

# 12. AI Architecture

The SRS recommends the following conceptual architecture:

```text
AI LAYER
   |
   +----------------------------+
   |                            |
Resume Analysis             Job Analysis
   |                            |
   +------------+---------------+
                |
                v
         Structured Data
                |
                v
       Embedding Generation
                |
                v
         Matching Engine
                |
        +-------+-------+
        |               |
        v               v
Candidate Side      Employer Side
Job Match Score     Candidate Score
        |               |
        v               v
Recommendations   Strengths/Gaps
        |
        v
Interview Questions
```

The implementation can begin with a simpler synchronous architecture for an MVP and evolve toward queues/background processing.

---

# 13. AI Resume Analysis

## Endpoint

```text
POST /api/ai/resume/analyze
```

The AI process should extract information from the candidate's actual resume content.

Important reliability rule:

**The AI must not fabricate candidate information.**

It should not invent:

- Experience
- Skills
- Education
- Certifications
- Projects
- Employers

Store both:

- Source/parsed resume text
- Structured analysis output

---

# 14. AI Job Recommendations

## Endpoint

```text
POST /api/ai/jobs/recommend
```

The system compares candidate information with available jobs and returns relevant jobs ranked by relevance.

Example:

```text
.NET Developer       92%
Backend Developer    83%
Technical Consultant 72%
```

Recommendations should be explainable through stored or generated match factors.

---

# 15. Candidate-Job Match Score

## Endpoint

```text
POST /api/ai/jobs/:id/match
```

The score must be between:

```text
0% and 100%
```

The SRS states that matching should consider:

- Skills
- Required experience
- Role relevance
- Technology similarity
- Location
- Job type
- Other job requirements

Where possible, required and preferred skills should be distinguished.

---

# 16. Recommended Match Algorithm

The initial implementation should combine:

1. Deterministic/rule-based scoring
2. Semantic AI matching

Suggested weighting:

| Factor | Weight |
|---|---:|
| Required skills | 35% |
| Relevant experience | 25% |
| Role/title relevance | 15% |
| Semantic similarity | 10% |
| Location | 5% |
| Job preferences | 5% |
| Education/certification | 5% |

The exact algorithm can be tuned using test data.

Conceptually:

```text
Final Match Score
=
Weighted Rule Score
+
Semantic Similarity Score
```

The final implementation must ensure that the final score remains within 0–100%.

---

# 17. Strengths and Gaps

For every candidate-job comparison, the backend should identify:

## Strong Areas

Examples:

- C#
- .NET Core
- SQL

## Gaps

Examples:

- Azure
- Kubernetes

Where information is available, distinguish:

- Missing required skills
- Missing preferred skills
- Skills with insufficient evidence

The explanation should be stored or reproducible for later display.

---

# 18. Candidate Ranking

Employers may view candidates ranked by match score.

Example:

```text
1. Candidate A — 94%
2. Candidate B — 89%
3. Candidate C — 84%
4. Candidate D — 77%
```

Important business rule:

**Ranking is a decision-support mechanism and must not automatically determine hiring decisions.**

---

# 19. AI Interview Questions

## Endpoint

```text
POST /api/ai/interview-questions
```

Inputs:

```text
Job Description
+
Candidate Resume
+
Candidate Profile
```

The AI should generate candidate-specific questions in categories such as:

- Technical
- Resume-based
- Job-specific
- Behavioral

Questions must be grounded in:

- Candidate resume
- Job description
- Candidate skills
- Candidate experience

Important rule:

**The AI should not invent projects or experience that the candidate did not mention.**

Store generated questions with:

- Job ID
- Candidate ID
- Questions
- Generated timestamp

---

# 20. API Specification

## 20.1 Authentication

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
```

---

## 20.2 Admin

Recommended Admin API endpoints:

```http
GET    /api/admin/dashboard

GET    /api/admin/candidates
GET    /api/admin/candidates/:id
PUT    /api/admin/users/:id/activate
PUT    /api/admin/users/:id/deactivate
DELETE /api/admin/users/:id

GET    /api/admin/employers
GET    /api/admin/employers/:id

GET    /api/admin/jobs
GET    /api/admin/jobs/:id
PUT    /api/admin/jobs/:id/close
DELETE /api/admin/jobs/:id

GET    /api/admin/applications
GET    /api/admin/statistics
```

The exact routes may be adjusted during API design, but all endpoints must enforce the Admin role.

## 20.3 Candidate

```http
GET  /api/candidate/profile
PUT  /api/candidate/profile

POST /api/candidate/resume
GET  /api/candidate/resume

GET  /api/jobs
GET  /api/jobs/:id

POST /api/jobs/:id/apply

GET  /api/candidate/applications
```

---

## 20.4 AI

```http
POST /api/ai/resume/analyze
POST /api/ai/jobs/recommend
POST /api/ai/jobs/:id/match
POST /api/ai/interview-questions
```

---

## 20.5 Employer

```http
POST /api/employer/jobs
GET  /api/employer/jobs
GET  /api/employer/jobs/:id
PUT  /api/employer/jobs/:id

GET /api/employer/jobs/:id/applicants

PUT /api/employer/applications/:id/shortlist
PUT /api/employer/applications/:id/reject
```

---

# 21. Suggested Backend Module Structure

```text
src/
├── modules/
│   ├── auth/
│   │   ├── auth.controller
│   │   ├── auth.service
│   │   ├── auth.routes
│   │   └── auth.validation
│   │
│   ├── admin/
│   ├── candidate/
│   ├── employer/
│   ├── jobs/
│   ├── applications/
│   └── ai/
│
├── middleware/
│   ├── authenticate
│   ├── authorizeRole
│   ├── validateRequest
│   ├── errorHandler
│   └── rateLimit
│
├── database/
│   ├── migrations/
│   └── repositories/
│
├── storage/
├── config/
├── utils/
└── app
```

Names should be adapted to the selected backend framework.

---

# 22. Validation Rules

## Authentication

- Required email
- Valid email format
- Required password
- Duplicate email prevention

## Candidate profile

Validate expected field types and URL formats where applicable.

## Resume

- Allowed file formats
- File size
- Secure upload handling

## Job

Validate all required fields:

- Role
- Experience
- Skills
- Location
- Salary
- Job type
- Description

## Authorization

Validate:

- Authenticated user
- Correct role
- Resource ownership

---

# 23. Privacy Requirements

Candidate resumes contain personal information.

The backend should:

- Secure uploaded resumes
- Restrict resume access
- Prevent unauthorized downloads
- Provide appropriate data deletion functionality
- Define data-retention rules
- Clearly support communication of AI processing of candidate information

Resume URLs should not be treated as publicly accessible by default if private storage is used.

---

# 24. Error Handling

Use a consistent error response format.

Recommended categories:

- Validation error
- Authentication error
- Authorization error
- Not found
- Conflict
- File upload error
- AI processing error
- Internal server error

Do not expose internal stack traces or secrets to clients.

---

# 25. Performance and Scalability

The SRS targets approximately 2–3 seconds for standard API responses under normal conditions.

AI operations may be asynchronous.

The architecture should support future growth in:

- Candidates
- Employers
- Jobs
- AI requests
- Additional job sources

Recommended future improvements:

- Background workers
- Job queues
- Caching
- Database indexing
- Object storage
- AI request retry mechanisms
- Monitoring

---

# 26. Monitoring

Monitor at least:

- API errors
- AI failures
- Resume-processing failures
- Job creation failures
- Application failures

Log enough context for debugging without exposing sensitive resume data or credentials.

---

# 27. Testing Requirements

## Functional testing

Test:

- Registration
- Login
- Profile
- Resume upload
- Job creation
- Job editing
- Job search
- Applications
- Shortlisting
- Rejection

## AI testing

Test:

- Resume extraction
- Skill extraction
- Match percentage
- Missing skills
- Strong areas
- Job recommendations
- Interview questions

## Security testing

Test:

- Unauthorized access
- Role restrictions
- File upload security
- API authorization
- Resource ownership

---

# 28. Suggested Development Order

## Module 1

Authentication and authorization.

## Module 2

Admin dashboard and administration.

## Module 3

Candidate profile and resume upload.

## Module 4

Employer profile and company management.

## Module 5

Job creation and management.

## Module 6

Job search and applications.

## Module 7

AI resume analysis.

## Module 8

AI job matching.

## Module 9

Employer AI candidate matching.

## Module 10

AI interview questions.

---

# 29. Three-Day Prototype Scope

## Day 1 — Core Platform

Admin:

- Admin login
- Admin dashboard
- Basic user management
- Basic job monitoring

Candidate:

- Registration/login
- Profile
- Resume upload
- Resume parsing

Employer:

- Registration/login
- Employer profile
- Create job
- Edit job

Database:

- Users
- CandidateProfiles
- Companies
- Resumes
- Jobs

## Day 2 — AI Matching

```text
Resume
  ↓
AI Parser
  ↓
Candidate Skills
  ↓
Job Requirements
  ↓
AI Matching
  ↓
Match %
```

Implement:

- Candidate-side job recommendations
- Employer-side candidate match score
- Strengths
- Gaps

## Day 3 — Applications + Interview AI

Candidate:

- Search jobs
- View job
- Apply
- View applications

Employer:

- View applicants
- Candidate match score
- Shortlist
- Reject
- Generate AI interview questions

---

# 30. MVP Acceptance Criteria

The MVP is complete when:

1. An Admin can securely access the administrative dashboard.
2. An Admin can view and manage candidate and employer accounts according to role permissions.
3. An Admin can view and moderate jobs.
4. An Admin can view platform statistics.
5. A candidate can register and upload a resume.
2. AI can extract useful candidate information.
3. Employers can create job openings.
4. Candidates can search available jobs.
5. AI can recommend relevant jobs.
6. Each job has an explainable match percentage.
7. Candidates can apply.
8. Employers can view applicants.
9. Employers can see candidate match scores.
10. AI can identify strengths and gaps.
11. Employers can shortlist/reject candidates.
12. AI can generate candidate-specific interview questions.
13. Candidate and employer data are protected using role-based access control.

---

# 31. Important AI Business Rules

These rules are mandatory principles from the project specification:

1. AI output is decision-support information.
2. Match scores should be explainable.
3. Candidate ranking must not automatically determine hiring decisions.
4. AI must not claim certainty about candidate success.
5. AI must not fabricate candidate experience, skills, education, certifications, projects, or employers.
6. Interview questions must be grounded in actual candidate and job information.
7. Candidate resume information must be handled as sensitive data.

---

# 32. Backend Success Criteria

The backend is ready for the MVP when:

- Authentication works for both roles.
- Role-based access control is enforced.
- Candidates can manage their own data.
- Employers can manage only their own jobs.
- Resume upload and analysis work.
- Jobs can be created, edited, searched, and viewed.
- Applications can be created and tracked.
- Employers can manage applications for their own jobs.
- AI match scores are generated within 0–100%.
- Strengths and gaps are available.
- Candidate ranking is available as decision support.
- Candidate-specific interview questions can be generated.
- Resume access is protected.
- Errors and AI failures are handled safely.


---

# 33. Admin Backend Business Rules

1. `ADMIN` is a separate role from `CANDIDATE` and `EMPLOYER`.
2. Every Admin endpoint must require authentication and the `ADMIN` role.
3. Administrative authorization must be enforced on the backend and must not depend on frontend route protection.
4. Admin actions that change user or job state must verify the target resource and record an audit event.
5. Admin job moderation should not normally permit editing employer-authored job content.
6. Admin should not normally shortlist or reject candidates because these remain employer hiring decisions.
7. Admin access to candidate resumes and private information must follow explicit privacy and authorization rules.
8. Deactivation behavior must be consistently enforced. An inactive user should not be able to authenticate or perform protected actions.
9. Deletion behavior must define whether records are permanently deleted or soft-deleted and how related data is handled.
10. AI monitoring is administrative observability only and must not alter the AI decision-support principles.

# 34. Recommended Admin Data Additions

## Users

Add an account status field:

```text
account_status
```

Recommended values:

```text
ACTIVE
INACTIVE
```

## Audit Logs

Add an `AuditLogs` entity:

```text
AuditLogs
- id
- admin_id
- action
- target_type
- target_id
- previous_value
- new_value
- created_at
```

The exact database types and relationships should be finalized during system design.


---

# 35. Employer Approval and Account Hold Requirements

## 35.1 Account Status Model

The system shall support:

```text
PENDING
ACTIVE
ON_HOLD
REJECTED
INACTIVE
```

Recommended interpretation:

- `PENDING`: waiting for Admin approval
- `ACTIVE`: permitted to use role-authorized features
- `ON_HOLD`: temporarily restricted by Admin
- `REJECTED`: employer registration was not approved
- `INACTIVE`: disabled account

The implementation should define allowed transitions centrally and should not duplicate transition rules across controllers.

## 35.2 Candidate Lifecycle

Candidate registration creates an `ACTIVE` account.

An Admin may:

```text
ACTIVE ↔ ON_HOLD
ACTIVE → INACTIVE
```

An `ON_HOLD` or `INACTIVE` candidate must not authenticate into protected features or perform protected actions.

Existing applications and historical records must remain preserved.

## 35.3 Employer Registration Requirements

Employer registration shall require:

- Recruiter name
- Email
- Password
- Company name
- Company information/description
- Company location
- CIN

CIN is mandatory.

A newly registered Employer shall receive:

```text
role = EMPLOYER
account_status = PENDING
```

Admin accounts must not be publicly self-registered.

## 35.4 Employer Approval Workflow

```text
REGISTER
   ↓
PENDING
   ↓
ADMIN REVIEW
 ┌─┴───────────┐
 ↓             ↓
ACTIVE      REJECTED
               ↓
      UPDATE + RESUBMIT
               ↓
             PENDING
```

Admin actions must be validated as explicit status transitions.

## 35.5 Pending Employer Restrictions

A `PENDING` Employer may authenticate only to the limited approval-status experience if the authentication design permits it.

A `PENDING` Employer must not:

- Create jobs
- Publish jobs
- Edit employer hiring data
- View applicants
- Shortlist candidates
- Reject candidates
- Generate interview questions

The backend must enforce these restrictions independently of frontend controls.

## 35.6 Employer Rejection and Resubmission

When rejecting an Employer, the Admin should provide a reason.

Recommended fields:

```text
status_reason
status_changed_by
status_changed_at
```

A rejected Employer may update company information and resubmit.

Resubmission transitions:

```text
REJECTED → PENDING
```

Resubmission must not automatically restore `ACTIVE` status.

## 35.7 Employer On Hold

An Admin may place an active Employer on hold.

Recommended business rule for this project:

```text
EMPLOYER ON_HOLD
        ↓
Employer cannot use protected employer actions
        ↓
Employer's ACTIVE jobs become unavailable
for new Candidate search and applications
        ↓
Existing applications remain preserved
```

Do not permanently delete jobs or applications merely because an Employer is placed on hold.

When the Employer is reactivated, job restoration must follow an explicit business rule and should not blindly reactivate jobs that were previously closed by the Employer.

## 35.8 CIN Validation

For the MVP:

```text
CIN REQUIRED
+
Basic format validation
+
Admin manual review
```

The backend must not claim that a CIN is officially government-verified unless a verified external integration is actually implemented.

## 35.9 Recommended Data Model Changes

### Users

```text
Users
- id
- name
- email
- password_hash
- role
- account_status
- status_reason
- status_changed_by
- status_changed_at
- created_at
- updated_at
```

### Companies

```text
Companies
- id
- employer_id
- company_name
- description
- website
- location
- cin
- created_at
- updated_at
```

CIN should have a uniqueness constraint if the business rule is that one company registration cannot reuse the same CIN.

### AuditLogs

```text
AuditLogs
- id
- admin_id
- action
- target_type
- target_id
- previous_status
- new_status
- reason
- created_at
```

All approval, rejection, hold, release, and activation actions must be audited.

## 35.10 Admin Employer Approval APIs

Recommended API surface:

```http
GET  /api/admin/employers/pending
GET  /api/admin/employers/:id

PUT  /api/admin/employers/:id/approve
PUT  /api/admin/employers/:id/reject
PUT  /api/admin/employers/:id/hold
PUT  /api/admin/employers/:id/activate
PUT  /api/admin/employers/:id/resubmit
```

Reject/hold requests should support an explicit reason where required.

Example:

```json
{
  "reason": "Company information could not be approved."
}
```

---

# 36. Backend Development Rules and Code Quality Standards

These rules are mandatory for implementation.

## 36.1 File Size Rule

No normal production source file should exceed **300 lines**.

When a file approaches 300 lines:

- Split responsibilities by layer or feature.
- Extract validation.
- Extract mapping/serialization.
- Extract reusable domain logic.
- Extract repositories.
- Extract helpers only when they represent a real reusable responsibility.

Do not create artificial fragmentation merely to satisfy a line count.

## 36.2 Controller Rule

Controllers must remain thin.

Controllers should primarily:

1. Receive request data.
2. Validate/authenticate through middleware.
3. Call a service/use case.
4. Return a standardized response.

Business rules must not be duplicated across controllers.

## 36.3 Service Rule

Business workflows belong in services/use cases.

Examples:

```text
services/
├── employer-approval.service
├── account-status.service
├── application.service
└── matching.service
```

Complex workflows should be split by responsibility.

## 36.4 Repository Rule

Database access should be isolated from controllers and business workflow logic where the selected backend architecture supports repositories.

Repositories should not contain HTTP concerns.

## 36.5 Validation Rule

Request validation schemas should be separate from controllers.

Examples:

```text
validations/
├── auth.schema
├── employer.schema
├── job.schema
└── admin.schema
```

## 36.6 Central Status Transition Rule

Account status transitions must be handled through a single domain/service layer.

Do not scatter logic such as:

```text
if status === ...
```

across unrelated controllers.

The status service should verify:

- Current status
- Requested status
- Actor role
- Valid transition
- Reason requirement
- Side effects
- Audit logging

## 36.7 Transaction Rule

Use database transactions for workflows that update multiple records.

Examples:

- Employer registration creating User + Company
- Employer approval updating status + audit log
- Employer hold updating account + affected job visibility + audit log
- Application creation with historical match snapshot when implemented

## 36.8 API Response Rule

Use a consistent response contract.

Example:

```json
{
  "success": true,
  "message": "Employer approved successfully.",
  "data": {}
}
```

Errors should follow a consistent structure.

## 36.9 Backend Module Structure

Recommended feature-oriented structure:

```text
src/
├── modules/
│   ├── auth/
│   │   ├── controllers/
│   │   ├── services/
│   │   ├── repositories/
│   │   ├── routes/
│   │   ├── schemas/
│   │   └── types/
│   ├── admin/
│   ├── candidate/
│   ├── employer/
│   ├── jobs/
│   ├── applications/
│   └── ai/
├── shared/
│   ├── middleware/
│   ├── errors/
│   ├── responses/
│   └── utils/
└── config/
```

## 36.10 Testing Rule

Every important status transition must have automated tests.

At minimum test:

- Employer registers as `PENDING`
- Pending Employer cannot create a job
- Admin approves Employer
- Approved Employer can create a job
- Admin rejects Employer
- Rejected Employer can resubmit
- Admin places Candidate on hold
- Held Candidate cannot perform protected actions
- Admin places Employer on hold
- Held Employer jobs are unavailable for new applications
- Existing applications remain preserved
- Every Admin status action creates an audit record
