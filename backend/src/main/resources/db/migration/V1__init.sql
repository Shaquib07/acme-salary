CREATE TABLE app_users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    UNIQUE KEY uk_app_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE employees (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    employee_number VARCHAR(32) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    department VARCHAR(100) NOT NULL,
    job_title VARCHAR(120) NOT NULL,
    country_code VARCHAR(8) NOT NULL,
    currency_code VARCHAR(8) NOT NULL,
    annual_salary DECIMAL(19, 2) NOT NULL,
    employment_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    hired_on DATE NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    last_editor VARCHAR(255),
    UNIQUE KEY uk_employees_number (employee_number),
    UNIQUE KEY uk_employees_email (email),
    KEY idx_employees_last_name (last_name),
    KEY idx_employees_country_status (country_code, status),
    KEY idx_employees_dept_status (department, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE fx_rates (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    from_currency VARCHAR(8) NOT NULL,
    to_currency VARCHAR(8) NOT NULL,
    rate DECIMAL(19, 6) NOT NULL,
    UNIQUE KEY uk_fx_rates_pair (from_currency, to_currency)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
