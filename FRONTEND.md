# AI Job Portal — Frontend Development Specification

**Source:** AI Job Portal Software Requirements Specification v1.0  
**Project Type:** AI-Powered Recruitment Platform  
**Application Type:** Web Application

---

## 1. Purpose

This document defines the frontend requirements and implementation scope for the AI Job Portal.

The application supports three roles:

- **Admin** — manages users, moderates jobs, monitors platform activity, and views platform statistics.
- **Candidate** — searches for jobs, manages a profile and resume, receives AI recommendations, views match scores, applies for jobs, and tracks applications.
- **Employer** — manages company information and jobs, reviews applicants, uses AI match results, shortlists/rejects candidates, and generates interview questions.

The frontend should present AI outputs as **decision-support information**, not as automatic hiring decisions.

---

## 2. Required Frontend Technology Stack

The frontend shall be developed using **Next.js**.

## Core Stack

- Next.js
- React
- TypeScript
- Next.js App Router
- Tailwind CSS
- Fetch API or Axios
- React Hook Form
- Zod for validation
- TanStack Query / React Query for server-state management (recommended)

## Next.js Architecture

The application should use the Next.js `app` directory and App Router.

Recommended responsibilities:

- **Server Components** — initial page rendering and server-side data loading where appropriate.
- **Client Components** — forms, interactive UI, file uploads, filtering, modals, and client-side state.
- **Route Groups** — organize authentication, candidate, and employer sections.
- **Middleware** — optional first-level route protection and redirects.
- **Backend API** — remains the primary source for authentication, authorization, business logic, database access, and AI processing.

Next.js route protection must not replace backend authorization.

---


## 2.1 AI Coding Agent and Taste Skill Rules

### Purpose

AI coding tools may be used to implement the frontend, but they must treat the project documents as the source of truth. Design-quality guidance such as Taste Skill must improve the implementation quality without changing business requirements or inventing product behavior.

### Required Reading Order Before Implementation

Before implementing a page, feature, or component, the coding agent must follow this order:

```text
1. AI Job Portal Requirements / SRS
2. FRONTEND.md
3. UI_UX_DESIGN.md
4. Applicable backend/API contract
5. Taste Skill or equivalent design-quality guidance
```

### Source-of-Truth Priority

When instructions conflict, use this priority order:

```text
Business Requirements / SRS
        ↓
Frontend Functional Requirements (FRONTEND.md)
        ↓
UI/UX Design Specification (UI_UX_DESIGN.md)
        ↓
Backend / API Contract
        ↓
Taste Skill Design Guidance
        ↓
General AI Suggestions
```

Taste Skill must never override a functional requirement, business rule, role restriction, account-status rule, API contract, or approved project workflow.

### Mandatory AI Agent Rules

Before considering a frontend task complete, the AI coding agent must:

1. Implement only approved pages, workflows, and actions.
2. Never invent new business features or silently change existing workflows.
3. Follow the project's role-based navigation and authorization requirements.
4. Respect account status in routing and UI behavior, including pending, rejected, active, and on-hold experiences where applicable.
5. Follow the color, typography, spacing, component, animation, and responsive rules defined in `AI_JOB_PORTAL_UI_UX_DESIGN.md`.
6. Use Taste Skill principles to improve visual hierarchy, layout quality, spacing, typography, composition, and overall interface polish.
7. Avoid generic template-like dashboards and repetitive AI-generated UI patterns.
8. Reuse existing components and design tokens before creating new variations.
9. Keep normal production source files approximately **300 lines or less**. Split files by responsibility when they become difficult to read or maintain.
10. Keep pages focused on composition; move reusable UI, large sections, complex forms, tables, dialogs, and business interaction logic into appropriate components, hooks, or modules.
11. Implement loading, empty, error, and success states for every meaningful data-driven experience.
12. Verify desktop, tablet, and mobile behavior before considering a page complete.
13. Preserve keyboard accessibility, visible focus states, semantic labels, sufficient contrast, and accessible feedback.
14. Clearly label AI-generated content as AI insight, recommendation, analysis, or generated output where appropriate.
15. Do not present AI output as an automatic or final hiring decision.
16. Do not replace confirmed API data with invented placeholder values in production behavior.
17. Do not perform broad refactoring outside the requested feature unless required for correctness, security, or a documented dependency.

### Taste Skill Usage Boundaries

