package com.acme.salary.insight;

import com.acme.salary.employee.EmployeeRepository;
import com.acme.salary.employee.EmployeeStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InsightService {

    public static final List<PayBand> BANDS = List.of(
            new PayBand("0-30k", bd(0), bd(30000)),
            new PayBand("30-50k", bd(30000), bd(50000)),
            new PayBand("50-75k", bd(50000), bd(75000)),
            new PayBand("75-100k", bd(75000), bd(100000)),
            new PayBand("100-150k", bd(100000), bd(150000)),
            new PayBand("150k+", bd(150000), null));

    private final EmployeeRepository employees;
    private final FxRateRepository fxRates;

    public InsightService(EmployeeRepository employees, FxRateRepository fxRates) {
        this.employees = employees;
        this.fxRates = fxRates;
    }

    @Transactional(readOnly = true)
    public InsightDtos.Summary summary() {
        List<EmployeeRepository.CurrencyAgg> byCurrency = employees.aggregateByCurrency(EmployeeStatus.ACTIVE);
        long headcount = byCurrency.stream().mapToLong(EmployeeRepository.CurrencyAgg::getHeadcount).sum();
        Map<String, BigDecimal> fx = usdRates();
        BigDecimal usd = BigDecimal.ZERO;
        List<InsightDtos.CurrencyPayroll> payroll = new ArrayList<>();
        for (EmployeeRepository.CurrencyAgg row : byCurrency) {
            payroll.add(new InsightDtos.CurrencyPayroll(
                    row.getCurrencyCode(),
                    row.getHeadcount(),
                    scale(row.getPayroll()),
                    scale(row.getAverageSalary())));
            usd = usd.add(toUsd(row.getPayroll(), row.getCurrencyCode(), fx));
        }
        return new InsightDtos.Summary(
                headcount,
                payroll,
                scale(usd),
                "USD totals use seeded FX rates and are approximate. Native currency payrolls are never mixed.");
    }

    @Transactional(readOnly = true)
    public List<InsightDtos.CountryRow> byCountry() {
        return employees.aggregateByCountry(EmployeeStatus.ACTIVE).stream()
                .map(r -> new InsightDtos.CountryRow(
                        r.getCountryCode(),
                        r.getCurrencyCode(),
                        r.getHeadcount(),
                        scale(r.getPayroll()),
                        scale(r.getAverageSalary())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InsightDtos.DepartmentRow> byDepartment() {
        return employees.aggregateByDepartment(EmployeeStatus.ACTIVE).stream()
                .map(r -> new InsightDtos.DepartmentRow(
                        r.getDepartment(),
                        r.getCurrencyCode(),
                        r.getHeadcount(),
                        scale(r.getPayroll()),
                        scale(r.getAverageSalary())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InsightDtos.BandCount> payBands() {
        Map<String, BigDecimal> fx = usdRates();
        Map<String, Long> counts = new LinkedHashMap<>();
        BANDS.forEach(b -> counts.put(b.label(), 0L));
        for (EmployeeRepository.SalarySlice slice : employees.activeSalaries(EmployeeStatus.ACTIVE)) {
            BigDecimal usd = toUsd(slice.getAnnualSalary(), slice.getCurrencyCode(), fx);
            String label = bandFor(usd);
            counts.merge(label, 1L, Long::sum);
        }
        return counts.entrySet().stream()
                .map(e -> new InsightDtos.BandCount(e.getKey(), e.getValue()))
                .toList();
    }

    public static String bandFor(BigDecimal usdSalary) {
        BigDecimal value = usdSalary == null ? BigDecimal.ZERO : usdSalary;
        for (PayBand band : BANDS) {
            boolean geMin = value.compareTo(band.minInclusive()) >= 0;
            boolean ltMax = band.maxExclusive() == null || value.compareTo(band.maxExclusive()) < 0;
            if (geMin && ltMax) {
                return band.label();
            }
        }
        return BANDS.get(BANDS.size() - 1).label();
    }

    private Map<String, BigDecimal> usdRates() {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        map.put("USD", BigDecimal.ONE);
        fxRates.findByToCurrency("USD").forEach(r -> map.put(r.getFromCurrency(), r.getRate()));
        return map;
    }

    static BigDecimal toUsd(BigDecimal amount, String currency, Map<String, BigDecimal> fx) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = fx.getOrDefault(currency, BigDecimal.ZERO);
        return amount.multiply(rate);
    }

    private static BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal bd(int value) {
        return BigDecimal.valueOf(value);
    }

    public record PayBand(String label, BigDecimal minInclusive, BigDecimal maxExclusive) {
    }
}
