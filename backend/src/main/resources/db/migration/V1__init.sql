CREATE TABLE users (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(200) NOT NULL,
    role VARCHAR(32) NOT NULL,
    account_status VARCHAR(32) NOT NULL,
    status_reason TEXT,
    hold_reason TEXT,
    status_changed_by VARCHAR(64),
    status_changed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE candidate_profiles (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    phone VARCHAR(40),
    location VARCHAR(200),
    title VARCHAR(200),
    experience VARCHAR(200),
    skills JSONB NOT NULL DEFAULT '[]'::jsonb,
    education JSONB NOT NULL DEFAULT '[]'::jsonb,
    certifications JSONB NOT NULL DEFAULT '[]'::jsonb,
    portfolio_url VARCHAR(500),
    linkedin_url VARCHAR(500)
);

CREATE TABLE resumes (
    id VARCHAR(64) PRIMARY KEY,
    candidate_id VARCHAR(64) NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    file_url TEXT,
    file_type VARCHAR(32),
    file_name VARCHAR(260),
    parsed_text TEXT,
    parsed_data JSONB,
    status VARCHAR(32) NOT NULL,
    error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE companies (
    id VARCHAR(64) PRIMARY KEY,
    employer_id VARCHAR(64) NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    company_name VARCHAR(200) NOT NULL,
    description TEXT,
    website VARCHAR(500),
    location VARCHAR(200),
    cin VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE jobs (
    id VARCHAR(64) PRIMARY KEY,
    employer_id VARCHAR(64) REFERENCES users (id) ON DELETE SET NULL,
    company_name VARCHAR(200) NOT NULL,
    role VARCHAR(200) NOT NULL,
    experience VARCHAR(200) NOT NULL,
    skills JSONB NOT NULL DEFAULT '[]'::jsonb,
    preferred_skills JSONB,
    location VARCHAR(200) NOT NULL,
    salary VARCHAR(120) NOT NULL,
    job_type VARCHAR(80) NOT NULL,
    work_mode VARCHAR(80),
    description TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    posted_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_jobs_status ON jobs (status);
CREATE INDEX idx_jobs_employer ON jobs (employer_id);

CREATE TABLE applications (
    id VARCHAR(64) PRIMARY KEY,
    job_id VARCHAR(64) NOT NULL REFERENCES jobs (id) ON DELETE CASCADE,
    candidate_id VARCHAR(64) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    rejection_reason TEXT,
    applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (job_id, candidate_id)
);

CREATE INDEX idx_applications_candidate ON applications (candidate_id);
CREATE INDEX idx_applications_job ON applications (job_id);

CREATE TABLE ai_match_results (
    id VARCHAR(64) PRIMARY KEY,
    job_id VARCHAR(64) NOT NULL REFERENCES jobs (id) ON DELETE CASCADE,
    candidate_id VARCHAR(64) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    match_score INTEGER NOT NULL,
    strong_areas JSONB NOT NULL DEFAULT '[]'::jsonb,
    gaps JSONB NOT NULL DEFAULT '[]'::jsonb,
    explanation TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE interview_questions (
    id VARCHAR(64) PRIMARY KEY,
    job_id VARCHAR(64) NOT NULL REFERENCES jobs (id) ON DELETE CASCADE,
    candidate_id VARCHAR(64) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    questions JSONB NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_logs (
    id VARCHAR(64) PRIMARY KEY,
    admin_id VARCHAR(64) NOT NULL,
    admin_name VARCHAR(200) NOT NULL,
    action VARCHAR(80) NOT NULL,
    target_type VARCHAR(40) NOT NULL,
    target_id VARCHAR(64) NOT NULL,
    previous_status VARCHAR(32),
    new_status VARCHAR(32),
    reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_created ON audit_logs (created_at DESC);

CREATE TABLE ai_usage (
    id VARCHAR(32) PRIMARY KEY,
    resume_analysis INTEGER NOT NULL DEFAULT 0,
    job_match INTEGER NOT NULL DEFAULT 0,
    recommendations INTEGER NOT NULL DEFAULT 0,
    interview_questions INTEGER NOT NULL DEFAULT 0,
    failures INTEGER NOT NULL DEFAULT 0
);

INSERT INTO ai_usage (id) VALUES ('global');
