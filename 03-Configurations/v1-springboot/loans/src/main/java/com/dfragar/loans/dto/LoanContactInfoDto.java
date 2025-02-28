package com.dfragar.loans.dto;

import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loans")
public record LoanContactInfoDto(
        String message,
        Map<String, String> contactDetails,
        List<String> onCallSupport
) {

}
