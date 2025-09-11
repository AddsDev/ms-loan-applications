SET search_path TO ms_applications, public;

CREATE TABLE ms_applications.statuses
(
    status_id    UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    name         TEXT        NOT NULL UNIQUE, -- PENDING, APPROVED, REJECTED
    description  TEXT        NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    lock_version BIGINT      NOT NULL DEFAULT 0
);

CREATE TABLE ms_applications.loan_types
(
    loan_type_id         UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    code                 VARCHAR(40)    NOT NULL UNIQUE,                                  -- CONSUMER, BUSINESS, MORTGAGE
    name                 TEXT           NOT NULL UNIQUE,
    min_amount           NUMERIC(14, 2) NOT NULL CHECK (min_amount >= 0),
    max_amount           NUMERIC(14, 2) NOT NULL CHECK (max_amount >= 0),
    min_term             INTEGER        NOT NULL CHECK (min_term >= 0),
    max_term             INTEGER        NOT NULL CHECK (max_term >= 0),
    CHECK (max_amount >= min_amount),
    interest_rate        NUMERIC(5, 2)  NOT NULL CHECK (interest_rate BETWEEN 0 AND 100), -- % annual
    automatic_validation BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    lock_version         BIGINT         NOT NULL DEFAULT 0
);

CREATE TABLE ms_applications.applications
(
    application_id    UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    amount            NUMERIC(14, 2) NOT NULL CHECK (amount > 0),
    term              INTEGER        NOT NULL CHECK (term > 0), -- in months
    email             public.citext           NOT NULL,
    identity_document TEXT           NULL,
    status_id         UUID           NOT NULL,
    loan_type_id      UUID           NOT NULL,
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    lock_version      BIGINT         NOT NULL DEFAULT 0,

    CONSTRAINT fk_application_status
        FOREIGN KEY (status_id) REFERENCES ms_applications.statuses (status_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_application_loan_type
        FOREIGN KEY (loan_type_id) REFERENCES ms_applications.loan_types (loan_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT ck_email_format CHECK (email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$')
);

-- Indexes
CREATE INDEX IF NOT EXISTS ix_application_status ON ms_applications.applications (status_id);
CREATE INDEX IF NOT EXISTS ix_application_loan_type ON ms_applications.applications (loan_type_id);
CREATE INDEX IF NOT EXISTS ix_application_email ON ms_applications.applications (email);

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

CREATE TRIGGER tg_touch_statuses
    BEFORE UPDATE
    ON ms_applications.statuses
    FOR EACH ROW
EXECUTE FUNCTION ms_applications.touch_row();

CREATE TRIGGER tg_touch_loan_types
    BEFORE UPDATE
    ON ms_applications.loan_types
    FOR EACH ROW
EXECUTE FUNCTION ms_applications.touch_row();

CREATE TRIGGER tg_touch_applications
    BEFORE UPDATE
    ON ms_applications.applications
    FOR EACH ROW
EXECUTE FUNCTION ms_applications.touch_row();

CREATE OR REPLACE FUNCTION ms_applications.validate_application_amount()
    RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
DECLARE
    min_val NUMERIC(14, 2);
    max_val NUMERIC(14, 2);
BEGIN
    SELECT min_amount, max_amount
    INTO min_val, max_val
    FROM ms_applications.loan_types
    WHERE loan_type_id = NEW.loan_type_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'loan_type % does not exist', NEW.loan_type_id;
    END IF;

    IF NEW.amount < min_val OR NEW.amount > max_val THEN
        RAISE EXCEPTION 'amount % is out of the range [% - %] for loan_type %',
            NEW.amount, min_val, max_val, NEW.loan_type_id
            USING ERRCODE = 'check_violation';
    END IF;

    RETURN NEW;
END
$$;

CREATE TRIGGER tg_validate_amount
    BEFORE INSERT OR UPDATE OF amount, loan_type_id
    ON ms_applications.applications
    FOR EACH ROW
EXECUTE FUNCTION ms_applications.validate_application_amount();