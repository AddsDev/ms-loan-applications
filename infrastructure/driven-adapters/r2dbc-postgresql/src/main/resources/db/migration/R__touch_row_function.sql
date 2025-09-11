SET search_path TO ms_applications, public;

CREATE OR REPLACE FUNCTION ms_applications.touch_row()
    RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
BEGIN
    NEW.updated_at := now();
    NEW.lock_version := COALESCE(OLD.lock_version, 0) + 1;
    RETURN NEW;
END
$$;