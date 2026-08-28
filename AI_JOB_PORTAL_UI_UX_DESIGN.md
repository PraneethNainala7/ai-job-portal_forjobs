# AI Job Portal — UI/UX & Visual Design Specification

**Document Type:** Frontend UI/UX Design Specification  
**Frontend:** Next.js  
**Applies To:** Public Website, Candidate Portal, Employer Portal, Admin Portal

---

# 1. Purpose

This document defines the visual style, colors, typography, spacing, components, layouts, interactions, responsive behavior, and page UX standards for the entire AI Job Portal.

The design must support the complete project journey:

```text
Public Visitor
    ↓
Authentication
    ↓
Role + Account Status
    ↓
Candidate / Employer / Admin Experience
```

AI features are presented as decision-support information and must not imply that AI makes the final hiring decision.

---

# 2. Design Vision

The product shall feel:

- Professional
- Modern
- Premium
- Clean
- Trustworthy
- Intelligent
- Enterprise-ready
- Accessible

The style is a **modern recruitment SaaS product with a restrained AI visual identity**.

Avoid:

- Excessive gradients
- Neon colors
- Heavy shadows
- Oversized rounded elements
- Constant animation
- Dashboard clutter

Visual hierarchy:

```text
PRIMARY CONTENT
       ↓
PRIMARY ACTION
       ↓
AI INSIGHT
       ↓
SECONDARY INFORMATION
```

---

# 3. Core Color System

## Brand Foundation

| Token | Color | Usage |
|---|---|---|
| Brand Foundation | `#0B1220` | Sidebar, dark navigation |
| Primary | `#4F46E5` | Primary actions, active states |
| Primary Hover | `#4338CA` | Hover state |
| Primary Light | `#EEF2FF` | Selected/light surfaces |
| AI Accent | `#7C3AED` | AI actions and AI identity |
| AI Blue | `#2563EB` | Supporting AI/data emphasis |

## Neutral Colors

| Token | Color |
|---|---|
| Application Background | `#F8FAFC` |
| Surface | `#FFFFFF` |
| Muted Surface | `#F1F5F9` |
| Primary Text | `#0F172A` |
| Secondary Text | `#64748B` |
| Muted Text | `#94A3B8` |
| Border | `#E2E8F0` |
| Input Border | `#CBD5E1` |

**Rule:** Use `#F8FAFC` as the main application background. White is primarily reserved for cards, forms, tables, and modals.

## AI Colors

| Token | Color |
|---|---|
| AI Accent | `#7C3AED` |
| AI Surface | `#F5F3FF` |
| AI Border | `#DDD6FE` |

## Status Colors

| Status | Background | Text |
|---|---|---|
| Active | `#DCFCE7` | `#166534` |
| Pending | `#FEF3C7` | `#92400E` |
| Rejected | `#FEE2E2` | `#991B1B` |
| On Hold | `#FFEDD5` | `#9A3412` |
| Inactive | `#F1F5F9` | `#475569` |
| Information | `#DBEAFE` | `#1D4ED8` |

Every status must contain text; color alone is insufficient.

---

# 4. Role Accent Rules

The application is one product:

```text
ONE BRAND
+
ONE DESIGN SYSTEM
+
ROLE-BASED NAVIGATION
+
SUBTLE ROLE ACCENTS
```

- Candidate: Indigo-led emphasis
- Employer: Blue-led emphasis where useful
- Admin: Navy/slate emphasis

Typography, spacing, components, and interaction rules remain consistent.

---

# 5. Typography

## Primary Font

**Geist** (variable, loaded via `next/font/google`)

Fallback:

```text
Geist, system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", sans-serif
```

## Numeric / Identifier Font

**Geist Mono** — used only for identifiers and standalone figures (CIN, match score
readouts). Never for body copy or labels.

| Element | Size | Weight |
|---|---:|---:|
| Page Title | 32px | 700 |
| Large Heading | 24px | 700 |
| Section Heading | 20px | 600 |
| Card Heading | 16px | 600 |
| Body Large | 16px | 400 |
| Body | 14px | 400 |
| Label | 14px | 500 |
| Small Text | 12px | 400 |

Use a limited, consistent type scale.

## Optical Tracking

Letter-spacing tightens as type scales up. Applied globally in `globals.css`, so
headings should not carry their own `tracking-*` utility unless overriding on purpose.

