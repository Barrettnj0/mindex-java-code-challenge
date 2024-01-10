package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import jdk.internal.org.jline.utils.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    private String compensationReadUrl;
    private String compensationCreateUrl;
    private String employeeUrl;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        compensationCreateUrl = "http://localhost:" + port + "/compensation";
        compensationReadUrl = "http://localhost:" + port + "/compensation/{id}";
        employeeUrl = "http://localhost:" + port + "/employee";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee(); // Sets up dummy test employee to add to dummy compensation
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody(); // Create employee through api to generate UUID properly

        Compensation testCompensation = new Compensation(); // Sets up dummy compensation to compare to actual
        testCompensation.setEmployee(createdEmployee);
        testCompensation.setSalary(50000.00);
        testCompensation.setEffectiveDate(new Date(1972,1,20));

        // Create checks
        Compensation createdCompensation = restTemplate.postForEntity(compensationCreateUrl, testCompensation, Compensation.class).getBody(); // Internal api call using dummy compensation and proper url

        assertNotNull(createdCompensation.getEmployee()); // If compensation is null then it fails the test as there can't be a compensation without an employee attached
        assertCompensationEquivalence(testCompensation, createdCompensation); // Checks if we receive the same info from api as the dummy since there shouldn't be changes or failure from the api


        // Read checks
        Compensation readCompensation = restTemplate.getForEntity(compensationReadUrl, Compensation.class, createdCompensation.getEmployee().getEmployeeId()).getBody(); // Internal api call using dummy compensation and proper url
        System.out.println("createdCompensation.getEmployee().getEmployeeId()).getBody(): " + createdCompensation.getEmployee().getEmployeeId());
        System.out.println("readCompensation.getEmployee().getEmployeeId()).getBody(): " + readCompensation.getEmployee().getEmployeeId());
        assertCompensationEquivalence(createdCompensation, readCompensation); // Checks if output from post request (data we inserted) is the same as output from get request (data received from db)

    }

    private static void assertCompensationEquivalence(Compensation expected, Compensation actual) { // Checks if expected == actual for employee, salary, and effective date
        assertEquals(expected.getEmployee().getFirstName(), actual.getEmployee().getFirstName());
        assertEquals(expected.getEmployee().getLastName(), actual.getEmployee().getLastName());
        assertEquals(expected.getEmployee().getDepartment(), actual.getEmployee().getDepartment());
        assertEquals(expected.getEmployee().getPosition(), actual.getEmployee().getPosition());
        assertEquals(expected.getSalary(), actual.getSalary());
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
    }
}