Taste Skill should influence **how the UI is designed and implemented**, including:

- Visual hierarchy
- Typography quality
- Spacing consistency
- Layout composition
- Component clarity
- Information density
- Interaction feedback
- Responsive behavior
- Motion restraint
- Avoidance of generic-looking UI

Taste Skill must not independently decide:

- New user roles
- New pages
- New database fields
- New business workflows
- New permissions
- Hiring decisions
- Employer approval rules
- Account hold/release rules
- Required registration fields
- API behavior

### Pre-Implementation Checklist

```text
[ ] Read the relevant SRS requirements
[ ] Read the relevant FRONTEND.md section
[ ] Read the relevant UI_UX_DESIGN.md section
[ ] Confirm the API/data contract
[ ] Confirm role and account-status access
[ ] Identify reusable components
[ ] Apply Taste Skill quality principles
[ ] Plan responsive behavior
[ ] Plan loading, empty, error, and success states
[ ] Plan accessibility requirements
```

### Completion Checklist

```text
[ ] Functional requirements are implemented
[ ] No unapproved workflow was invented
[ ] Role and account-status restrictions work correctly
[ ] UI matches UI_UX_DESIGN.md
[ ] Taste Skill principles improved quality without changing requirements
[ ] Components are reusable and responsibilities are separated
[ ] Normal source files remain approximately 300 lines or less
[ ] Loading state works
[ ] Empty state works
[ ] Error state works
[ ] Success feedback works
[ ] Desktop behavior verified
[ ] Tablet behavior verified
[ ] Mobile behavior verified
[ ] Keyboard and accessibility behavior checked
```

---

# 3. Frontend Application Structure

```text
app/
├── (public)/
│   ├── page.tsx
│   └── jobs/
│       ├── page.tsx
│       └── [id]/page.tsx
│
├── (auth)/
│   ├── login/page.tsx
│   └── register/page.tsx
│
├── candidate/
│   ├── layout.tsx
│   ├── dashboard/page.tsx
│   ├── profile/page.tsx
│   ├── resume/page.tsx
│   ├── jobs/page.tsx
│   └── applications/page.tsx
│
├── employer/
│   ├── layout.tsx
│   ├── dashboard/page.tsx
│   ├── profile/page.tsx
│   ├── jobs/
│   │   ├── page.tsx
│   │   ├── create/page.tsx
│   │   └── [id]/
│   │       ├── page.tsx
│   │       └── edit/page.tsx
│   └── applicants/
│       └── [jobId]/page.tsx
│
├── layout.tsx
├── globals.css
└── providers.tsx

components/
├── common/
├── layout/
├── auth/
├── candidate/
├── employer/
├── jobs/
└── ai/

lib/
├── api/
│   ├── auth.ts
│   ├── candidate.ts
│   ├── employer.ts
│   ├── jobs.ts
│   └── ai.ts
├── auth/
├── utils/
└── validations/

hooks/
types/
middleware.ts
```

---

# 4. Main Navigation

## 4.1 Admin Navigation

Suggested navigation:

- Dashboard
- Users
  - Candidates
  - Employers
- Jobs
- Applications
- Platform Statistics
- Logout

## 4.2 Candidate Navigation

Suggested navigation:

- Dashboard
- My Profile
- My Resume
- Search Jobs
- My Applications
- Logout

## 4.3 Employer Navigation

Suggested navigation:

- Dashboard
- My Jobs
- Create Job
- Applicants
- Company/Profile
- Logout

Navigation should be role-aware. Candidate-only screens must not be displayed as available actions to employers, and employer-only screens must not be displayed as available actions to candidates.

---

# 5. Public Screens

## 5.1 Login

### Purpose

Authenticate an existing candidate or employer.

### Fields

- Email
- Password

### Actions

- Login
- Navigate to registration

### Expected behavior

After successful login, redirect based on role:

- Candidate → Candidate Dashboard
- Employer → Employer Dashboard

### Validation

- Email required
- Valid email format
- Password required

### States

- Idle
- Loading
- Authentication error
- Success

---

## 5.2 Registration

The UI should allow role selection.

### Candidate Registration

Fields:

- Name
- Email
- Password

### Employer Registration

Fields:

- Recruiter name
- Email
- Password
- Company name
- Company information

### UI Behavior

The registration form may dynamically display different fields depending on the selected role.

---

# 6. Candidate Frontend

## 6.1 Candidate Dashboard