| Element | Tracking |
|---|---:|
| `h1` | -0.033em |
| `h2` | -0.026em |
| `h3`–`h5` | -0.019em |
| Body | 0 |
| Uppercase eyebrow | +0.12em to +0.14em |

## Numerals

Tables, `<time>`, and any element marked `data-numeric` render with `tabular-nums` so
figures align vertically between rows.

---

# 6. Spacing and Layout

## Spacing Scale

```text
4px
8px
12px
16px
20px
24px
32px
40px
48px
64px
```

Recommended:

- Small internal gap: 8px
- Form field gap: 20–24px
- Card padding: 24px
- Section gap: 32px
- Major section gap: 48px

## Desktop Application Shell

- Sidebar: 260px
- Header: 72px
- Page padding: 32px
- Background: `#F8FAFC`

```text
┌──────────────┬──────────────────────────────────────┐
│              │ HEADER                               │
│   SIDEBAR    ├──────────────────────────────────────┤
│              │ Breadcrumb                           │
│              │ Page Title                           │
│              │ Description                          │
│              │ Main Content                         │
└──────────────┴──────────────────────────────────────┘
```

## Tablet

- Page padding: 24px
- Collapsible sidebar
- Prefer 1–2 column layouts

## Mobile

- Page padding: 16px
- Sidebar becomes drawer
- Cards stack vertically
- Forms stack vertically

---

# 7. Global Components

## Sidebar

```text
Background: #0B1220
Width: 260px
```

Navigation item:

- Height: 44px
- Padding: 12px 14px
- Border radius: 8px
- Icon/label gap: 12px

Inactive:

```text
Text: #94A3B8
```

Hover:

```text
Background: rgba(255,255,255,0.06)
Text: #FFFFFF
```

Active:

```text
Background: #4F46E5
Text: #FFFFFF
```

## Header

```text
Height: 72px
Background: #FFFFFF
Bottom Border: #E2E8F0
```

Contains where relevant:

- Mobile menu
- Breadcrumb area
- Notifications
- User profile menu

## Breadcrumbs

Example:

```text
Admin / Employer Approvals / Company Details
```

- Font size: 13px
- Parent: `#64748B`
- Current page: `#0F172A`

## Cards

```text
Background: #FFFFFF
Border: 1px solid #E2E8F0
Border Radius: 16px
Padding: 24px
```

Use subtle elevation only when hierarchy requires it.

## Buttons

### Primary

```text
Background: #4F46E5
Text: #FFFFFF
Hover: #4338CA
Height: 40px or 44px
Radius: 8px
```

### Secondary

```text
Background: #FFFFFF
Border: #E2E8F0
Text: #0F172A
```

### AI Action

```text
Background: #7C3AED
Text: #FFFFFF
```

### Danger

```text
Background: #DC2626
Text: #FFFFFF
```

Destructive actions require confirmation.

## Forms

- Input height: 44px
- Radius: 8px
- Border: `#CBD5E1`
- Persistent labels
- Required fields clearly marked
- Error message below field

Focus:

```text
Border: #4F46E5
Focus ring: light indigo
```

## Tables

```text
Header Background: #F8FAFC
Border: #E2E8F0
Row Height: 56–64px
Row Hover: #F8FAFC
```

Support where relevant:

- Search
- Filters
- Sorting
- Pagination
- Loading
- Empty state
- Error state

## Modals

- Dim background
- Clear primary and cancel actions
- Keyboard accessible
- Focus contained inside modal

## Toasts

Use for routine async feedback:

- Changes saved
- Employer approved
- Resume upload failed

Do not use toasts as the only confirmation for critical state changes.

---

# 8. AI Component Style

AI sections use:

```text
Background: #F5F3FF
Border: 1px solid #DDD6FE
Border Radius: 16px
```

Example:

```text
✦ AI MATCH ANALYSIS

Match Score
87%

Strengths
✓ C#
✓ .NET
✓ SQL

Skill Gaps
○ Azure
○ Docker
```

AI results must be labeled:

- AI Insight
- AI Recommendation
- AI Match Analysis
- AI Generated Questions

---

# 9. Information Architecture

## Public

```text
Home
├── Browse Jobs
│   └── Job Details
├── Login
└── Register
```

## Candidate

```text
Dashboard
Find Jobs
AI Recommendations
My Profile
Resume
My Applications
```

## Employer

### Pending

```text
Approval Status
Company Profile
Logout
```

### Rejected

```text
Approval Status
Update Company
Resubmit
Logout
```

### Active

```text
Dashboard
Manage Jobs
Applicants
Company Profile
```

