package com.mvplevel.conveyorservice.service;

import com.mvplevel.conveyorservice.constants.enums.EmploymentStatus;
import com.mvplevel.conveyorservice.constants.enums.Gender;
import com.mvplevel.conveyorservice.constants.enums.MaritalStatus;
import com.mvplevel.conveyorservice.constants.enums.PositionStatus;
import com.mvplevel.conveyorservice.dto.CreditDTO;
import com.mvplevel.conveyorservice.dto.EmploymentDTO;
import com.mvplevel.conveyorservice.dto.PaymentScheduleElement;
import com.mvplevel.conveyorservice.dto.ScoringDataDTO;
import com.mvplevel.conveyorservice.exception.ScoringException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import static com.mvplevel.conveyorservice.constants.ConstantValues.*;

@Service
@Slf4j
public class ScoringService {


    // calculate the rate
    public BigDecimal calcRate(Boolean isInsuranceEnabled, Boolean isSalaryClient){
        BigDecimal rate = CURRENT_RATE;

        if (!isInsuranceEnabled && !isSalaryClient){
            rate = rate.add(BigDecimal.valueOf(3.75));
        }
        if (isInsuranceEnabled && !isSalaryClient){
            rate = rate.add(BigDecimal.valueOf(0.5));
        }
        if (!isInsuranceEnabled && isSalaryClient){
            rate = rate.add(BigDecimal.valueOf(1.5));
        }
        if (isInsuranceEnabled && isSalaryClient){
            rate = rate.subtract(BigDecimal.valueOf(2.5));
        }

        return rate;
    }

    // calculate the scoring
    public BigDecimal scoring(ScoringDataDTO scoringData) {

        log.info("----------------Scoring for client {} {} {} ---", scoringData.getFirstName(), scoringData.getMiddleName(), scoringData.getLastName());

        BigDecimal currRate = new BigDecimal(String.valueOf(CURRENT_RATE));
        EmploymentDTO employmentDTO = scoringData.getEmployment();

        // checking employees status
        if (employmentDTO.getEmploymentStatus() == EmploymentStatus.UNEMPLOYED){
            log.info("Unemployed individuals do not qualify");
            throw new ScoringException("Unemployed individuals do not qualify");
        }
        if (employmentDTO.getEmploymentStatus() == EmploymentStatus.SELF_EMPLOYED){
            log.info("Rate increases by 1 for sel employed clients");
            currRate = currRate.add(SELF_EMPLOYED_RATE);
        }
        if (employmentDTO.getEmploymentStatus() == EmploymentStatus.BUSINESS_OWNER){
            log.info("rate increases by 3 for business owner");
            currRate = currRate.add(BUSINESS_OWNER_RATE);
        }

        // checking employee position
        if (employmentDTO.getPosition() == PositionStatus.MID_MANAGER){
            log.info("rate increases by 2 for middle manager");
            currRate = currRate.add(MIDDLE_MANAGER_RATE);
        }
        if (employmentDTO.getPosition() == PositionStatus.TOP_MANAGER){
            log.info("rate increases by 4 for top manager");
            currRate = currRate.add(TOP_MANAGER_RATE);
        }

        // checking if the salary is more than 20 times and refuse if so
        if (scoringData.getAmount().compareTo(employmentDTO.getSalary().multiply(BigDecimal.valueOf(20))) > 0){
            log.info("loan amount cannot be more than 20 times");
            throw new ScoringException("loan amount cannot be more than 20 times");
        }

        // checking marital status
        if (scoringData.getMaritalStatus() == MaritalStatus.MARRIED){
            log.info("rate is reduced by 3 because marital status is MARRIED");
            currRate = currRate.subtract(MARRIED_RATE);
        }
        if (scoringData.getMaritalStatus() == MaritalStatus.DIVORCED){
            log.info("rate increases by 1 because marital status is DIVORCED");
            currRate = currRate.add(DIVORCED_RATE);
        }

        // checking number of dependent if is over 1 then increases rate by 1
        if (scoringData.getDependentAmount() > 1){
            log.info("rate increases by 1 because the dependent amount is greater 1");
            currRate = currRate.add(BigDecimal.ONE);
        }

        // checking if the age is less than 20 or over 60 then reject
        long currAge = calcAge(scoringData);
        if (currAge < 20){
            log.info("person under 20 years do not qualify for a loan");
            throw new ScoringException("person under 20 years do not qualify for a loan");
        }
        if (currAge > 60){
            log.info("person over 60 do not qualify for a loan");
            throw new ScoringException("person over 60 do not qualify for a loan");
        }

        // checking the gender and their age
        if ((scoringData.getGender() == Gender.FEMALE) && (currAge >= 35 && currAge <= 60)){
            log.info("rate is reduced by 3 because gender is FEMALE and between 35 to 60 of age");
            currRate = currRate.subtract(BigDecimal.valueOf(3));
        }
        if ((scoringData.getGender() == Gender.MALE) && (currAge >= 30 && currAge <= 55)){
            log.info("rate is reduced by 3 because gender is MALE and between 3 to 55 of age");
            currRate = currRate.subtract(BigDecimal.valueOf(3));
        }

        // checking the total working experience of an employee
        if (employmentDTO.getWorkExperienceTotal() < 12){
            log.info("do not qualify for a loan because total working experience is less than 12");
            throw new ScoringException("do not qualify for a loan because total working experience is less than 12");
        }
        if (employmentDTO.getWorkExperienceCurrent() < 3){
            log.info("do not qualify for a loan because current working experience is less than 3");
            throw new ScoringException("do not qualify for a loan because current working experience is less than 3");
        }

        return currRate;

    }