### Main sections

- Welcome area
- AI Recommended Jobs
- Match percentage for each recommendation
- Quick links to profile, resume, search, and applications

### Recommended Job Card

Each recommendation should display:

- Job role
- Company
- Location where available
- Match percentage
- Job type where available
- View details action

Example presentation:

```text
.NET Developer
ABC Technologies
92% Match

[ View Job ]
```

Recommendations should be ranked by relevance.

---

## 6.2 Candidate Profile

### Editable fields

- Full name
- Email
- Phone
- Location
- Professional title
- Skills
- Experience
- Education
- Certifications
- LinkedIn/Profile URL
- Portfolio/GitHub URL

### Features

- View profile
- Edit profile
- Save changes
- Display validation errors
- Show API success/error feedback

### Skills UI

A tag/chip input is recommended for skills.

---

## 6.3 Resume Screen

### Features

- Display currently uploaded resume
- Upload a new resume
- Show upload progress
- Show validation errors
- Display parsed/AI profile information after analysis

### Supported formats

- PDF
- DOC
- DOCX

### AI Analysis Display

The frontend should be capable of displaying extracted information such as:

- Skills
- Experience
- Job titles
- Technologies
- Education
- Certifications
- Projects
- Industries
- Seniority

### Suggested sections

```text
My Resume
----------------
resume.pdf

[ Upload Resume ]

AI Profile
----------------
Primary Skills
- C#
- .NET Core
- SQL
- REST API

Additional Skills
- Docker
- Azure

Experience
4 years
```

The frontend should clearly distinguish the original resume from AI-generated extracted information.

---

## 6.4 Job Search

### Search filters

- Role
- Skills
- Location
- Experience
- Salary
- Job type

### Features

- Search form
- Clear/reset filters
- Loading state
- Empty result state
- Error state
- Job result cards/list

### Job Result Card

Recommended fields:

- Role
- Company
- Location
- Experience
- Salary
- Job type
- Short description
- Match percentage where available
- View Details

---

## 6.5 Job Details

The screen should display:

- Role
- Company
- Experience
- Skills
- Location
- Salary
- Job type
- Description
- Posted date

For an authenticated candidate with available AI analysis, also display:

- Match score
- Strong areas
- Gaps
- Apply action

### Match Explanation UI

Example:

```text
92% Match

Strong Areas
✓ C#
✓ .NET Core
✓ SQL
✓ REST APIs

Gaps
⚠ Azure
⚠ Kubernetes
```

Where available, the UI should distinguish:

- Missing required skills
- Missing preferred skills
- Skills with insufficient evidence

---

## 6.6 Apply for Job

### Flow

```text
Open Job
   ↓
View Match Score
   ↓
Apply
   ↓
Application Created
```

### UI requirements

- Apply button
- Loading state while submitting
- Success confirmation
- API error message
- Prevent duplicate actions when already applied, if the backend provides that information

After successful application, the candidate should be able to navigate to My Applications.

---

## 6.7 My Applications

Display submitted applications.

### Application Card/Table Fields

- Job role
- Company
- Applied date
- Application status

### Supported statuses

- Applied
- Under Review
- Shortlisted
- Rejected
- Interview
- Selected

Status should be visually distinguishable without relying only on color.

---

# 7. Employer Frontend

## 7.1 Employer Dashboard

Display summary information such as:

- Number of jobs
- Number of applicants
- Number of shortlisted candidates
- Number of interviews

Main actions:

- View My Jobs
- Create Job
- View Applicants
- Manage Profile

---

## 7.2 Employer / Company Profile

The UI should support managing company information including:

- Company name
- Description
- Website
- Location

---

## 7.3 Create Job

### Required fields

- Role
- Experience
- Skills
- Location
- Salary
- Job type
- Description

### Frontend requirements

- Form validation
- Skills tag/chip input recommended
- Submit loading state
- Success feedback
- Error feedback

---

## 7.4 My Jobs

Display jobs created by the authenticated employer.

Each job should show:

- Role
- Applicant count
- Status

Actions:

- View
- Edit
- Close job

Example:

```text
Senior .NET Developer
Applicants: 34
Status: Active

[ View ] [ Edit ] [ Close ]
```

---

## 7.5 Edit Job

The edit screen should:

- Prepopulate existing job values
- Validate required fields
- Submit updates
- Display errors
- Prevent access to unauthorized jobs through frontend route protection

