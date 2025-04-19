package com.dfragar.accounts.service.client;

import com.dfragar.accounts.dto.CardDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class CardFallback implements CardFeignClient {

    @Override
    public ResponseEntity<CardDto> fetchCardDetails(String correlationId, String mobileNumber) {
        return null;
    }

}
