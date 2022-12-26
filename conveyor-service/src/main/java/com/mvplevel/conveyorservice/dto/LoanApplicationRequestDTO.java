package com.mvplevel.conveyorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplicationRequestDTO {

    @DecimalMin(value = "10000", message = "must be equal or greater than 10000")
    private BigDecimal amount;

    @Min(value = 6, message = "term cannot be less than 6")
    private Integer term;

    @Size(min = 2, max = 30, message = "length must be between 2 and 30 characters")
    private String firstName;

    @Size(min = 2, max = 30, message = "length must be between 2 and 30 characters")
    private String lastName;

    @Size(min = 2, max = 30, message = "length must be between 2 and 30 characters")
    private String middleName;

    @Email(regexp = "[\\w\\.]{2,50}@[\\w\\.]{2,20}", message = "email address is invalid")
    private String email;

    private LocalDate birthDate;

    @Size(min = 4, max = 4, message = "length must be 4 characters")
    private String passportSeries;

    @Size(min = 6, max = 6, message = "length must be 6 characters")
    private String passportNumber;
}