    // calculate monthly payment
    public BigDecimal calcMonthlyPayment(BigDecimal amount, BigDecimal rate, Integer term) {

        log.info("Calculating the monthly payment");

        BigDecimal monthlyRate = calcRateMonth(rate);
        BigDecimal monthlyRateDivAddFunc = monthlyPaymentFormula(monthlyRate, term);

        return amount.multiply(monthlyRateDivAddFunc).setScale(2, RoundingMode.CEILING);

    }

    // calculate creditDTO
    public CreditDTO calcCreditDto(ScoringDataDTO scoringData){

        log.info("Calculating the credit dto");

        BigDecimal rate =  calcRate(scoringData.getIsInsuranceEnabled(), scoringData.getIsSalaryClient());
        BigDecimal monthlyPayment = calcMonthlyPayment(scoringData.getAmount(), rate, scoringData.getTerm());
        BigDecimal totalCost = calcTotalCostLoan(monthlyPayment, scoringData.getTerm());

        CreditDTO creditDTO = new CreditDTO();
        creditDTO.setAmount(scoringData.getAmount());
        creditDTO.setTerm(scoringData.getTerm());
        creditDTO.setMonthlyPayment(monthlyPayment);
        creditDTO.setRate(rate);
        creditDTO.setPsk(totalCost);
        creditDTO.setIsInsuranceEnabled(scoringData.getIsInsuranceEnabled());
        creditDTO.setIsSalaryClient(scoringData.getIsSalaryClient());
        creditDTO.setPaymentSchedule(calcPaymentSchedule(scoringData));

        return creditDTO;

    }

    // calculate monthly payment schedule
    public List<PaymentScheduleElement> calcPaymentSchedule(ScoringDataDTO scoringData){

        log.info("Calculating the payment schedule");

        LocalDate paymentDate = LocalDate.now();
        BigDecimal rate = scoring(scoringData).divide(HUNDRED, 5, RoundingMode.CEILING);
        BigDecimal totalPayment = calcMonthlyPayment(scoringData.getAmount(), rate, scoringData.getTerm());
        BigDecimal remainingDebt = calcTotalCostLoan(scoringData.getAmount(), scoringData.getTerm());
        Integer termPayments = scoringData.getTerm() * MONTHS_PER_YEAR;
        List<PaymentScheduleElement> paymentSchedule = new ArrayList<>();

        for (int i = 1; i <= termPayments; i++){
            paymentDate = paymentDate.plusMonths(1);

            BigDecimal interestPayment = interestPayment(remainingDebt, rate);
            BigDecimal debtPayment = totalPayment.subtract(interestPayment);
            remainingDebt = remainingDebt.subtract(debtPayment);

            paymentSchedule.add(new PaymentScheduleElement(
                    i,
                    paymentDate,
                    totalPayment,
                    interestPayment,
                    debtPayment,
                    remainingDebt));

        }
        return  paymentSchedule;
    }

    // calculate total cost of the loan psk
    public BigDecimal calcTotalCostLoan(BigDecimal monthlyPayment, Integer term){

        log.info("Calculating the total cost of the loan");

        // calculate term in months :: term * 12
        Integer termInMonths = term * MONTHS_PER_YEAR;

        return monthlyPayment.multiply(BigDecimal.valueOf(termInMonths));
    }

    // calculate age
    public long calcAge(ScoringDataDTO scoringData){
        if (scoringData.getBirthDate() != null){
            int age = Period.between(scoringData.getBirthDate(), LocalDate.now()).getYears();

            if (age >= 18)
                return age;
            else
                throw new ScoringException("Age cannot be less than 18");
        }
        else{
            throw new ScoringException("Age cannot be null");
        }
    }

    // calculate rate month
    private BigDecimal calcRateMonth(BigDecimal rate){
        // r/100/n
        return rate.divide(HUNDRED, 5, RoundingMode.CEILING)
                .divide(BigDecimal.valueOf(MONTHS_PER_YEAR), 5, RoundingMode.CEILING);
    }

    // calculate interest payment
    private BigDecimal interestPayment(BigDecimal remainingDebt, BigDecimal rate){

        log.info("Calculating the interest payment");

        return remainingDebt.multiply(calcRateMonth(rate)).setScale(2, RoundingMode.CEILING);
    }

    // a formula to calculate monthly payment
    private BigDecimal monthlyPaymentFormula(BigDecimal monthlyRate, Integer term){

        BigDecimal monthlyCalc = BigDecimal.ONE.add(monthlyRate).pow(term).subtract(BigDecimal.ONE);
        BigDecimal monthlyRateDiv = monthlyRate.divide(monthlyCalc, 5, RoundingMode.CEILING);

        if (monthlyRateDiv.equals(BigDecimal.ZERO)){
            log.error("Cannot divide by zero");
        }

        BigDecimal monthlyRateAdd = monthlyRate.add(monthlyRateDiv);

        return monthlyRateAdd;
    }

}
