package com.mvplevel.conveyorservice.dto;

import com.mvplevel.conveyorservice.constants.enums.EmploymentStatus;
import com.mvplevel.conveyorservice.constants.enums.PositionStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmploymentDTO {
    private EmploymentStatus employmentStatus;
    private String employerINN;
    private BigDecimal salary;
    private PositionStatus position;
    private Integer workExperienceTotal;
    private Integer workExperienceCurrent;
}
