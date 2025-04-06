package com.dfragar.accounts.service.client;

import com.dfragar.accounts.dto.LoanDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "loans", fallback = LoanFallback.class)
public interface LoanFeignClient {

    @GetMapping(value = "/api/fetch", consumes = "application/json")
    public ResponseEntity<LoanDto> fetchLoanDetails(
            @RequestHeader("bank-correlation-id") String correlationId,
            @RequestParam String mobileNumber);

}
