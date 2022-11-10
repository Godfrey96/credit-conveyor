package com.mvplevel.conveyorservice.dto;

import com.mvplevel.conveyorservice.enums.EmployementStatus;
import com.mvplevel.conveyorservice.enums.Position;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmploymentDTO {
    private EmployementStatus employmentStatus;
    private String employerINN;
    private BigDecimal salary;
    private Position position;
    private Integer workExperienceTotal;
    private Integer workExperienceCurrent;
}
