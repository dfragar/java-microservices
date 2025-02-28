package com.dfragar.cards.dto;

import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cards")
public record CardContactInfoDto(
        String message,
        Map<String, String> contactDetails,
        List<String> onCallSupport
) {

}