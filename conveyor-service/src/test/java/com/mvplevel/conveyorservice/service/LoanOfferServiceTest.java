package com.mvplevel.conveyorservice.service;

import com.mvplevel.conveyorservice.dto.LoanApplicationRequestDTO;
import com.mvplevel.conveyorservice.dto.LoanOfferDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class LoanOfferServiceTest {

    @InjectMocks
    private LoanOfferService loanOfferService;

//    @BeforeEach

    @Test
    void generateOffers(){
        List<LoanOfferDTO> loanOfferDTOS = loanOfferService.loanOffers(new LoanApplicationRequestDTO());
        assertNotNull(loanOfferDTOS);
        assertEquals(4, loanOfferDTOS.size());
    }

}
