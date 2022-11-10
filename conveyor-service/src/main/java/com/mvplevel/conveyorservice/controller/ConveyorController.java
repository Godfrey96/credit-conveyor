package com.mvplevel.conveyorservice.controller;

import com.mvplevel.conveyorservice.dto.CreditDTO;
import com.mvplevel.conveyorservice.dto.LoanApplicationRequestDTO;
import com.mvplevel.conveyorservice.dto.LoanOfferDTO;
import com.mvplevel.conveyorservice.dto.ScoringDataDTO;
import com.mvplevel.conveyorservice.service.LoanOfferService;
import com.mvplevel.conveyorservice.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/conveyor")
@RequiredArgsConstructor
public class ConveyorController {

    private LoanOfferService loanOfferService;
    private ScoringService scoringService;

    @PostMapping("/offers")
    public ResponseEntity<List<LoanOfferDTO>> createOffers(@RequestBody LoanApplicationRequestDTO loanApplicationRequest){
        loanOfferService.loanOffers(loanApplicationRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/calculation")
    public ResponseEntity<CreditDTO> calculateCredit(@RequestBody ScoringDataDTO scoringData){
        scoringService.calCreditDTO(scoringData);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