Backend authorization remains the actual security control.

---

## 7.6 Applicant List

Employers should be able to view applicants for their own jobs.

### Display fields

- Candidate name
- Match score
- Experience where available
- Application date
- Application status

### Sorting

The UI should support backend-provided or client-side sorting for:

- Match score
- Experience
- Application date

Example:

```text
Candidate       Match
John Smith      94%
David Kumar     88%
Rahul Sharma    84%
```

---

## 7.7 Candidate Details

The employer view should display relevant applicant information permitted by the backend.

AI sections:

### Match Score

```text
Candidate Match: 94%
```

### Strong Areas

- C#
- .NET Core
- SQL

### Gaps

- Azure
- Kubernetes

### Actions

- Shortlist
- Reject
- Generate AI Interview Questions

The UI should not imply that AI automatically makes the hiring decision.

---

## 7.8 Shortlist Candidate

On successful action:

- Update application status to `Shortlisted`
- Show success feedback
- Refresh candidate/application information

Optional confirmation dialog is recommended.

---

## 7.9 Reject Candidate

On successful action:

- Update application status to `Rejected`
- Show success feedback
- Refresh candidate/application information

The backend may optionally support a rejection reason. If implemented, display a reason field in a confirmation form/modal.

---

## 7.10 AI Interview Questions

Employers can request questions for a specific candidate and job.

The UI should display categorized questions:

- Technical
- Resume-based
- Job-specific
- Behavioral

Questions must be presented as AI-generated decision-support content.

Recommended states:

- Not generated
- Generating
- Generated
- Generation failed

---


# 7. Admin Frontend

## 7.1 Admin Dashboard

The Admin Dashboard should provide a high-level view of the platform.

### Summary cards

Display:

- Total candidates
- Total employers
- Total users
- Total jobs
- Active jobs
- Closed jobs
- Total applications

### Optional MVP dashboard content

- Recent registrations
- Recent job postings
- Recent application activity

The dashboard should present administrative monitoring information and should not expose unnecessary private candidate data.

---

## 7.2 Candidate Management

Admin should be able to:

- View all candidate accounts
- Search candidates
- Filter candidates
- View a candidate profile
- Activate/deactivate a candidate account
- Delete a candidate account if this action is included in the final business rules

### Candidate list fields

Recommended fields:

- Name
- Email
- Professional title
- Location
- Experience
- Account status
- Registration date

Sensitive resume content should not automatically be displayed in list views.

---

## 7.3 Employer Management

Admin should be able to:

- View all employer accounts
- Search employers
- Filter employers
- View employer and company information
- Activate/deactivate an employer account
- Delete an employer account if this action is included in the final business rules

### Employer list fields

Recommended fields:

- Recruiter name
- Email
- Company name
- Company location
- Account status
- Registration date

---

## 7.4 Job Management

Admin should be able to:

- View all jobs
- Search jobs
- Filter jobs
- View job details
- Close/deactivate a job for moderation purposes
- Delete a job if this action is included in the final business rules

### Important rule

Admin moderation should not normally allow the Admin to edit the employer's job content. Employers remain responsible for creating and editing their own jobs.

### Job list fields

- Role
- Company
- Employer
- Location
- Job type
- Status
- Posted date
- Applicant count where available

---

## 7.5 Application Monitoring

Admin may view overall application activity for platform monitoring.

Recommended fields:

- Job role
- Candidate
- Company
- Employer
- Application status
- Applied date

Admin should not normally make hiring decisions such as shortlisting or rejecting candidates.

---

## 7.6 User Account Status Actions

Where supported by the backend, the UI should provide confirmation before:

- Deactivating an account
- Activating an account
- Deleting an account

The UI should refresh the relevant list and display clear success or error feedback.

---

## 7.7 Platform Statistics

The Admin should be able to view platform-level statistics such as:

- Total users
- Total candidates
- Total employers
- Total jobs
- Active jobs
- Closed jobs
- Total applications

The initial MVP can use simple summary cards rather than advanced analytics.

---

## 7.8 Admin AI Monitoring

For the MVP, this can be a simple administrative view if backend data is available.

Potential information:

- Resume analysis requests
- Job matching requests
- Interview question generation requests
- Failed AI operations

This is monitoring information only and should not change the underlying AI hiring decision-support rules.

---

# 8. Authentication and Authorization UI

