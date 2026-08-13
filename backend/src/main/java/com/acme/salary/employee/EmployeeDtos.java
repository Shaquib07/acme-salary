package com.acme.salary.employee;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class EmployeeDtos {

    public record EmployeeResponse(
            Long id,
            String employeeNumber,
            String firstName,
            String lastName,
            String email,
            String department,
            String jobTitle,
            String countryCode,
            String currencyCode,
            BigDecimal annualSalary,
            EmploymentType employmentType,
            EmployeeStatus status,
            LocalDate hiredOn,
            Instant updatedAt,
            String lastEditor) {
        public static EmployeeResponse from(Employee e) {
            return new EmployeeResponse(
                    e.getId(),
                    e.getEmployeeNumber(),
                    e.getFirstName(),
                    e.getLastName(),
                    e.getEmail(),
                    e.getDepartment(),
                    e.getJobTitle(),
                    e.getCountryCode(),
                    e.getCurrencyCode(),
                    e.getAnnualSalary(),
                    e.getEmploymentType(),
                    e.getStatus(),
                    e.getHiredOn(),
                    e.getUpdatedAt(),
                    e.getLastEditor());
        }
    }

    public record CreateEmployeeRequest(
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            @NotBlank @Email String email,
            @NotBlank String department,
            @NotBlank String jobTitle,
            @NotBlank String countryCode,
            @NotBlank String currencyCode,
            @NotNull @DecimalMin(value = "0.01", message = "Salary must be greater than 0") BigDecimal annualSalary,
            @NotNull EmploymentType employmentType,
            @NotNull LocalDate hiredOn) {
    }

    public record UpdateEmployeeRequest(
            @Size(max = 100) String firstName,
            @Size(max = 100) String lastName,
            String department,
            String jobTitle,
            String countryCode,
            String currencyCode,
            EmploymentType employmentType,
            EmployeeStatus status) {
    }

    public record SalaryPatchRequest(
            @NotNull @DecimalMin(value = "0.01", message = "Salary must be greater than 0") BigDecimal annualSalary,
            String currencyCode) {
    }

    public record PageResponse<T>(java.util.List<T> content, int page, int size, long totalElements, int totalPages) {
    }
}
