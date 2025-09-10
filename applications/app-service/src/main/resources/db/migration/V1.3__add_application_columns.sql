SET search_path TO ms_applications;

ALTER TABLE ms_applications.applications
    ADD COLUMN name TEXT NOT NULL DEFAULT '';
ALTER TABLE ms_applications.applications
    ADD COLUMN base_salary NUMERIC(14, 2) NOT NULL DEFAULT 0;