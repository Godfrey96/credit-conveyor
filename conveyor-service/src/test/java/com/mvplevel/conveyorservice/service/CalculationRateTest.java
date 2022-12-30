package com.mvplevel.conveyorservice.service;

import com.mvplevel.conveyorservice.constants.enums.EmploymentStatus;
import com.mvplevel.conveyorservice.constants.enums.MaritalStatus;
import com.mvplevel.conveyorservice.constants.enums.PositionStatus;
import com.mvplevel.conveyorservice.dto.EmploymentDTO;
import com.mvplevel.conveyorservice.dto.ScoringDataDTO;
import com.mvplevel.conveyorservice.exception.ScoringException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.mvplevel.conveyorservice.constants.enums.EmploymentStatus.*;
import static com.mvplevel.conveyorservice.constants.enums.Gender.FEMALE;
import static com.mvplevel.conveyorservice.constants.enums.Gender.MALE;
import static com.mvplevel.conveyorservice.constants.enums.MaritalStatus.MARRIED;
import static com.mvplevel.conveyorservice.constants.enums.PositionStatus.TOP_MANAGER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CalculationRateTest {

    private ScoringService scoringService;
    private EmploymentDTO employmentDTO;
    private ScoringDataDTO scoringDataDTO;

    @BeforeEach
    void init(){
        scoringService = new ScoringService();
        employmentDTO = new EmploymentDTO(
                BUSINESS_OWNER,
                "123456",
                BigDecimal.valueOf(45000),
                TOP_MANAGER,
                20,
                5
        );
        scoringDataDTO = new ScoringDataDTO(
                BigDecimal.valueOf(20000),
                8,
                "Mogau",
                "Ngwatle",
                "Godfrey",
                MALE,
                LocalDate.of(2000, 05, 05),
                "1234",
                "123456",
                LocalDate.of(2015,11,8),
                "Randburg Home Affairs",
                MARRIED,
                1,
                employmentDTO,
                "Savings",
                true,
                true
        );

    }

    @Test
    void shouldCalculateRate(){
        BigDecimal expected = new BigDecimal("7.75");
        assertThat(scoringService.scoring(scoringDataDTO)).isEqualTo(expected);
    }

    @Test
    void shouldThrowRejectIfUnemployed(){
        employmentDTO.setEmploymentStatus(UNEMPLOYED);
        assertThatThrownBy(() -> {
            scoringService.scoring(scoringDataDTO);
        }).isInstanceOf(ScoringException.class)
                .hasMessageContaining("Unemployed individuals do not qualify");
    }

    @Test
    void shouldCalculateRateBasedOnFemaleAndAgeBetween35And60(){
        scoringDataDTO.setGender(FEMALE);
        scoringDataDTO.setBirthDate(LocalDate.of(1985, 5, 10));
        BigDecimal expected = new BigDecimal("4.75");
        assertThat(scoringService.scoring(scoringDataDTO)).isEqualTo(expected);
    }

    @Test
    void shouldCalculateRateBasedOnMaleAndAgeBetween30And55(){
        scoringDataDTO.setGender(MALE);
        scoringDataDTO.setBirthDate(LocalDate.of(1985, 5, 10));
        BigDecimal expected = new BigDecimal("4.75");
        assertThat(scoringService.scoring(scoringDataDTO)).isEqualTo(expected);
    }

    @Test
    void shouldCalculateRateIfDependentIsGreaterOne(){
        scoringDataDTO.setDependentAmount(3);
        BigDecimal expected = new BigDecimal("8.75");
        assertThat(scoringService.scoring(scoringDataDTO)).isEqualTo(expected);
    }

    @Test
    void shouldThrowExceptionIfTotalWorkExperienceIsLessThan12(){
        employmentDTO.setWorkExperienceTotal(8);
        assertThatThrownBy(() -> {
            scoringService.scoring(scoringDataDTO);
        }).isInstanceOf(ScoringException.class)
                .hasMessageContaining("do not qualify for a loan because total working experience is less than 12");
    }

    @Test
    void shouldThrowExceptionIfCurrentWorkExperienceIsLessThan3(){
        employmentDTO.setWorkExperienceCurrent(2);
        assertThatThrownBy(() -> {
            scoringService.scoring(scoringDataDTO);
        }).isInstanceOf(ScoringException.class)
                .hasMessageContaining("do not qualify for a loan because current working experience is less than 3");
    }

    @Test
    void shouldThrowExceptionIfAgeIsLessThan18(){
        scoringDataDTO.setBirthDate(LocalDate.of(2015, 10, 12));
        assertThatThrownBy(() -> {
            scoringService.scoring(scoringDataDTO);
        }).isInstanceOf(ScoringException.class)
                .hasMessageContaining("Age cannot be less than 18");
    }

    @Test
    void shouldThrowExceptionIfAgeIsLessThan20(){
        scoringDataDTO.setBirthDate(LocalDate.of(2004, 10, 12));
        assertThatThrownBy(() -> {
            scoringService.scoring(scoringDataDTO);
        }).isInstanceOf(ScoringException.class)
                .hasMessageContaining("person under 20 years do not qualify for a loan");
    }

    @Test
    void shouldThrowExceptionIfAgeIsGreaterThan60(){
        scoringDataDTO.setBirthDate(LocalDate.of(1940, 10, 12));
        assertThatThrownBy(() -> {
            scoringService.scoring(scoringDataDTO);
        }).isInstanceOf(ScoringException.class)
                .hasMessageContaining("person over 60 do not qualify for a loan");
    }

    @Test
    void shouldThrowNullExceptionIfAgeIsNull(){
        scoringDataDTO.setBirthDate(null);
        assertThatThrownBy(() -> {
            scoringService.scoring(scoringDataDTO);
        }).isInstanceOf(ScoringException.class)
                .hasMessageContaining("Age cannot be null");
    }

    @Test
    void shouldThrowExceptionIfAmountIsTwentyTimesTheSalary(){
        scoringDataDTO.setAmount(BigDecimal.valueOf(1000000));
        employmentDTO.setSalary(BigDecimal.valueOf(5000));
        assertThatThrownBy(() -> {
            scoringService.scoring(scoringDataDTO);
        }).isInstanceOf(ScoringException.class)
                .hasMessageContaining("loan amount cannot be more than 20 times");
    }

    @ParameterizedTest
    @EnumSource(value = EmploymentStatus.class, names = {"SELF_EMPLOYED", "BUSINESS_OWNER"})
    void shouldCalculateRateBasedOnEmploymentStatus(EmploymentStatus employmentStatus){
        employmentDTO.setEmploymentStatus(employmentStatus);
        assertThat(scoringService.scoring(scoringDataDTO)).isNotNegative();
    }

    @ParameterizedTest
    @EnumSource(value = MaritalStatus.class, names = {"MARRIED", "DIVORCED"})
    void shouldCalculateRateBasedOnMaritalStatus(MaritalStatus maritalStatus){
        scoringDataDTO.setMaritalStatus(maritalStatus);
        assertThat(scoringService.scoring(scoringDataDTO)).isNotNegative();
    }

    @ParameterizedTest
    @EnumSource(value = PositionStatus.class, names = {"MID_MANAGER", "TOP_MANAGER"})
    void shouldCalculateRateBasedOnPosition(PositionStatus positionStatus){
        employmentDTO.setPosition(positionStatus);
        assertThat(scoringService.scoring(scoringDataDTO)).isNotNegative();
    }

}
