SET search_path TO ms_applications;

CREATE ROLE ms_applications_rw LOGIN PASSWORD '***' NOSUPERUSER NOCREATEDB NOCREATEROLE;
GRANT USAGE ON SCHEMA ms_applications TO ms_applications_rw;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA ms_applications TO ms_applications_rw;
ALTER DEFAULT PRIVILEGES IN SCHEMA ms_applications
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO ms_applications_rw;