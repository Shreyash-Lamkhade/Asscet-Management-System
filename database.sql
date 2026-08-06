BEGIN EXECUTE IMMEDIATE 'DROP TABLE registrations CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE events CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE users CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE users_seq'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE events_seq'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE registrations_seq'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TRIGGER users_bir'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TRIGGER events_bir'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TRIGGER registrations_bir'; EXCEPTION WHEN OTHERS THEN NULL; END;
/

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
    CONSTRAINT fk_registration_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_registration_event FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    CONSTRAINT uk_user_event UNIQUE (user_id, event_id)
);

CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE events_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE registrations_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

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

INSERT INTO users (name, email, password, role)
VALUES ('Admin User', 'admin@asscet.com', 'admin123', 'Admin');

INSERT INTO users (name, email, password, role)
VALUES ('Student User', 'user@asscet.com', 'user123', 'User');

INSERT INTO events (name, description, event_date, venue, category, total_seats, available_seats, fee, total_budget, expenses)
VALUES ('Tech Talk 2026', 'Seminar on modern software development.', TO_DATE('2026-05-10', 'YYYY-MM-DD'), 'Main Hall', 'Tech', 150, 150, 100.00, 25000.00, 5000.00);

INSERT INTO events (name, description, event_date, venue, category, total_seats, available_seats, fee, total_budget, expenses)
VALUES ('Cultural Fest', 'Music, dance and art competition.', TO_DATE('2026-06-14', 'YYYY-MM-DD'), 'Open Auditorium', 'Cultural', 300, 300, 50.00, 40000.00, 12000.00);

COMMIT;
