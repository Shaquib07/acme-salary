package com.acme.salary.employee;

import com.acme.salary.catalog.CountryCurrency;
import com.acme.salary.web.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final List<String> SORTABLE = List.of(
            "lastName", "firstName", "email", "department", "countryCode", "annualSalary", "hiredOn", "employeeNumber");

    private final EmployeeRepository employees;

    public EmployeeService(EmployeeRepository employees) {
        this.employees = employees;
    }

    @Transactional(readOnly = true)
    public Page<Employee> search(String q, String country, String department, EmployeeStatus status, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), parseSort(sort));
        return employees.findAll(EmployeeSpecs.filter(q, country, department, status), pageable);
    }

    @Transactional(readOnly = true)
    public Employee get(Long id) {
        return employees.findById(id).orElseThrow(() -> new NotFoundException("Employee not found"));
    }

    @Transactional
    public Employee create(EmployeeDtos.CreateEmployeeRequest request, String editor) {
        CountryCurrency.requireMatchingCurrency(request.countryCode(), request.currencyCode());
        requirePositive(request.annualSalary());
        if (employees.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }
        Employee employee = new Employee();
        employee.setEmployeeNumber(nextNumber());
        employee.setFirstName(request.firstName().trim());
        employee.setLastName(request.lastName().trim());
        employee.setEmail(request.email().trim().toLowerCase());
        employee.setDepartment(request.department().trim());
        employee.setJobTitle(request.jobTitle().trim());
        employee.setCountryCode(request.countryCode().trim().toUpperCase());
        employee.setCurrencyCode(request.currencyCode().trim().toUpperCase());
        employee.setAnnualSalary(request.annualSalary());
        employee.setEmploymentType(request.employmentType());
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setHiredOn(request.hiredOn());
        employee.setLastEditor(editor);
        return employees.save(employee);
    }

    @Transactional
    public Employee update(Long id, EmployeeDtos.UpdateEmployeeRequest request, String editor, boolean admin) {
        Employee employee = get(id);
        if (request.firstName() != null) {
            employee.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null) {
            employee.setLastName(request.lastName().trim());
        }
        if (request.department() != null) {
            employee.setDepartment(request.department().trim());
        }
        if (request.jobTitle() != null) {
            employee.setJobTitle(request.jobTitle().trim());
        }
        if (request.employmentType() != null) {
            employee.setEmploymentType(request.employmentType());
        }
        if (request.countryCode() != null) {
            employee.setCountryCode(request.countryCode().trim().toUpperCase());
        }
        if (request.currencyCode() != null) {
            employee.setCurrencyCode(request.currencyCode().trim().toUpperCase());
        }
        CountryCurrency.requireMatchingCurrency(employee.getCountryCode(), employee.getCurrencyCode());
        if (request.status() != null) {
            if (request.status() == EmployeeStatus.INACTIVE && !admin) {
                throw new org.springframework.security.access.AccessDeniedException("Only HR_ADMIN can deactivate employees");
            }
            employee.setStatus(request.status());
        }
        employee.setLastEditor(editor);
        return employees.save(employee);
    }

    @Transactional
    public Employee patchSalary(Long id, EmployeeDtos.SalaryPatchRequest request, String editor) {
        Employee employee = get(id);
        requirePositive(request.annualSalary());
        if (request.currencyCode() != null && !request.currencyCode().isBlank()) {
            employee.setCurrencyCode(request.currencyCode().trim().toUpperCase());
        }
        CountryCurrency.requireMatchingCurrency(employee.getCountryCode(), employee.getCurrencyCode());
        employee.setAnnualSalary(request.annualSalary());
        employee.setLastEditor(editor);
        return employees.save(employee);
    }

    @Transactional(readOnly = true)
    public List<Employee> listForExport(String q, String country, String department, EmployeeStatus status) {
        return employees.findAll(EmployeeSpecs.filter(q, country, department, status), Sort.by("lastName", "firstName"));
    }

    private static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Salary must be greater than 0");
        }
    }

    private String nextNumber() {
        long next = employees.maxId() + 1;
        return "EMP-%05d".formatted(next);
    }

    private static int clampSize(int size) {
        if (size <= 0) {
            return 20;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by("lastName").ascending();
        }
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        if (!SORTABLE.contains(field)) {
            field = "lastName";
        }
        boolean desc = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim());
        return desc ? Sort.by(field).descending() : Sort.by(field).ascending();
    }
}
