package com.acme.salary.insight;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
public class InsightController {

    private final InsightService insights;

    public InsightController(InsightService insights) {
        this.insights = insights;
    }

    @GetMapping("/summary")
    public InsightDtos.Summary summary() {
        return insights.summary();
    }

    @GetMapping("/by-country")
    public List<InsightDtos.CountryRow> byCountry() {
        return insights.byCountry();
    }

    @GetMapping("/by-department")
    public List<InsightDtos.DepartmentRow> byDepartment() {
        return insights.byDepartment();
    }

    @GetMapping("/pay-bands")
    public List<InsightDtos.BandCount> payBands() {
        return insights.payBands();
    }
}
