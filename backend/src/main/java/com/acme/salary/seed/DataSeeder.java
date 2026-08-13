package com.acme.salary.seed;

import com.acme.salary.auth.AppUser;
import com.acme.salary.auth.AppUserRepository;
import com.acme.salary.auth.Role;
import com.acme.salary.catalog.CountryCurrency;
import com.acme.salary.config.AcmeProperties;
import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeRepository;
import com.acme.salary.employee.EmployeeStatus;
import com.acme.salary.employee.EmploymentType;
import com.acme.salary.insight.FxRate;
import com.acme.salary.insight.FxRateRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final long SEED = 42L;

    private static final String[] FIRST = {
            "Aisha", "Ben", "Chen", "Diego", "Elena", "Farah", "Gabe", "Hana", "Ivan", "Jia",
            "Kai", "Lina", "Mateo", "Nora", "Omar", "Priya", "Quinn", "Ravi", "Sofia", "Tomas"
    };
    private static final String[] LAST = {
            "Shah", "Nguyen", "Patel", "Silva", "Muller", "Khan", "Sato", "Costa", "Brown", "Singh",
            "Garcia", "Andersen", "Dubois", "Okafor", "Williams", "Yamamoto", "Rossi", "Novak", "Ali", "Park"
    };
    private static final String[] DEPARTMENTS = {
            "Engineering", "Product", "Design", "Sales", "Marketing", "Finance", "People", "Operations", "Legal", "Support"
    };
    private static final String[] TITLES = {
            "Specialist", "Analyst", "Engineer", "Manager", "Lead", "Director", "Coordinator", "Associate"
    };
    private static final Map<String, int[]> SALARY_BANDS = Map.of(
            "USD", new int[] {45000, 180000},
            "GBP", new int[] {32000, 140000},
            "EUR", new int[] {38000, 150000},
            "INR", new int[] {600000, 4500000},
            "SGD", new int[] {50000, 200000},
            "AUD", new int[] {55000, 190000},
            "CAD", new int[] {50000, 175000},
            "JPY", new int[] {4000000, 18000000},
            "BRL", new int[] {40000, 280000},
            "AED", new int[] {80000, 420000});

    private final AcmeProperties properties;
    private final AppUserRepository users;
    private final EmployeeRepository employees;
    private final FxRateRepository fxRates;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            AcmeProperties properties,
            AppUserRepository users,
            EmployeeRepository employees,
            FxRateRepository fxRates,
            PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.users = users;
        this.employees = employees;
        this.fxRates = fxRates;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!properties.seed().enabled()) {
            return;
        }
        seedUsers();
        seedFx();
        seedEmployees();
    }

    private void seedUsers() {
        String password = passwordEncoder.encode(properties.seed().demoPassword());
        upsertUser("admin@acme.test", "Alex Admin", Role.HR_ADMIN, password);
        upsertUser("hr@acme.test", "Morgan Manager", Role.HR_MANAGER, password);
        upsertUser("finance@acme.test", "Riley Finance", Role.FINANCE_VIEWER, password);
    }

    private void upsertUser(String email, String name, Role role, String hash) {
        AppUser user = users.findByEmailIgnoreCase(email).orElseGet(AppUser::new);
        user.setEmail(email);
        user.setDisplayName(name);
        user.setRole(role);
        user.setEnabled(true);
        user.setPasswordHash(hash);
        users.save(user);
    }

    private void seedFx() {
        Map<String, String> rates = Map.of(
                "USD", "1",
                "GBP", "1.27",
                "EUR", "1.08",
                "INR", "0.012",
                "SGD", "0.74",
                "AUD", "0.66",
                "CAD", "0.73",
                "JPY", "0.0067",
                "BRL", "0.18",
                "AED", "0.27");
        rates.forEach((from, rate) -> {
            FxRate row = fxRates.findByFromCurrencyAndToCurrency(from, "USD").orElseGet(FxRate::new);
            row.setFromCurrency(from);
            row.setToCurrency("USD");
            row.setRate(new BigDecimal(rate));
            fxRates.save(row);
        });
    }

    private void seedEmployees() {
        int target = properties.seed().employees();
        long existing = employees.count();
        if (existing >= target) {
            log.info("Employee seed skipped; already have {}", existing);
            return;
        }
        Random random = new Random(SEED);
        List<CountryCurrency.Pair> countries = CountryCurrency.all();
        List<Employee> batch = new ArrayList<>();
        int start = (int) existing + 1;
        for (int i = start; i <= target; i++) {
            CountryCurrency.Pair place = countries.get(random.nextInt(countries.size()));
            String first = FIRST[random.nextInt(FIRST.length)];
            String last = LAST[random.nextInt(LAST.length)];
            Employee employee = new Employee();
            employee.setEmployeeNumber("EMP-%05d".formatted(i));
            employee.setFirstName(first);
            employee.setLastName(last);
            employee.setEmail((first + "." + last + "." + i + "@acme.example").toLowerCase());
            employee.setDepartment(DEPARTMENTS[random.nextInt(DEPARTMENTS.length)]);
            employee.setJobTitle(TITLES[random.nextInt(TITLES.length)]);
            employee.setCountryCode(place.countryCode());
            employee.setCurrencyCode(place.currencyCode());
            employee.setAnnualSalary(salary(place.currencyCode(), random));
            employee.setEmploymentType(EmploymentType.values()[random.nextInt(EmploymentType.values().length)]);
            employee.setStatus(random.nextDouble() < 0.06 ? EmployeeStatus.INACTIVE : EmployeeStatus.ACTIVE);
            employee.setHiredOn(LocalDate.of(2014, 1, 1).plusDays(random.nextInt(4000)));
            employee.setLastEditor("seed");
            batch.add(employee);
            if (batch.size() == 500) {
                employees.saveAll(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            employees.saveAll(batch);
        }
        log.info("Seeded employees up to {}", target);
    }

    private static BigDecimal salary(String currency, Random random) {
        int[] band = SALARY_BANDS.getOrDefault(currency, new int[] {40000, 120000});
        int value = band[0] + random.nextInt(Math.max(1, band[1] - band[0]));
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
