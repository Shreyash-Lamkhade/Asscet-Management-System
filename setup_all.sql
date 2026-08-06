-- ASSCET Event Management System (Oracle 10g) - Full reset + setup
-- Safe to re-run. Run in SQL*Plus as your schema user (e.g., SHREYASH).
-- Usage:
--   sqlplus Shreyash/your_password@XE
--   @setup_all.sql

SET DEFINE OFF;

-- Drop triggers
BEGIN EXECUTE IMMEDIATE 'DROP TRIGGER expense_bir'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TRIGGER feedback_bir'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TRIGGER registrations_bir'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TRIGGER events_bir'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TRIGGER users_bir'; EXCEPTION WHEN OTHERS THEN NULL; END;
/

-- Drop tables (children first)
BEGIN EXECUTE IMMEDIATE 'DROP TABLE expense_items CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE feedback CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE registrations CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE events CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE users CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/

-- Drop sequences
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE expense_seq'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE feedback_seq'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE registrations_seq'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE events_seq'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE users_seq'; EXCEPTION WHEN OTHERS THEN NULL; END;
/

-- Core tables
CREATE TABLE users (
  user_id NUMBER(10) PRIMARY KEY,
  name VARCHAR2(100) NOT NULL,
  email VARCHAR2(120) NOT NULL UNIQUE,
  password VARCHAR2(100) NOT NULL,
  role VARCHAR2(20) NOT NULL
);

CREATE TABLE events (
  event_id NUMBER(10) PRIMARY KEY,
  name VARCHAR2(100) NOT NULL,
  description VARCHAR2(255) NOT NULL,
  event_date DATE NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  venue VARCHAR2(100) NOT NULL,
  category VARCHAR2(50) NOT NULL,
  total_seats NUMBER(10) NOT NULL,
  available_seats NUMBER(10) NOT NULL,
  fee NUMBER(10, 2) NOT NULL,
  total_budget NUMBER(12, 2) NOT NULL,
  expenses NUMBER(12, 2) DEFAULT 0 NOT NULL
);

CREATE TABLE registrations (
  reg_id NUMBER(10) PRIMARY KEY,
  user_id NUMBER(10) NOT NULL,
  event_id NUMBER(10) NOT NULL,
  check_in CHAR(1) DEFAULT 'N' NOT NULL,
  check_in_time DATE,
  CONSTRAINT fk_registration_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_registration_event FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
  CONSTRAINT uk_user_event UNIQUE (user_id, event_id)
);

-- New feature tables
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
);

CREATE TABLE expense_items (
  expense_id NUMBER(10) PRIMARY KEY,
  event_id NUMBER(10) NOT NULL,
  title VARCHAR2(120) NOT NULL,
  amount NUMBER(12, 2) NOT NULL,
  spent_on DATE DEFAULT SYSDATE NOT NULL,
  created_at DATE DEFAULT SYSDATE NOT NULL,
  CONSTRAINT fk_expense_event FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE
);

-- Sequences
CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE events_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE registrations_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE feedback_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE expense_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- Triggers
CREATE OR REPLACE TRIGGER users_bir
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
  IF :NEW.user_id IS NULL THEN
    SELECT users_seq.NEXTVAL INTO :NEW.user_id FROM dual;
  END IF;
END;
/

CREATE OR REPLACE TRIGGER events_bir
BEFORE INSERT ON events
FOR EACH ROW
BEGIN
  IF :NEW.event_id IS NULL THEN
    SELECT events_seq.NEXTVAL INTO :NEW.event_id FROM dual;
  END IF;
END;
/

CREATE OR REPLACE TRIGGER registrations_bir
BEFORE INSERT ON registrations
FOR EACH ROW
BEGIN
  IF :NEW.reg_id IS NULL THEN
    SELECT registrations_seq.NEXTVAL INTO :NEW.reg_id FROM dual;
  END IF;
END;
/

CREATE OR REPLACE TRIGGER feedback_bir
BEFORE INSERT ON feedback
FOR EACH ROW
BEGIN
  IF :NEW.feedback_id IS NULL THEN
    SELECT feedback_seq.NEXTVAL INTO :NEW.feedback_id FROM dual;
  END IF;
END;
/

CREATE OR REPLACE TRIGGER expense_bir
BEFORE INSERT ON expense_items
FOR EACH ROW
BEGIN
  IF :NEW.expense_id IS NULL THEN
    SELECT expense_seq.NEXTVAL INTO :NEW.expense_id FROM dual;
  END IF;
END;
/