## Admin

```text
Dashboard
Employer Approvals
Candidates
Employers
Jobs
Applications
Audit Activity
```

---

# 10. Account Status UX

Routing evaluates:

```text
ROLE
+
ACCOUNT STATUS
```

Example:

```text
EMPLOYER + PENDING  → Approval Pending
EMPLOYER + REJECTED → Update + Resubmit
EMPLOYER + ACTIVE   → Employer Dashboard
EMPLOYER + ON_HOLD  → Restricted Account Screen
```

Do not show restricted users a normal dashboard full of disabled controls.

---

# 11. Public Pages

## Home

Sections:

1. Header/navigation
2. Hero
3. Job search entry
4. AI value proposition
5. Candidate and employer pathways
6. Featured/recent jobs if supported
7. Footer

Primary actions:

- Find Jobs
- Create Candidate Account
- Create Employer Account

Public pages may use subtle indigo/blue decorative elements, but dashboards remain clean and practical.

## Browse Jobs

Sections:

- Page heading
- Search
- Filters
- Results
- Job list
- Pagination

Job cards prioritize:

- Job title
- Company
- Location
- Job type/work mode
- Skills summary
- View Details

## Job Details

Structure:

1. Breadcrumb
2. Job header
3. Key metadata
4. Candidate match summary when available
5. Description
6. Required skills
7. Preferred skills
8. Experience requirements
9. Primary action

---

# 12. Authentication

## Login

Elements:

- Brand
- Email
- Password
- Forgot password
- Login
- Registration entry point

## Account Type Selection

Present two clear choices:

```text
Find a Job
Create Candidate Account

Hire Talent
Create Employer Account
```

## Candidate Registration

Collect required account information. Complete profile enrichment after account entry.

## Employer Registration

Collect recruiter and company information.

**CIN is mandatory.**

Flow:

```text
Registration Submitted
        ↓
Employer Status = Pending
        ↓
Admin Review
```

---

# 13. Candidate Experience

## Candidate Dashboard

Goal:

> What should I do next?

Sections:

- Welcome
- Profile completion
- Resume status
- AI recommendations
- Recent applications
- Quick actions

## Profile

Grouped sections:

- Personal information
- Professional information
- Skills
- Education
- Job preferences where supported

## Resume

Sections:

- Current resume
- Upload/replace
- Processing status
- AI resume analysis

## AI Job Recommendations

Each recommendation shows:

- Job
- Company
- Match percentage where available
- Explanation entry
- View Job

## Match Analysis

Structure:

```text
Overall Match
Strengths
Skill Gaps
Experience Alignment
Explanation
```

Primary action:

```text
Apply for Job
```

## Application Flow

```text
Job Details
    ↓
Review Application
    ↓
Confirm Application
    ↓
Success
    ↓
My Applications
```

## My Applications

Show:

- Application status
- Job
- Company
- Applied date
- View details

---

# 14. Employer Experience

## Pending Approval

Dedicated restricted experience:

```text
EMPLOYER APPROVAL PENDING

Your company registration is being reviewed.

Status: Pending Approval

Under review:
✓ Company Information
✓ Company Location
✓ CIN
```

Do not display the full employer dashboard with disabled features.

## Rejected / Resubmission

Show:

- Registration requires attention
- Rejection reason
- Editable company information
- Resubmit action

```text
Rejected
    ↓
Update Information
    ↓
Resubmit
    ↓
Pending Review
```

## Employer Dashboard

Goal:

> What is happening with my hiring?

Sections:

- Active jobs
- Application volume
- Shortlisted candidates
- Recent activity
- Quick actions

## Manage Jobs

Toolbar:

- Search
- Filters
- Create Job

Possible tabs:

```text
All
Active
Draft (if supported)
Closed
```

## Create/Edit Job

Grouped sections:

1. Basic information
2. Requirements
3. Skills
4. Location/work details
5. Compensation where supported
6. Description
7. Save/publish actions

## Applicants

```text
Job
 ↓
Applicants
 ↓
Search / Filter / Sort
 ↓
Candidate List
 ↓
Candidate Details
```

## Candidate Review

Information hierarchy:

```text
Candidate
    ↓
Resume / Experience
    ↓
AI Match Analysis
    ↓
Employer Decision
```

Actions:

- Shortlist
- Reject
- Generate AI Interview Questions

## AI Interview Questions

Show categories such as:

- Technical
- Resume-based
- Job-specific
- Behavioral

Show loading feedback during generation.

