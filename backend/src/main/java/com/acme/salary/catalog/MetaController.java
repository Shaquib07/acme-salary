package com.acme.salary.catalog;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meta")
public class MetaController {

    private final FilterOptionsService filters;

    public MetaController(FilterOptionsService filters) {
        this.filters = filters;
    }

    @GetMapping("/filters")
    public Map<String, Object> filters() {
        return Map.of(
                "countries", CountryCurrency.all(),
                "departments", filters.departments(),
                "employmentTypes", List.of("FULL_TIME", "PART_TIME", "CONTRACT"),
                "statuses", List.of("ACTIVE", "INACTIVE"));
    }
}
