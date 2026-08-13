package com.acme.salary.insight;

import java.math.BigDecimal;
import java.util.List;

public class InsightDtos {

    public record CurrencyPayroll(String currencyCode, long headcount, BigDecimal payroll, BigDecimal averageSalary) {
    }

    public record Summary(
            long activeHeadcount,
            List<CurrencyPayroll> payrollByCurrency,
            BigDecimal approximateUsdPayroll,
            String usdDisclaimer) {
    }

    public record CountryRow(
            String countryCode, String currencyCode, long headcount, BigDecimal payroll, BigDecimal averageSalary) {
    }

    public record DepartmentRow(
            String department, String currencyCode, long headcount, BigDecimal payroll, BigDecimal averageSalary) {
    }

    public record BandCount(String band, long headcount) {
    }
}
