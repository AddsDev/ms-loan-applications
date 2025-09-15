SET search_path TO ms_applications, public;

ALTER TABLE ms_applications.applications
    ADD COLUMN advisor_email public.citext NULL;
ALTER TABLE ms_applications.applications
    ADD COLUMN reason TEXT NULL DEFAULT '';