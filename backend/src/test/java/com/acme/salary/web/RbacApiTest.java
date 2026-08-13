package com.acme.salary.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.salary.auth.AppUser;
import com.acme.salary.auth.AppUserRepository;
import com.acme.salary.auth.Role;
import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeRepository;
import com.acme.salary.employee.EmployeeStatus;
import com.acme.salary.employee.EmploymentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class RbacApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AppUserRepository users;

    @Autowired
    private EmployeeRepository employees;

    @Autowired
    private PasswordEncoder encoder;

    private Long employeeId;

    @BeforeEach
    void setUp() {
        employees.deleteAll();
        users.deleteAll();
        saveUser("admin@acme.test", Role.HR_ADMIN);
        saveUser("hr@acme.test", Role.HR_MANAGER);
        saveUser("finance@acme.test", Role.FINANCE_VIEWER);
        Employee employee = new Employee();
        employee.setEmployeeNumber("EMP-00001");
        employee.setFirstName("Sam");
        employee.setLastName("Lee");
        employee.setEmail("sam.lee@acme.example");
        employee.setDepartment("Engineering");
        employee.setJobTitle("Engineer");
        employee.setCountryCode("US");
        employee.setCurrencyCode("USD");
        employee.setAnnualSalary(new BigDecimal("90000.00"));
        employee.setEmploymentType(EmploymentType.FULL_TIME);
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setHiredOn(LocalDate.of(2019, 3, 1));
        employeeId = employees.save(employee).getId();
    }

    @Test
    void unauthenticatedRequestIs401() throws Exception {
        mvc.perform(get("/api/employees")).andExpect(status().isUnauthorized());
    }

    @Test
    void viewerCannotPatchSalary() throws Exception {
        String token = login("finance@acme.test");
        mvc.perform(patch("/api/employees/" + employeeId + "/salary")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"annualSalary\": 99000}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewerCannotExport() throws Exception {
        String token = login("finance@acme.test");
        mvc.perform(get("/api/employees/export").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCannotCreate() throws Exception {
        String token = login("hr@acme.test");
        mvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "firstName", "New",
                                "lastName", "Hire",
                                "email", "new.hire@acme.test",
                                "department", "Sales",
                                "jobTitle", "Rep",
                                "countryCode", "US",
                                "currencyCode", "USD",
                                "annualSalary", 70000,
                                "employmentType", "FULL_TIME",
                                "hiredOn", "2024-01-01"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanPatchSalary() throws Exception {
        String token = login("hr@acme.test");
        mvc.perform(patch("/api/employees/" + employeeId + "/salary")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"annualSalary\": 99000}"))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanCreate() throws Exception {
        String token = login("admin@acme.test");
        mvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "firstName", "New",
                                "lastName", "Hire",
                                "email", "new.hire@acme.test",
                                "department", "Sales",
                                "jobTitle", "Rep",
                                "countryCode", "US",
                                "currencyCode", "USD",
                                "annualSalary", 70000,
                                "employmentType", "FULL_TIME",
                                "hiredOn", "2024-01-01"))))
                .andExpect(status().isOk());
    }

    private void saveUser(String email, Role role) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setDisplayName(email);
        user.setRole(role);
        user.setEnabled(true);
        user.setPasswordHash(encoder.encode("Password123!"));
        users.save(user);
    }

    private String login(String email) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", email, "password", "Password123!"))))
                .andExpect(status().isOk())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
