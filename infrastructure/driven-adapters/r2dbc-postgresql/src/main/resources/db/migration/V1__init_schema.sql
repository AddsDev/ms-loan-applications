CREATE SCHEMA IF NOT EXISTS ms_applications;

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS citext   WITH SCHEMA public;

SET search_path TO ms_applications, public;
