package com.acme.salary.catalog;

import com.acme.salary.employee.EmployeeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FilterOptionsService {

    private final EmployeeRepository employees;

    public FilterOptionsService(EmployeeRepository employees) {
        this.employees = employees;
    }

    @Transactional(readOnly = true)
    public List<String> departments() {
        return employees.findDistinctDepartments();
    }
}
