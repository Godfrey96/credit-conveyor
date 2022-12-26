package com.mvplevel.conveyorservice.service;
;
import com.mvplevel.conveyorservice.dto.LoanApplicationRequestDTO;
;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoanOfferServiceTest {

    @Mock
    private LoanOfferService loanOfferService;

    @InjectMocks
    private ScoringService scoringService;
    private LoanApplicationRequestDTO loanApplicationRequest;

    @BeforeEach
    void init() {
        loanApplicationRequest = new LoanApplicationRequestDTO(
                BigDecimal.valueOf(20000),
                8,
                "Mogau",
                "Ngwatle",
                "Godfrey",
                "mogau@gmail.com",
                LocalDate.of(1991, 07, 12),
                "1234",
                "987654"
        );
    }

    @Test
    public void shouldReturnListOfFourOffers(){

        when(scoringService.calcRate(anyBoolean(), anyBoolean())).thenReturn(BigDecimal.valueOf(3.75),BigDecimal.valueOf(0.5),BigDecimal.valueOf(1.5),BigDecimal.valueOf(2.5));
        when(scoringService.calcMonthlyPayment(loanApplicationRequest.getAmount(), BigDecimal.valueOf(2.5), loanApplicationRequest.getTerm())).thenReturn(BigDecimal.valueOf(2523.60));
        when(scoringService.calcTotalCostLoan(BigDecimal.valueOf(2523.60), loanApplicationRequest.getTerm())).thenReturn(BigDecimal.valueOf(1920000));

        assertThat(loanOfferService.loanOffers(loanApplicationRequest)).isEqualTo(4);
    }

}
