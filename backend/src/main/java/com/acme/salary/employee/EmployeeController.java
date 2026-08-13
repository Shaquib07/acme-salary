package com.acme.salary.employee;

import com.acme.salary.auth.AppUserDetails;
import com.acme.salary.auth.Role;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employees;

    public EmployeeController(EmployeeService employees) {
        this.employees = employees;
    }

    @GetMapping
    public EmployeeDtos.PageResponse<EmployeeDtos.EmployeeResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        Page<Employee> result = employees.search(q, country, department, status, page, size, sort);
        return new EmployeeDtos.PageResponse<>(
                result.getContent().stream().map(EmployeeDtos.EmployeeResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @GetMapping("/{id}")
    public EmployeeDtos.EmployeeResponse get(@PathVariable Long id) {
        return EmployeeDtos.EmployeeResponse.from(employees.get(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('HR_ADMIN')")
    public EmployeeDtos.EmployeeResponse create(
            @Valid @RequestBody EmployeeDtos.CreateEmployeeRequest request, Authentication authentication) {
        return EmployeeDtos.EmployeeResponse.from(employees.create(request, email(authentication)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR_ADMIN','HR_MANAGER')")
    public EmployeeDtos.EmployeeResponse update(
            @PathVariable Long id,
            @RequestBody EmployeeDtos.UpdateEmployeeRequest request,
            Authentication authentication) {
        boolean admin = hasRole(authentication, Role.HR_ADMIN);
        return EmployeeDtos.EmployeeResponse.from(employees.update(id, request, email(authentication), admin));
    }

    @PatchMapping("/{id}/salary")
    @PreAuthorize("hasAnyRole('HR_ADMIN','HR_MANAGER')")
    public EmployeeDtos.EmployeeResponse patchSalary(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDtos.SalaryPatchRequest request,
            Authentication authentication) {
        return EmployeeDtos.EmployeeResponse.from(employees.patchSalary(id, request, email(authentication)));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('HR_ADMIN','HR_MANAGER')")
    public void export(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) EmployeeStatus status,
            HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.csv");
        List<Employee> rows = employees.listForExport(q, country, department, status);
        try (PrintWriter writer = response.getWriter()) {
            writer.println("employeeNumber,firstName,lastName,email,department,jobTitle,country,currency,annualSalary,status");
            for (Employee e : rows) {
                writer.printf(
                        "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        csv(e.getEmployeeNumber()),
                        csv(e.getFirstName()),
                        csv(e.getLastName()),
                        csv(e.getEmail()),
                        csv(e.getDepartment()),
                        csv(e.getJobTitle()),
                        csv(e.getCountryCode()),
                        csv(e.getCurrencyCode()),
                        e.getAnnualSalary().toPlainString(),
                        e.getStatus());
            }
        }
    }

    private static String email(Authentication authentication) {
        return ((AppUserDetails) authentication.getPrincipal()).getUsername();
    }

    private static boolean hasRole(Authentication authentication, Role role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
