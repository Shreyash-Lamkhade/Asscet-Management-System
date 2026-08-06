-- Oracle 10g XE upgrade script for new features:
-- (2) Payment status, (3) Attendance check-in, (4) Feedback ratings, (5) Expense items
-- Safe to re-run: ignores "already exists" errors where possible.

-- 1) Add new columns to REGISTRATIONS (ignore if they already exist)
BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE registrations ADD (check_in CHAR(1) DEFAULT ''N'' NOT NULL)';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -01430 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE registrations ADD (check_in_time DATE)';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -01430 THEN RAISE; END IF;
END;
/

-- 2) FEEDBACK table + sequence + trigger
BEGIN
  EXECUTE IMMEDIATE '
    CREATE TABLE feedback (
      feedback_id NUMBER(10) PRIMARY KEY,
      user_id NUMBER(10) NOT NULL,
      event_id NUMBER(10) NOT NULL,
      rating NUMBER(2) NOT NULL,
      feedback_text VARCHAR2(400),
      created_at DATE DEFAULT SYSDATE NOT NULL,
      CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
      CONSTRAINT fk_feedback_event FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
      CONSTRAINT uk_feedback_user_event UNIQUE (user_id, event_id)
    )';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -955 THEN RAISE; END IF; -- name already used
END;
/

BEGIN
  EXECUTE IMMEDIATE 'CREATE SEQUENCE feedback_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -955 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE '
    CREATE OR REPLACE TRIGGER feedback_bir
    BEFORE INSERT ON feedback
    FOR EACH ROW
    BEGIN
      IF :NEW.feedback_id IS NULL THEN
        SELECT feedback_seq.NEXTVAL INTO :NEW.feedback_id FROM dual;
      END IF;
    END;';
END;
/

-- 3) EXPENSE_ITEMS table + sequence + trigger
BEGIN
  EXECUTE IMMEDIATE '
    CREATE TABLE expense_items (
      expense_id NUMBER(10) PRIMARY KEY,
      event_id NUMBER(10) NOT NULL,
      title VARCHAR2(120) NOT NULL,
      amount NUMBER(12,2) NOT NULL,
      spent_on DATE DEFAULT SYSDATE NOT NULL,
      created_at DATE DEFAULT SYSDATE NOT NULL,
      CONSTRAINT fk_expense_event FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE
    )';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -955 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'CREATE SEQUENCE expense_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE';
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -955 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE '
    CREATE OR REPLACE TRIGGER expense_bir
    BEFORE INSERT ON expense_items
    FOR EACH ROW
    BEGIN
      IF :NEW.expense_id IS NULL THEN
        SELECT expense_seq.NEXTVAL INTO :NEW.expense_id FROM dual;
      END IF;
    END;';
END;
/

COMMIT;
