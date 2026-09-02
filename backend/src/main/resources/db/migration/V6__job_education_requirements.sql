ALTER TABLE jobs ADD COLUMN education_requirements JSONB NOT NULL DEFAULT '[]';
ALTER TABLE jobs ADD COLUMN certification_requirements JSONB NOT NULL DEFAULT '[]';