-- Demo users (admin + students)
INSERT INTO users (name, email, password, role) VALUES ('Admin User', 'admin@asscet.com', 'admin123', 'Admin');
INSERT INTO users (name, email, password, role) VALUES ('Student User', 'user@asscet.com', 'user123', 'User');
INSERT INTO users (name, email, password, role) VALUES ('Rahul Kumar', 'rahul@asscet.com', 'rahul123', 'User');
INSERT INTO users (name, email, password, role) VALUES ('Priya Sharma', 'priya@asscet.com', 'priya123', 'User');
INSERT INTO users (name, email, password, role) VALUES ('Arjun Mehta', 'arjun@asscet.com', 'arjun123', 'User');

-- Demo events
INSERT INTO events (name, description, event_date, start_date, end_date, venue, category, total_seats, available_seats, fee, total_budget, expenses)
VALUES ('Tech Talk 2026', 'Seminar on modern software development.', TO_DATE('2026-05-10', 'YYYY-MM-DD'), TO_DATE('2026-05-10', 'YYYY-MM-DD'), TO_DATE('2026-05-10', 'YYYY-MM-DD'), 'Main Hall', 'Tech', 150, 150, 100, 25000, 5000);

INSERT INTO events (name, description, event_date, start_date, end_date, venue, category, total_seats, available_seats, fee, total_budget, expenses)
VALUES ('Cultural Fest', 'Music, dance and art competition.', TO_DATE('2026-06-14', 'YYYY-MM-DD'), TO_DATE('2026-06-14', 'YYYY-MM-DD'), TO_DATE('2026-06-18', 'YYYY-MM-DD'), 'Open Auditorium', 'Cultural', 300, 300, 50, 40000, 12000);

INSERT INTO events (name, description, event_date, start_date, end_date, venue, category, total_seats, available_seats, fee, total_budget, expenses)
VALUES ('AI Symposium', 'Talks on AI and ML.', TO_DATE('2026-05-20', 'YYYY-MM-DD'), TO_DATE('2026-05-20', 'YYYY-MM-DD'), TO_DATE('2026-05-22', 'YYYY-MM-DD'), 'Seminar Hall', 'Tech', 200, 200, 150, 50000, 12000);

INSERT INTO events (name, description, event_date, start_date, end_date, venue, category, total_seats, available_seats, fee, total_budget, expenses)
VALUES ('Startup Workshop', 'Entrepreneurship and pitching ideas.', TO_DATE('2026-07-10', 'YYYY-MM-DD'), TO_DATE('2026-07-10', 'YYYY-MM-DD'), TO_DATE('2026-07-10', 'YYYY-MM-DD'), 'Innovation Lab', 'Workshop', 120, 120, 200, 45000, 15000);

INSERT INTO events (name, description, event_date, start_date, end_date, venue, category, total_seats, available_seats, fee, total_budget, expenses)
VALUES ('Sports Meet', 'Annual indoor and outdoor sports.', TO_DATE('2026-06-05', 'YYYY-MM-DD'), TO_DATE('2026-06-05', 'YYYY-MM-DD'), TO_DATE('2026-06-05', 'YYYY-MM-DD'), 'College Ground', 'Sports', 250, 250, 75, 35000, 10000);

-- Demo registrations (using email + event name to avoid ID guessing)
INSERT INTO registrations (user_id, event_id)
SELECT u.user_id, e.event_id FROM users u, events e
WHERE u.email = 'rahul@asscet.com' AND e.name = 'AI Symposium';

INSERT INTO registrations (user_id, event_id)
SELECT u.user_id, e.event_id FROM users u, events e
WHERE u.email = 'priya@asscet.com' AND e.name = 'AI Symposium';

INSERT INTO registrations (user_id, event_id)
SELECT u.user_id, e.event_id FROM users u, events e
WHERE u.email = 'arjun@asscet.com' AND e.name = 'Sports Meet';

-- Demo attendance
UPDATE registrations r
SET check_in = 'Y', check_in_time = SYSDATE
WHERE r.user_id = (SELECT user_id FROM users WHERE email='rahul@asscet.com')
  AND r.event_id = (SELECT event_id FROM events WHERE name='AI Symposium');

-- Demo feedback
INSERT INTO feedback (user_id, event_id, rating, feedback_text)
SELECT u.user_id, e.event_id, 5, 'Excellent event'
FROM users u, events e
WHERE u.email='rahul@asscet.com' AND e.name='AI Symposium';

-- Demo expenses
INSERT INTO expense_items (event_id, title, amount, spent_on)
SELECT e.event_id, 'Snacks', 1200, SYSDATE FROM events e WHERE e.name='AI Symposium';

INSERT INTO expense_items (event_id, title, amount, spent_on)
SELECT e.event_id, 'Poster Printing', 800, SYSDATE FROM events e WHERE e.name='AI Symposium';

COMMIT;
