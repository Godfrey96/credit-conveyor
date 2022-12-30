package com.mvplevel.conveyorservice.service;

import com.mvplevel.conveyorservice.dto.EmploymentDTO;
import com.mvplevel.conveyorservice.dto.ScoringDataDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.mvplevel.conveyorservice.constants.enums.EmploymentStatus.BUSINESS_OWNER;
import static com.mvplevel.conveyorservice.constants.enums.Gender.MALE;
import static com.mvplevel.conveyorservice.constants.enums.MaritalStatus.MARRIED;
import static com.mvplevel.conveyorservice.constants.enums.PositionStatus.TOP_MANAGER;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ScoringServiceTest {

    @InjectMocks
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
    void shouldCalculateMonthlyPayment(){
        BigDecimal amount = scoringDataDTO.getAmount();
        BigDecimal rate = new BigDecimal("2.5");
        Integer term = scoringDataDTO.getTerm();
        BigDecimal monthlyPayment = new BigDecimal("2523.60");

        assertThat(scoringService.calcMonthlyPayment(amount, rate, term)).isEqualTo(monthlyPayment);
    }

    @Test
    void shouldCalculateBestRate(){
        BigDecimal expected = new BigDecimal("7.50");
        assertThat(scoringService.calcRate(false, false)).isEqualTo(expected);
    }

    @Test
    void shouldCalculateSecondBestRate(){
        BigDecimal expected = new BigDecimal("4.25");
        assertThat(scoringService.calcRate(true, false)).isEqualTo(expected);
    }

    @Test
    void shouldCalculateSecondWorstRate(){
        BigDecimal expected = new BigDecimal("5.25");
        assertThat(scoringService.calcRate(false, true)).isEqualTo(expected);
    }

    @Test
    void shouldCalculateWorstRate(){
        BigDecimal expected = new BigDecimal("1.25");
        assertThat(scoringService.calcRate(true, true)).isEqualTo(expected);
    }

    @Test
    void shouldCalculateTotalCostOfLoan(){
        BigDecimal amount = scoringDataDTO.getAmount();
        Integer term = scoringDataDTO.getTerm();
        BigDecimal totalCostExpected = new BigDecimal("1920000");

        assertThat(scoringService.calcTotalCostLoan(amount, term)).isEqualTo(totalCostExpected);
    }

}
