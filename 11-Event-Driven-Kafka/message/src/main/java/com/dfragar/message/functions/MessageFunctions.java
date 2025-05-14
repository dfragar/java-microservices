package com.dfragar.message.functions;

import com.dfragar.message.dto.AccountMsgDto;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageFunctions {

    private static final Logger log = LoggerFactory.getLogger(MessageFunctions.class);

    @Bean
    public Function<AccountMsgDto, AccountMsgDto> email() {
        return accountMsgDto -> {
            log.info("Sending email with the details : " + accountMsgDto.toString());
            return accountMsgDto;
        };
    }

    @Bean
    public Function<AccountMsgDto, Long> sms() {
        return accountMsgDto -> {
            log.info("Sending sms with the details : " + accountMsgDto.toString());
            return accountMsgDto.accountNumber();
        };
    }

}