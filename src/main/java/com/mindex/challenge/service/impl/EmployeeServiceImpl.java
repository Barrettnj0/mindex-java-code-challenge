package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure generateStructure(String id) {
        LOG.debug("Creating reporting structure based off id [{}]", id);

        ReportingStructure reportingStructure = new ReportingStructure(); // Initialize reporting structure to hold info

        Employee employee = employeeRepository.findByEmployeeId(id); // Use already available employee finding function from repository

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        LOG.info("EmployeeId: " + employee.getEmployeeId());

        reportingStructure.setEmployee(employee); // Use info to populate employee info
        reportingStructure.setNumberOfReports(calcReportNumber(employee)); // Use private method here to populate reports for tidier code

        return reportingStructure;
    }

    private Integer calcReportNumber(Employee employee) {
        Employee reportingEmployee = employee;
        List<String> idList = reportingEmployee.getDirectReports().stream().map(Employee::getEmployeeId).collect(Collectors.toList()); // Create initial idList to iterate through

        Integer reportNumber = 0; // Instantiating reportNumber as 0 since logic will count up by checking each id

        while (idList.size() > 0) { // Iterate over idList while there is still ids to check
            reportingEmployee = employeeRepository.findByEmployeeId(idList.get(0)); // Find employee by id by using current id in index 0

            if(reportingEmployee.getDirectReports() != null) { // Make sure this employee has reporting employees or will throw nullPointer
                idList.addAll(reportingEmployee.getDirectReports() // Add reporting employeeIds to iterative list
                        .stream()
                        .map(Employee::getEmployeeId)
                        .collect(Collectors.toList())
                );
            }

            idList.remove(0); // Remove id in index 0 as it has now been checked, remaining ids will move to next in line
            reportNumber++; // Add one to report number since if we are checking it from idList then it must be added to the reporting employee
        }

        return reportNumber;
    }
}
