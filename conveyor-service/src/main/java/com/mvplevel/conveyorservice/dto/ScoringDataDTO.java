package com.mvplevel.conveyorservice.dto;

import com.mvplevel.conveyorservice.enums.Gender;
import com.mvplevel.conveyorservice.enums.MaritalStatus;
import lombok.Data;

import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScoringDataDTO {
    @Min(value=10000, message="must be equal or greater than 10000")
    private BigDecimal amount;
    private Integer term;
    private String firstName;
    private String lastName;
    private String middleName;
    private Gender gender;
    private LocalDate birthDate;
    private String passportSeries;
    private String passportNumber;
    private LocalDate passportIssueDate;
    private String passportIssueBranch;
    private MaritalStatus maritalStatus;
    private Integer dependentAmount;
    private EmploymentDTO employment;
    private String account;
    private Boolean isInsuranceEnabled;
    private Boolean isSalaryClient;
}
