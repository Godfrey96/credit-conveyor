package com.mvplevel.conveyorservice.service;

import com.mvplevel.conveyorservice.dto.CreditDTO;
import com.mvplevel.conveyorservice.dto.PaymentScheduleElement;
import com.mvplevel.conveyorservice.dto.ScoringDataDTO;

import java.math.BigDecimal;
import java.util.List;

public interface ScoringService {
    BigDecimal calcRate(Boolean isInsuranceEnabled, Boolean isSalaryClient);

    List<PaymentScheduleElement> calcPaymentSchedule(BigDecimal totalAmount, Integer term,
                                                     BigDecimal rate, BigDecimal monthlyPayment);

    BigDecimal calTotalCostPsk(BigDecimal monthlyPayment, Integer term);

    BigDecimal calcMonthlyPayment(BigDecimal totalAmount, Integer term, BigDecimal rate);

    BigDecimal calcRate(ScoringDataDTO scoringDataDTO);
    long calcAge(ScoringDataDTO scoringDataDTO);
    CreditDTO calCreditDTO(ScoringDataDTO scoringData);

}
