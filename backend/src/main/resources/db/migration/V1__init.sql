CREATE TABLE app_users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE employees (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_number VARCHAR(32) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    job_title VARCHAR(120) NOT NULL,
    country_code VARCHAR(8) NOT NULL,
    currency_code VARCHAR(8) NOT NULL,
    annual_salary NUMERIC(19, 2) NOT NULL,
    employment_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    hired_on DATE NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    last_editor VARCHAR(255)
);

CREATE INDEX idx_employees_email ON employees (email);
CREATE INDEX idx_employees_last_name ON employees (last_name);
CREATE INDEX idx_employees_country_status ON employees (country_code, status);
CREATE INDEX idx_employees_dept_status ON employees (department, status);

CREATE TABLE fx_rates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    from_currency VARCHAR(8) NOT NULL,
    to_currency VARCHAR(8) NOT NULL,
    rate NUMERIC(19, 6) NOT NULL,
    UNIQUE (from_currency, to_currency)
);
