package com.acme.salary.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acme.salary.employee.EmployeeDtos.CreateEmployeeRequest;
import com.acme.salary.employee.EmployeeDtos.SalaryPatchRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(EmployeeService.class)
class EmployeeServiceTest {

    @Autowired
    private EmployeeService employees;

    @Autowired
    private EmployeeRepository repository;

    @Test
    void paginatesSearchResults() {
        for (int i = 0; i < 5; i++) {
            employees.create(create("user" + i + "@acme.test", "US", "USD", "50000"), "test");
        }
        var page = employees.search(null, "US", null, EmployeeStatus.ACTIVE, 0, 2, "lastName,asc");
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalPages()).isEqualTo(3);
    }

    @Test
    void rejectsDuplicateEmail() {
        employees.create(create("dup@acme.test", "US", "USD", "50000"), "test");
        assertThatThrownBy(() -> employees.create(create("dup@acme.test", "US", "USD", "60000"), "test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void rejectsNonPositiveSalary() {
        Employee saved = employees.create(create("pay@acme.test", "GB", "GBP", "40000"), "test");
        assertThatThrownBy(() -> employees.patchSalary(
                        saved.getId(), new SalaryPatchRequest(BigDecimal.ZERO, "GBP"), "test"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void managerCannotDeactivate() {
        Employee saved = employees.create(create("stay@acme.test", "US", "USD", "50000"), "test");
        assertThatThrownBy(() -> employees.update(
                        saved.getId(),
                        new EmployeeDtos.UpdateEmployeeRequest(null, null, null, null, null, null, null, EmployeeStatus.INACTIVE),
                        "hr@acme.test",
                        false))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
        assertThat(repository.findById(saved.getId()).orElseThrow().getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
    }

    private static CreateEmployeeRequest create(String email, String country, String currency, String salary) {
        return new CreateEmployeeRequest(
                "Ada",
                "Lovelace",
                email,
                "Engineering",
                "Engineer",
                country,
                currency,
                new BigDecimal(salary),
                EmploymentType.FULL_TIME,
                LocalDate.of(2020, 1, 15));
    }
}
