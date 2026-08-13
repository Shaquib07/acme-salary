package com.acme.salary.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.salary.auth.AppUser;
import com.acme.salary.auth.AppUserRepository;
import com.acme.salary.auth.Role;
import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeRepository;
import com.acme.salary.employee.EmployeeStatus;
import com.acme.salary.employee.EmploymentType;
import com.acme.salary.insight.FxRate;
import com.acme.salary.insight.FxRateRepository;
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
class InsightApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AppUserRepository users;

    @Autowired
    private EmployeeRepository employees;

    @Autowired
    private FxRateRepository fxRates;

    @Autowired
    private PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        employees.deleteAll();
        fxRates.deleteAll();
        users.deleteAll();
        AppUser user = new AppUser();
        user.setEmail("finance@acme.test");
        user.setDisplayName("Finance");
        user.setRole(Role.FINANCE_VIEWER);
        user.setEnabled(true);
        user.setPasswordHash(encoder.encode("Password123!"));
        users.save(user);

        FxRate usd = new FxRate();
        usd.setFromCurrency("USD");
        usd.setToCurrency("USD");
        usd.setRate(BigDecimal.ONE);
        fxRates.save(usd);
        FxRate inr = new FxRate();
        inr.setFromCurrency("INR");
        inr.setToCurrency("USD");
        inr.setRate(new BigDecimal("0.012"));
        fxRates.save(inr);

        saveEmployee("a@acme.example", "US", "USD", "100000");
        saveEmployee("b@acme.example", "IN", "INR", "100000");
    }

    @Test
    void summaryKeepsPayrollSplitByCurrency() throws Exception {
        String token = login();
        mvc.perform(get("/api/insights/summary").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payrollByCurrency.length()").value(2))
                .andExpect(jsonPath("$.approximateUsdPayroll").value(101200.0));
    }

    private void saveEmployee(String email, String country, String currency, String salary) {
        Employee employee = new Employee();
        employee.setEmployeeNumber("EMP-" + email.hashCode());
        employee.setFirstName("Pat");
        employee.setLastName("Pay");
        employee.setEmail(email);
        employee.setDepartment("Finance");
        employee.setJobTitle("Analyst");
        employee.setCountryCode(country);
        employee.setCurrencyCode(currency);
        employee.setAnnualSalary(new BigDecimal(salary));
        employee.setEmploymentType(EmploymentType.FULL_TIME);
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setHiredOn(LocalDate.of(2021, 6, 1));
        employees.save(employee);
    }

    private String login() throws Exception {
        MvcResult result = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", "finance@acme.test", "password", "Password123!"))))
                .andExpect(status().isOk())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
