package com.mvplevel.conveyorservice.service.impl;

import com.mvplevel.conveyorservice.dto.LoanApplicationRequestDTO;
import com.mvplevel.conveyorservice.dto.LoanOfferDTO;
import com.mvplevel.conveyorservice.service.LoanOfferService;
import com.mvplevel.conveyorservice.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class LoanOfferServiceImpl implements LoanOfferService {

    private static final Integer MONTHS_PER_YEAR = 12;
    private ScoringService scoringService;

    @Override
    public List<LoanOfferDTO> loanOffers(LoanApplicationRequestDTO loanApplicationRequest) {
        return List.of(
                generateOffer(loanApplicationRequest, false, false),
                generateOffer(loanApplicationRequest, false, true),
                generateOffer(loanApplicationRequest, true, false),
                generateOffer(loanApplicationRequest, true, true)
        );
    }

    public LoanOfferDTO generateOffer(LoanApplicationRequestDTO loanApplicationRequest,
                                      Boolean isInsuranceEnabled, Boolean isSalaryClient) {
        BigDecimal rate = scoringService.calcRate(isInsuranceEnabled, isSalaryClient);
        BigDecimal requestedAmount = loanApplicationRequest.getAmount();
        Integer term = MONTHS_PER_YEAR * loanApplicationRequest.getTerm();
        BigDecimal monthlyPayment = scoringService.calcMonthlyPayment(requestedAmount, term, rate);
        BigDecimal totalAmount = scoringService.calTotalCostPsk(monthlyPayment, term);

        LoanOfferDTO loanOfferDTO = new LoanOfferDTO();
        loanOfferDTO.setApplicationId(generateApplicationId());
        loanOfferDTO.setRequestedAmount(requestedAmount);
        loanOfferDTO.setTotalAmount(totalAmount);
        loanOfferDTO.setMonthlyPayment(monthlyPayment);
        loanOfferDTO.setRate(rate);
        loanOfferDTO.setIsInsuranceEnabled(isInsuranceEnabled);
        loanOfferDTO.setIsSalaryClient(isSalaryClient);

        return loanOfferDTO;
    }

    private Long generateApplicationId() {
        return new Random().nextLong();
    }


}