## 8.1 Next.js Protected Routes

Unauthenticated users must be redirected away from protected application routes.

Examples:

- `/admin/*`
- `/candidate/*`
- `/employer/*`

Protection can be implemented using Next.js middleware, server-side authentication checks, and/or protected layouts depending on the authentication architecture.

## 8.2 Role-Based Routes

Admin routes must require the Admin role.

Candidate routes must require the Candidate role.

Employer routes must require the Employer role.

Recommended Next.js organization:

```text
/admin/*
/candidate/*
/employer/*
```

Each role area can use its own protected `layout.tsx`.

The frontend must not rely solely on route hiding or middleware for security; the backend must enforce authorization independently.

## 8.3 Logout

Logout should:

- Call the logout API where applicable
- Clear local authentication state
- Clear cached protected data
- Redirect to login

---

# 9. API Integration

The frontend should organize API calls by domain.

## Authentication

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`

## Candidate

- `GET /api/candidate/profile`
- `PUT /api/candidate/profile`
- `POST /api/candidate/resume`
- `GET /api/candidate/resume`
- `GET /api/jobs`
- `GET /api/jobs/:id`
- `POST /api/jobs/:id/apply`
- `GET /api/candidate/applications`

## AI

- `POST /api/ai/resume/analyze`
- `POST /api/ai/jobs/recommend`
- `POST /api/ai/jobs/:id/match`
- `POST /api/ai/interview-questions`

## Employer

- `POST /api/employer/jobs`
- `GET /api/employer/jobs`
- `GET /api/employer/jobs/:id`
- `PUT /api/employer/jobs/:id`
- `GET /api/employer/jobs/:id/applicants`
- `PUT /api/employer/applications/:id/shortlist`
- `PUT /api/employer/applications/:id/reject`

---

# 10. Recommended Frontend Types

```ts
type UserRole = "ADMIN" | "CANDIDATE" | "EMPLOYER";

type ApplicationStatus =
  | "APPLIED"
  | "UNDER_REVIEW"
  | "SHORTLISTED"
  | "REJECTED"
  | "INTERVIEW"
  | "SELECTED";

interface CandidateProfile {
  id: string;
  fullName: string;
  email: string;
  phone?: string;
  location?: string;
  title?: string;
  experience?: string;
  skills: string[];
  education?: string[];
  certifications?: string[];
  portfolioUrl?: string;
  linkedinUrl?: string;
}

interface Job {
  id: string;
  role: string;
  companyName?: string;
  experience: string;
  skills: string[];
  location: string;
  salary: string;
  jobType: string;
  description: string;
  status: string;
  postedDate?: string;
}

interface MatchResult {
  jobId: string;
  candidateId: string;
  matchScore: number;
  strongAreas: string[];
  gaps: string[];
  explanation?: string;
}