---

# 15. Admin Experience

## Admin Dashboard

Prioritize actions requiring attention.

Most prominent action:

```text
Pending Employer Approvals
```

Other sections may include:

- Candidate overview
- Employer overview
- Jobs
- Applications
- Recent activity

## Employer Approvals

List supports:

- Search
- Filters
- Employer/company identity
- Status
- Submitted date
- Review action

## Employer Review

Structure:

```text
Breadcrumb

Employer Review

Recruiter Information

Company Information

CIN

Status

Actions:
Approve
Reject
Put Account On Hold
```

Reject must collect a reason when required by the workflow.

## Candidate Management

Support:

- Search
- Filters
- Candidate details
- Account status
- Hold/release actions

## Employer Management

Support:

- Search
- Filters
- Employer details
- Account status
- Hold/release actions

## Audit Activity

Use a readable table for:

- Actor
- Action
- Target
- Timestamp
- Context

---

# 16. Confirmation Flows

Use confirmation dialogs for:

- Rejecting employer registration
- Holding candidate accounts
- Holding employer accounts
- Releasing held accounts when confirmation is needed
- Deleting records
- Leaving unsaved changes

Example:

```text
Put Employer Account On Hold?

The employer will have restricted access.
Existing data will be preserved.

[Cancel] [Put on Hold]
```

---

# 17. Loading, Empty, Error, and Success States

Every data-driven page must define these states.

## Loading

- Skeleton cards
- Skeleton table rows
- Inline loading for small actions
- Progress feedback for longer AI operations

## Empty

Explain:

- What is empty
- Why it may be empty
- What to do next

Example:

```text
No applications yet

Applications will appear here after you apply for jobs.

[Find Jobs]
```

## Error

- Plain-language explanation
- Retry action when appropriate
- No raw technical errors

## Success

- Toast for routine actions
- Confirmation page/result for important workflows

---

# 18. Responsive Design

## Desktop — 1280px+

- Full sidebar
- Multi-column layouts
- Full tables

## Tablet — 768px–1279px

- Collapsible navigation
- Reduced spacing
- 1–2 column layouts

## Mobile — below 768px

- Drawer navigation
- One-column content
- Stacked forms
- Compact header
- Responsive tables

---

# 19. Accessibility

The frontend must support:

- Keyboard navigation
- Visible focus indicators
- Sufficient contrast
- Semantic labels
- Status text in addition to color
- Accessible modal focus management
- Field-associated error messages
- Touch-friendly mobile controls

Do not use placeholder text as the only form label.

---

# 20. Animation Rules

Recommended duration:

```text
150ms–250ms
```

Use animation for:

- Sidebar
- Dropdowns
- Modals
- Toasts
- Accordions
- Subtle card hover
- Loading transitions

Avoid:

- Constant motion
- Bouncing controls
- Heavy page transitions
- Continuous AI glow
- Animation on every interaction

Motion must improve feedback or orientation.

---

# 21. Next.js Implementation Rules

Use reusable components and feature-based organization:

```text
Page
 ↓
Page Sections
 ↓
Reusable Components
 ↓
Hooks / API / Schemas
```

Recommended:

```text
features/
├── candidate/
│   ├── components/
│   ├── hooks/
│   ├── schemas/
│   └── api/
├── employer/
│   ├── components/
│   ├── hooks/
│   ├── schemas/
│   └── api/
├── admin/
│   ├── components/
│   ├── hooks/
│   ├── schemas/
│   └── api/
└── shared/
    ├── ui/
    ├── layout/
    └── feedback/
```

Normal production source files should remain approximately **300 lines or less**. Split large pages and components by responsibility.

---

# 22. Final Design Rules

1. One unified design system across the platform.
2. Deep Navy + Indigo is the primary visual identity.
3. Purple is reserved primarily for AI features.
4. Light application background and white surfaces.
5. Information hierarchy before decoration.
6. AI supports decisions; AI does not visually make final hiring decisions.
7. Account status controls the experience and navigation.
8. Every important data page supports loading, empty, error, and success states.
9. Motion is subtle and purposeful.
10. Responsive behavior is built into components.
11. Components and pages remain modular and readable.

---

# 23. Final Visual Identity

```text
AI JOB PORTAL

Deep Navy
    +
Indigo
    +
Clean White
    +
Subtle Purple AI Accent

Professional
Modern
Premium
Intelligent
Trustworthy
Enterprise-ready
```

This document is the visual baseline for every frontend page and component.
