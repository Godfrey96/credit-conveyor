package com.mvplevel.conveyorservice.service;


import com.mvplevel.conveyorservice.dto.LoanApplicationRequestDTO;
import com.mvplevel.conveyorservice.dto.LoanOfferDTO;

import java.util.List;

public interface LoanOfferService {
    List<LoanOfferDTO> loanOffers(LoanApplicationRequestDTO loanApplicationRequest);
}