interface Application {
  id: string;
  jobId: string;
  candidateId: string;
  status: ApplicationStatus;
  appliedAt: string;
}
```

Exact API field names should match the backend contract.

---

# 11. Loading, Error, and Empty States

Every data-driven screen should handle:

- Initial loading
- Action loading
- Success
- Empty data
- API/network error

Examples:

### Resume

- No resume uploaded
- Uploading
- Processing
- AI analysis complete
- AI analysis failed

### Job Search

- Initial state
- Searching
- Results found
- No jobs found
- Search failed

### AI Generation

- Not started
- Generating
- Completed
- Failed

---

# 12. Responsive Design

The application should support:

- Desktop
- Tablet
- Mobile

Responsive behavior should include:

- Collapsible navigation
- Responsive tables or card alternatives
- Mobile-friendly forms
- Accessible action buttons
- Readable AI analysis sections

---

# 13. Accessibility Requirements

Recommended minimum implementation:

- Semantic HTML
- Labels for form controls
- Keyboard navigation
- Visible focus states
- Accessible error messages
- Sufficient contrast
- Status text not conveyed only by color

---

# 14. Frontend Validation

Validation should exist for usability, but backend validation remains mandatory.

Examples:

### Registration

- Required fields
- Valid email
- Password requirements defined by backend

### Profile

- URL format validation for LinkedIn/Portfolio where applicable

### Resume

- Allowed file types
- File size validation according to backend configuration

### Job

- All required fields completed

---

# 15. Suggested Development Order

## Phase 1 — Core UI

1. Create Next.js application
2. Configure TypeScript and Tailwind CSS
3. Set up App Router structure
4. Create authentication screens
5. Configure role-based navigation
6. Configure protected layouts and/or middleware

## Phase 2 — Admin

1. Admin dashboard
2. Candidate management
3. Employer management
4. Job moderation
5. Application monitoring
6. Platform statistics

## Phase 3 — Candidate

1. Candidate dashboard
2. Profile
3. Resume upload
4. Resume analysis display
5. Job search
6. Job details
7. Apply
8. My applications

## Phase 4 — Employer

1. Employer dashboard
2. Company profile
3. Create job
4. My jobs
5. Edit job
6. Applicant list
7. Candidate details
8. Shortlist/reject

## Phase 5 — AI

1. AI resume analysis UI
2. Job recommendations
3. Match explanation
4. Strengths and gaps
5. Interview question generation

## Phase 6 — Final Quality

1. Error states
2. Loading states
3. Responsive design
4. Accessibility
5. API integration testing
6. Role-based UI testing

---

# 16. MVP Completion Checklist

## Admin

- [ ] Admin login
- [ ] Admin dashboard
- [ ] View/search/filter candidates
- [ ] View/search/filter employers
- [ ] Activate/deactivate user accounts
- [ ] View/search/filter jobs
- [ ] Close/deactivate jobs for moderation
- [ ] Application monitoring
- [ ] Platform statistics

## Candidate

- [ ] Registration/login
- [ ] Profile
- [ ] Resume upload
- [ ] AI resume analysis display
- [ ] Job search
- [ ] AI job recommendations
- [ ] Match percentage
- [ ] Match explanation
- [ ] Apply
- [ ] View applications

## Employer

- [ ] Registration/login
- [ ] Employer profile
- [ ] Create job
- [ ] Edit job
- [ ] View applicants
- [ ] AI candidate match
- [ ] Strong areas
- [ ] Skill gaps
- [ ] Shortlist
- [ ] Reject
- [ ] Generate AI interview questions

---

# 17. Frontend Success Criteria

The frontend is considered ready for the MVP when:

1. An Admin can access the Admin Dashboard and administrative routes.
2. An Admin can manage candidate and employer accounts according to permissions.
3. An Admin can view and moderate jobs according to permissions.
4. An Admin can view platform statistics.
5. A candidate can register and log in.
2. A candidate can create/manage a profile.
3. A candidate can upload a resume.
4. AI resume analysis can be displayed.
5. Jobs can be searched and viewed.
6. AI recommendations and explainable match percentages can be displayed.
7. A candidate can apply and track applications.
8. An employer can create and manage jobs.
9. An employer can view applicants.
10. Match scores, strengths, and gaps are visible.
11. Employers can shortlist or reject candidates.
12. AI interview questions can be generated and displayed.
13. Candidate and employer Next.js routes are separated and protected by role.


---

# 18. Admin Frontend Business Rules

1. Admin is a platform-management role and is separate from Candidate and Employer roles.
2. Admin can manage platform users according to authorized administrative actions.
3. Admin can moderate jobs but should not normally edit employer-authored job content.
4. Admin should not normally shortlist or reject candidates because those are employer hiring decisions.
5. Admin access to candidate information should follow privacy and authorization requirements.
6. Admin actions that change account or job status should require clear confirmation in the UI.
7. Frontend role protection must never replace backend authorization.


---

# 19. Employer Approval and Account Hold Workflow

## 19.1 Admin Account Control

The Admin can control both Candidate and Employer accounts.

Supported account statuses:

```text
PENDING
ACTIVE
ON_HOLD
REJECTED
INACTIVE
```

### Candidate lifecycle

```text
REGISTER
   ↓
ACTIVE
   ↓
ADMIN MAY PLACE ON HOLD
   ↓
ON_HOLD
   ↓
ADMIN RELEASES HOLD
   ↓
ACTIVE
```

Candidate registration does not require Admin approval in the MVP.

An `ON_HOLD` or `INACTIVE` candidate must not be allowed to use protected Candidate features.

Existing profile, resume, applications, and historical data remain preserved.

## 19.2 Employer Registration

Employer registration must collect mandatory company information.

Required fields:

- Recruiter name
- Email
- Password
- Company name
- Company description/information
- Company location
- CIN (Corporate Identification Number)

The registration form must clearly indicate that CIN is mandatory.

## 19.3 Employer Approval Lifecycle

```text
EMPLOYER REGISTERS
        ↓
