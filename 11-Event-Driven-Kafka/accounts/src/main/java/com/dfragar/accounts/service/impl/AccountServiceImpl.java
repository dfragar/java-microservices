package com.dfragar.accounts.service.impl;

import com.dfragar.accounts.constant.AccountConstants;
import com.dfragar.accounts.dto.AccountDto;
import com.dfragar.accounts.dto.AccountMsgDto;
import com.dfragar.accounts.dto.CustomerDto;
import com.dfragar.accounts.entity.Account;
import com.dfragar.accounts.entity.Customer;
import com.dfragar.accounts.exception.CustomerAlreadyExistsException;
import com.dfragar.accounts.exception.ResourceNotFoundException;
import com.dfragar.accounts.mapper.AccountMapper;
import com.dfragar.accounts.mapper.CustomerMapper;
import com.dfragar.accounts.repository.AccountRepository;
import com.dfragar.accounts.repository.CustomerRepository;
import com.dfragar.accounts.service.IAccountService;
import java.util.Optional;
import java.util.Random;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements IAccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private AccountRepository accountRepository;
    private CustomerRepository customerRepository;
    private final StreamBridge streamBridge;

    /**
     *
     * @param customerDto - CustomerDto Object
     */
    @Override
    public void createAccount(CustomerDto customerDto) {
        Customer customer = CustomerMapper.mapToCustomer(customerDto, new Customer());
        Optional<Customer> optionalCustomer = customerRepository.findByMobileNumber(
                customerDto.getMobileNumber());
        if (optionalCustomer.isPresent()) {
            throw new CustomerAlreadyExistsException(
                    "Customer already registered with given mobile number " + customerDto.getMobileNumber());
        }

        Customer savedCustomer = customerRepository.save(customer);
        Account savedAccount = accountRepository.save(createNewAccount(savedCustomer));
        sendCommunication(savedAccount, savedCustomer);
    }

    private void sendCommunication(Account account, Customer customer) {
        var accountsMsgDto = new AccountMsgDto(account.getAccountNumber(), customer.getName(),
                customer.getEmail(), customer.getMobileNumber());
        log.info("Sending Communication request for the details: {}", accountsMsgDto);
        var result = streamBridge.send("sendCommunication-out-0", accountsMsgDto);
        log.info("Is the Communication request successfully triggered ? : {}", result);
    }

    /**
     * @param customer - Customer Object
     * @return the new account details
     */
    private Account createNewAccount(Customer customer) {
        Account newAccount = new Account();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);

        newAccount.setAccountNumber(randomAccNumber);
        newAccount.setAccountType(AccountConstants.SAVINGS);
        newAccount.setBranchAddress(AccountConstants.ADDRESS);

        return newAccount;
    }

    /**
     *
     * @param mobileNumber - mobile number of customer
     * @return Account details based on a given mobile number
     */
    @Override
    public CustomerDto fetchAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );

        Account account = accountRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId",
                        customer.getCustomerId().toString())
        );

        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer, new CustomerDto());
        customerDto.setAccountDto(AccountMapper.mapToAccountDto(account, new AccountDto()));

        return customerDto;
    }

    /**
     * @param customerDto - CustomerDto Object
     * @return boolean indicating if the update of Account details is successful or not
     */
    @Override
    public boolean updateAccount(CustomerDto customerDto) {
        boolean isUpdated = false;
        AccountDto accountDto = customerDto.getAccountDto();
        if (accountDto != null) {
            Account account = accountRepository.findById(accountDto.getAccountNumber()).orElseThrow(
                    () -> new ResourceNotFoundException("Account", "AccountNumber",
                            accountDto.getAccountNumber().toString())
            );
            AccountMapper.mapToAccount(accountDto, account);
            account = accountRepository.save(account);

            Long customerId = account.getCustomerId();
            Customer customer = customerRepository.findById(customerId).orElseThrow(
                    () -> new ResourceNotFoundException("Customer", "CustomerID", customerId.toString())
            );
            CustomerMapper.mapToCustomer(customerDto, customer);
            customerRepository.save(customer);
            isUpdated = true;
        }
        return isUpdated;
    }

    /**
     * @param mobileNumber - Input Mobile Number
     * @return boolean indicating if the delete of Account details is successful or not
     */
    @Override
    public boolean deleteAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        accountRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
        return true;
    }

    /**
     * @param accountNumber - Long
     * @return boolean indicating if the update of communication status is successful or not
     */
    @Override
    public boolean updateCommunicationStatus(Long accountNumber) {
        boolean isUpdated = false;
        if (accountNumber != null) {
            Account account = accountRepository.findById(accountNumber).orElseThrow(
                    () -> new ResourceNotFoundException("Account", "AccountNumber", accountNumber.toString())
            );
            account.setCommunicationSw(true);
            accountRepository.save(account);
            isUpdated = true;
        }
        return isUpdated;
    }

}
