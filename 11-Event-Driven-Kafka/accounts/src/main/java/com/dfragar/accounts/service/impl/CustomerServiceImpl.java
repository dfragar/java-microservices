package com.dfragar.accounts.service.impl;

import com.dfragar.accounts.dto.AccountDto;
import com.dfragar.accounts.dto.CardDto;
import com.dfragar.accounts.dto.CustomerDetailsDto;
import com.dfragar.accounts.dto.LoanDto;
import com.dfragar.accounts.entity.Account;
import com.dfragar.accounts.entity.Customer;
import com.dfragar.accounts.exception.ResourceNotFoundException;
import com.dfragar.accounts.mapper.AccountMapper;
import com.dfragar.accounts.mapper.CustomerMapper;
import com.dfragar.accounts.repository.AccountRepository;
import com.dfragar.accounts.repository.CustomerRepository;
import com.dfragar.accounts.service.ICustomersService;
import com.dfragar.accounts.service.client.CardFeignClient;
import com.dfragar.accounts.service.client.LoanFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerServiceImpl implements ICustomersService {

    private AccountRepository accountRepository;
    private CustomerRepository customerRepository;
    private CardFeignClient cardFeignClient;
    private LoanFeignClient loanFeignClient;

    /**
     * @param mobileNumber - Input Mobile Number
     * @param correlationId - Correlation ID value generated at Edge server
     * @return Customer Details based on a given mobileNumber
     */
    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber, String correlationId) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Account account = accountRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId",
                        customer.getCustomerId().toString())
        );

        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer,
                new CustomerDetailsDto());
        customerDetailsDto.setAccountDto(AccountMapper.mapToAccountDto(account, new AccountDto()));

        ResponseEntity<LoanDto> loansDtoResponseEntity = loanFeignClient.fetchLoanDetails(correlationId,
                mobileNumber);
        if (null != loansDtoResponseEntity) {
            customerDetailsDto.setLoanDto(loansDtoResponseEntity.getBody());
        }

        ResponseEntity<CardDto> cardsDtoResponseEntity = cardFeignClient.fetchCardDetails(correlationId,
                mobileNumber);
        if (null != cardsDtoResponseEntity) {
            customerDetailsDto.setCardDto(cardsDtoResponseEntity.getBody());
        }

        return customerDetailsDto;

    }

}