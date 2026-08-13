package com.acme.salary.employee;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    boolean existsByEmailIgnoreCase(String email);

    @Query("select coalesce(max(e.id), 0) from Employee e")
    long maxId();

    @Query("select distinct e.department from Employee e order by e.department")
    List<String> findDistinctDepartments();

    @Query("""
            select e.currencyCode as currencyCode, count(e) as headcount,
                   sum(e.annualSalary) as payroll, avg(e.annualSalary) as averageSalary
            from Employee e
            where e.status = :status
            group by e.currencyCode
            order by e.currencyCode
            """)
    List<CurrencyAgg> aggregateByCurrency(@Param("status") EmployeeStatus status);

    @Query("""
            select e.countryCode as countryCode, e.currencyCode as currencyCode, count(e) as headcount,
                   sum(e.annualSalary) as payroll, avg(e.annualSalary) as averageSalary
            from Employee e
            where e.status = :status
            group by e.countryCode, e.currencyCode
            order by e.countryCode
            """)
    List<CountryAgg> aggregateByCountry(@Param("status") EmployeeStatus status);

    @Query("""
            select e.department as department, e.currencyCode as currencyCode, count(e) as headcount,
                   sum(e.annualSalary) as payroll, avg(e.annualSalary) as averageSalary
            from Employee e
            where e.status = :status
            group by e.department, e.currencyCode
            order by e.department, e.currencyCode
            """)
    List<DepartmentAgg> aggregateByDepartment(@Param("status") EmployeeStatus status);

    @Query("""
            select e.currencyCode as currencyCode, e.annualSalary as annualSalary
            from Employee e
            where e.status = :status
            """)
    List<SalarySlice> activeSalaries(@Param("status") EmployeeStatus status);

    interface CurrencyAgg {
        String getCurrencyCode();

        long getHeadcount();

        BigDecimal getPayroll();

        BigDecimal getAverageSalary();
    }

    interface CountryAgg {
        String getCountryCode();

        String getCurrencyCode();

        long getHeadcount();

        BigDecimal getPayroll();

        BigDecimal getAverageSalary();
    }

    interface DepartmentAgg {
        String getDepartment();

        String getCurrencyCode();

        long getHeadcount();

        BigDecimal getPayroll();

        BigDecimal getAverageSalary();
    }

    interface SalarySlice {
        String getCurrencyCode();

        BigDecimal getAnnualSalary();
    }
}
