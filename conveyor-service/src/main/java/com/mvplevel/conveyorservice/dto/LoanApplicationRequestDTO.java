package com.mvplevel.conveyorservice.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanApplicationRequestDTO {

    @Min(value=10000, message="must be equal or greater than 10000")
    private BigDecimal amount;
    private Integer term;
    @Pattern(regexp="^[a-zA-Z0-9]{2,30}",message="length must be between 2 and 30 characters")
    private String firstName;
    @Pattern(regexp="^[a-zA-Z0-9]{2,30}",message="length must be between 2 and 30 characters")
    private String lastName;
    private String middleName;
    @Pattern(regexp="^[a-zA-Z0-9]{2,50}",message="length must be between 2 and 50 characters")
    private String email;
    private LocalDate birthDate;
    @Pattern(regexp="^[0-9]{4}",message="length must be 4 characters")
    private String passportSeries;
    @Pattern(regexp="^[0-9]{6}",message="length must be 6 characters")
    private String passportNumber;
}
