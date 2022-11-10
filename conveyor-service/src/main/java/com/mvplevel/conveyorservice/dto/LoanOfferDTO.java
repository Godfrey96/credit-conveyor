package com.mvplevel.conveyorservice.dto;


import lombok.Data;

import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Data
public class LoanOfferDTO {
    private Long applicationId;
    @Min(value=10000, message="must be equal or greater than 10000")
    private BigDecimal requestedAmount;
    @Min(value=10000, message="must be equal or greater than 10000")
    private BigDecimal totalAmount;
    private Integer term;
    private BigDecimal monthlyPayment;
    private BigDecimal rate;
    private Boolean isInsuranceEnabled;
    private Boolean isSalaryClient;
}
