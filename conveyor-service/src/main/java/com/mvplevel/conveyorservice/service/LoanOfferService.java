package com.mvplevel.conveyorservice.service;

import com.mvplevel.conveyorservice.dto.LoanApplicationRequestDTO;
import com.mvplevel.conveyorservice.dto.LoanOfferDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class LoanOfferService {

    @Autowired
    private ScoringService scoringService;

    public List<LoanOfferDTO> loanOffers(LoanApplicationRequestDTO loanApplication){
        return List.of(
                generateNewOffer(false, false, loanApplication),
                generateNewOffer(false, true, loanApplication),
                generateNewOffer(true, false, loanApplication),
                generateNewOffer(true, true, loanApplication)
        );
    }

    public LoanOfferDTO generateNewOffer(boolean isInsuranceEnabled,
                                         boolean isSalaryClient,
                                         LoanApplicationRequestDTO loanApplicationDTO) {

        BigDecimal rate = scoringService.calcRate(isInsuranceEnabled, isSalaryClient);
        BigDecimal requestedAmount = loanApplicationDTO.getAmount();
        Integer term = loanApplicationDTO.getTerm();
        BigDecimal monthlyPayment = scoringService.calcMonthlyPayment(requestedAmount, rate, term);
        BigDecimal totalAmount = scoringService.calcTotalCostLoan(monthlyPayment, term);

        LoanOfferDTO loanOfferDTO = new LoanOfferDTO();

        loanOfferDTO.setRequestedAmount(requestedAmount);
        loanOfferDTO.setTotalAmount(totalAmount);
        loanOfferDTO.setTerm(term);
        loanOfferDTO.setMonthlyPayment(monthlyPayment);
        loanOfferDTO.setRate(rate);
        loanOfferDTO.setIsInsuranceEnabled(isInsuranceEnabled);
        loanOfferDTO.setIsSalaryClient(isSalaryClient);

        return loanOfferDTO;

    }

}
