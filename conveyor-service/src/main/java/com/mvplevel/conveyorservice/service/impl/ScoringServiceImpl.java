package com.mvplevel.conveyorservice.service.impl;

import com.mvplevel.conveyorservice.dto.CreditDTO;
import com.mvplevel.conveyorservice.dto.EmploymentDTO;
import com.mvplevel.conveyorservice.dto.PaymentScheduleElement;
import com.mvplevel.conveyorservice.dto.ScoringDataDTO;
import com.mvplevel.conveyorservice.enums.EmployementStatus;
import com.mvplevel.conveyorservice.enums.Gender;
import com.mvplevel.conveyorservice.enums.MaritalStatus;
import com.mvplevel.conveyorservice.enums.Position;
import com.mvplevel.conveyorservice.service.ScoringService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ScoringServiceImpl implements ScoringService {

    private static final BigDecimal CURRENT_RATE = new BigDecimal(15);
    private static final Integer MONTHS_PER_YEAR = 12;

    @Override
    public BigDecimal calcRate(Boolean isInsuranceEnabled, Boolean isSalaryClient) {

        BigDecimal rate = new BigDecimal(String.valueOf(CURRENT_RATE));

        if (!isInsuranceEnabled && !isSalaryClient){
            rate = rate.add(new BigDecimal(3));
        }
        if (!isInsuranceEnabled && isSalaryClient){
            rate = rate.add(new BigDecimal(2));
        }
        if (isInsuranceEnabled && !isSalaryClient){
            rate = rate.add(new BigDecimal(2));
        }
        if (isInsuranceEnabled && isSalaryClient){
            rate = rate.subtract(new BigDecimal(3));
        }
        return rate;
    }

    @Override
    public long calcAge(ScoringDataDTO scoringDataDTO) {
        if (scoringDataDTO.getBirthDate() != null)
            return Period.between(scoringDataDTO.getBirthDate(), LocalDate.now()).getYears();
        else
            return 0;
    }

    @Override
    public CreditDTO calCreditDTO(ScoringDataDTO scoringData) {

        BigDecimal rate = calcRate(scoringData);
        BigDecimal totalAmount = scoringData.getAmount().multiply(rate);
        BigDecimal monthlyPayment = calcMonthlyPayment(totalAmount, scoringData.getTerm(), rate);
        BigDecimal psk = calTotalCostPsk(monthlyPayment, scoringData.getTerm());

        CreditDTO creditDTO = new CreditDTO();
        creditDTO.setAmount(scoringData.getAmount());
        creditDTO.setTerm(scoringData.getTerm());
        creditDTO.setMonthlyPayment(monthlyPayment);
        creditDTO.setRate(rate);
        creditDTO.setPsk(psk);
        creditDTO.setIsInsuranceEnabled(scoringData.getIsInsuranceEnabled());
        creditDTO.setIsSalaryClient(scoringData.getIsSalaryClient());
        creditDTO.setPaymentSchedule(calcPaymentSchedule(totalAmount,scoringData.getTerm(), rate, monthlyPayment));

        return creditDTO;
    }

    @Override
    public List<PaymentScheduleElement> calcPaymentSchedule(BigDecimal totalAmount, Integer term,
                                                            BigDecimal rate, BigDecimal monthlyPayment) {
        List<PaymentScheduleElement> paymentSchedule = new ArrayList<>();
        BigDecimal remainingDebt = totalAmount.setScale(2, RoundingMode.CEILING);

        for (int i = 1; i < term + 1; i++){
            LocalDate date = LocalDate.now().plusMonths(1);

            BigDecimal interestPayment = rate.divide(BigDecimal.valueOf(MONTHS_PER_YEAR), 5, RoundingMode.CEILING);
            BigDecimal debtPayment = monthlyPayment.subtract(interestPayment);

            remainingDebt = remainingDebt.subtract(debtPayment);

            paymentSchedule.add(new PaymentScheduleElement(
                    i,
                    date,
                    totalAmount,
                    interestPayment,
                    debtPayment,
                    remainingDebt));
        }

        return paymentSchedule;
    }

    @Override
    public BigDecimal calTotalCostPsk(BigDecimal monthlyPayment, Integer term) {
        return monthlyPayment.multiply(BigDecimal.valueOf(term));
    }

    @Override
    public BigDecimal calcMonthlyPayment(BigDecimal totalAmount, Integer term, BigDecimal rate) {

        Integer termYears = term * MONTHS_PER_YEAR;

        // r/100/n
        BigDecimal monthlyInterestRate = rate.divide(BigDecimal.valueOf(100), 5, RoundingMode.CEILING)
                .divide(BigDecimal.valueOf(MONTHS_PER_YEAR), 6, RoundingMode.CEILING);

        // r*(1+r)**n
        BigDecimal numerator = monthlyInterestRate.multiply(BigDecimal.ONE.add(monthlyInterestRate)).pow(termYears);

        // (1+r)**n -1
        BigDecimal denominator = (BigDecimal.ONE.add(monthlyInterestRate)).pow(termYears).subtract(BigDecimal.ONE);

        // a = p*r(1+r)**n / (1+r)**n -1
        BigDecimal monthlyPayment = totalAmount.multiply(numerator.divide(denominator));

        return monthlyPayment;
    }

    @Override
    public BigDecimal calcRate(ScoringDataDTO scoringData) {

        BigDecimal rate = new BigDecimal(String.valueOf(CURRENT_RATE));

        EmploymentDTO employmentDTO = scoringData.getEmployment();

        // checking employees status
        if (employmentDTO.getEmploymentStatus() == EmployementStatus.UNEMPLOYED){
            log.info("Unemployed individuals do not qualify");
        }
        if (employmentDTO.getEmploymentStatus() == EmployementStatus.SELF_EMPLOYED){
            log.info("Rate increases by 1 for sel employed clients");
            rate = rate.add(BigDecimal.ONE);
        }
        if (employmentDTO.getEmploymentStatus() == EmployementStatus.BUSINESS_OWNER){
            log.info("rate increases by 3 for business owner");
            rate = rate.add(new BigDecimal(3));
        }

        // checking employee position
        if (employmentDTO.getPosition() == Position.MID_MANAGER){
            log.info("rate increases by 2 for middle manager");
            rate = rate.add(new BigDecimal(2));
        }
        if (employmentDTO.getPosition() == Position.TOP_MANAGER){
            log.info("rate increases by 4 for top manager");
            rate = rate.add(new BigDecimal(4));
        }

        // checking if the salary is more than 20 times and refuse if so
        if (scoringData.getAmount().compareTo(employmentDTO.getSalary().multiply(new BigDecimal(20))) > 0){
            log.info("loan amount cannot be more than 20 times");
        }

        // checking marital status
        if (scoringData.getMaritalStatus() == MaritalStatus.MARRIED){
            log.info("rate is reduced by 3 because marital status is MARRIED");
            rate = rate.subtract(new BigDecimal(3));
        }
        if (scoringData.getMaritalStatus() == MaritalStatus.DIVORCED){
            log.info("rate increases by 1 because marital status is DIVORCED");
            rate = rate.add(new BigDecimal(1));
        }

        // checking number of dependent if is over 1 then increases rate by 1
        if (scoringData.getDependentAmount() > 1){
            log.info("rate increases by 1 because the dependent amount is greater 1");
            rate = rate.add(new BigDecimal(1));
        }

        // checking if the age is less than 20 or over 60 then reject
        long currAge = calcAge(scoringData);
        if (currAge < 20){
            log.info("person under 20 years do not qualify for a loan");
        }
        if (currAge > 60){
            log.info("person over 60 do not qualify for a loan");
        }

        // checking the gender and their age
        if ((scoringData.getGender() == Gender.FEMALE) && (currAge >= 35 && currAge <= 60)){
            log.info("rate is reduced by 3 because gender is FEMALE and between 35 to 60 of age");
            rate = rate.subtract(new BigDecimal(3));
        }
        if ((scoringData.getGender() == Gender.MALE) && (currAge >= 3 && currAge <= 55)){
            log.info("rate is reduced by 3 because gender is MALE and between 3 to 55 of age");
            rate = rate.subtract(new BigDecimal(3));
        }

        // checking the total working experience of an employee
        if (employmentDTO.getWorkExperienceTotal() < 12){
            log.info("do not qualify for a loan because total working experience is less than 12");
        }
        if (employmentDTO.getWorkExperienceCurrent() < 3){
            log.info("do not qualify for a loan because current working experience is less than 3");
        }
        return rate;

    }

}