STATUS = PENDING
        ↓
ADMIN REVIEWS COMPANY + CIN
        ↓
   ┌────┴─────┐
   ↓          ↓
APPROVE      REJECT
   ↓          ↓
ACTIVE     REJECTED
              ↓
        UPDATE + RESUBMIT
              ↓
           PENDING
```

An Employer in `PENDING` status must not create or publish jobs and must not access applicant management or hiring features.

An `ACTIVE` Employer can use the normal Employer features.

An Admin may later place an Employer account `ON_HOLD`.

## 19.4 Employer Pending and Rejected Screens

The frontend must provide dedicated status screens.

### Pending

Display:

- Registration received
- Company approval is pending
- Employer cannot create jobs until approval

### Rejected

Display:

- Registration was not approved
- Rejection reason when provided
- Ability to update company information and resubmit

### On Hold

Display a clear restricted-account message.

Do not reveal internal Admin-only notes.

## 19.5 Admin Employer Approval UI

The Admin employer review screen should display:

- Recruiter name
- Email
- Company name
- Company description
- Company website, when available
- Company location
- CIN
- Current account status
- Registration date

Admin actions:

- Approve
- Reject with reason
- Place on hold
- Release hold

Destructive or status-changing actions must require confirmation.

---

# 20. Frontend Development Rules and Code Quality Standards

These rules are mandatory for implementation.

## 20.1 File Size Rule

No normal production source file should exceed **300 lines**.

If a file approaches 300 lines:

- Extract UI into smaller components.
- Extract business/API logic into hooks or service modules.
- Extract validation schemas.
- Extract constants and types.
- Split large pages into feature components.

The 300-line rule is a maintainability rule, not a reason to artificially split simple related code.

## 20.2 Page Composition Rule

A page should primarily compose feature components.

Example:

```text
app/admin/employers/page.tsx
    ├── EmployerFilters
    ├── EmployerTable
    ├── EmployerPagination
    └── EmployerApprovalDialog
```

Avoid placing:

- Large forms
- API calls
- Complex state machines
- Validation schemas
- Large tables

all inside one `page.tsx`.

## 20.3 Component Size Guideline

Recommended targets:

- Small reusable component: 30–150 lines
- Feature component: normally under 250 lines
- Page/layout: normally under 200 lines
- Absolute preferred maximum for a source file: 300 lines

## 20.4 API Layer Rule

Do not call backend APIs directly throughout page components.

Use domain modules:

```text
lib/api/
├── auth.ts
├── admin.ts
├── candidate.ts
├── employer.ts
├── jobs.ts
└── ai.ts
```

API modules should contain request logic only.

## 20.5 Hook Rule

Complex feature state and mutations should be placed in custom hooks.

Examples:

```text
hooks/
├── use-auth.ts
├── use-admin-employers.ts
├── use-candidate-profile.ts
├── use-jobs.ts
└── use-applications.ts
```

## 20.6 Feature-Based Component Structure

Prefer:

```text
features/
├── admin/
│   ├── components/
│   ├── hooks/
│   └── schemas/
├── candidate/
├── employer/
└── jobs/
```

over placing all components for the entire application in one large folder.

## 20.7 Type Safety

- Avoid `any`.
- Define API request/response types.
- Keep shared domain types in dedicated files.
- Validate external data at boundaries where appropriate.
- Reuse enums/unions for account and application statuses.

## 20.8 Client Component Rule

Use `"use client"` only when browser interactivity is required.

Prefer Server Components for static or server-loaded page composition where appropriate.

## 20.9 UI State Rule

Every data-driven screen should consider:

- Loading
- Success
- Empty
- Error
- Permission/restricted state where applicable

## 20.10 Account Status Routing

After authentication, the frontend must consider both role and account status.

Example:

```text
ADMIN + ACTIVE
→ Admin Dashboard

CANDIDATE + ACTIVE
→ Candidate Dashboard

CANDIDATE + ON_HOLD
→ Account Restricted Screen

EMPLOYER + PENDING
→ Employer Approval Pending Screen

EMPLOYER + REJECTED
→ Employer Rejection / Resubmission Screen

EMPLOYER + ACTIVE
→ Employer Dashboard

EMPLOYER + ON_HOLD
→ Account Restricted Screen
```

Frontend status handling improves user experience but backend authorization remains mandatory.
