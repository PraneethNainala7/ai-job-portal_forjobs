ALTER TABLE applications
    ADD COLUMN resume_file_url TEXT,
    ADD COLUMN resume_file_name VARCHAR(260),
    ADD COLUMN resume_file_type VARCHAR(32),
    ADD COLUMN resume_parsed_data JSONB,
    ADD COLUMN match_score INTEGER,
    ADD COLUMN match_strong_areas JSONB,
    ADD COLUMN match_gaps JSONB,
    ADD COLUMN match_explanation TEXT;
