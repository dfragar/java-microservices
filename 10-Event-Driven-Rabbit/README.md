# 📬 Proyecto `message` - Comunicación entre microservicios con Spring Cloud Stream y RabbitMQ

Este proyecto demuestra cómo crear un microservicio llamado `message` que se comunica con otro microservicio (
`accounts`) utilizando **Spring Cloud Stream** con **RabbitMQ** como middleware de mensajería.

---

## 🛠️ Creación del Proyecto

1. Ve a [https://start.spring.io/](https://start.spring.io/) y crea un nuevo proyecto Spring Boot con el
   nombre **message**.
2. Agrega las siguientes dependencias al archivo `pom.xml`:

```xml

<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-stream</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-stream-binder-rabbit</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-stream-test-binder</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 📄 DTO - `AccountsMsgDto`

```java
package com.dfragar.message.dto;

/**
 * @param accountNumber
 * @param name
 * @param email
 * @param mobileNumber
 */
public record AccountsMsgDto(
        Long accountNumber,
        String name,
        String email,
        String mobileNumber
) {

}
```

---

## ⚙️ Funciones - `MessageFunctions`

```java
package com.dfragar.message.functions;

import com.dfragar.message.dto.AccountsMsgDto;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageFunctions {

    private static final Logger log = LoggerFactory.getLogger(MessageFunctions.class);

    @Bean
    public Function<AccountsMsgDto, AccountsMsgDto> email() {
        return accountsMsgDto -> {
            log.info("Sending email with the details : " + accountsMsgDto.toString());
            return accountsMsgDto;
        };
    }

    @Bean
    public Function<AccountsMsgDto, Long> sms() {
        return accountsMsgDto -> {
            log.info("Sending sms with the details : " + accountsMsgDto.toString());
            return accountsMsgDto.accountNumber();
        };
    }

}
```

---

## ⚙️ Configuración - `application.yml`

```yaml
server:
  port: 9010

spring:
  application:
    name: "message"
  cloud:
    function:
      definition: email|sms
    stream:
      bindings:
        emailsms-in-0:
          destination: send-communication
          group: ${spring.application.name}
        emailsms-out-0:
          destination: communication-sent
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    connection-timeout: 10s
```

---

## 🧩 Integración en microservicio `accounts`

### ➕ Agregar dependencias en el `pom.xml`:

```xml

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream</artifactId>
</dependency>
<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-stream-binder-rabbit</artifactId>
</dependency>
```

### 📄 DTO - `AccountMsgDto`

```java
package com.dfragar.accounts.dto;

/**
 * @param accountNumber
 * @param name
 * @param email
 * @param mobileNumber
 */
public record AccountMsgDto(
        Long accountNumber,
        String name,
        String email,
        String mobileNumber
) {

}
```

### ⚙️ Configuración - `application.yml`

```yaml
spring:
  cloud:
    function:
      definition: updateCommunication
    stream:
      bindings:
        updateCommunication-in-0:
          destination: communication-sent
          group: ${spring.application.name}
        sendCommunication-out-0:
          destination: send-communication
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    connection-timeout: 10s
```

---

## ✉️ Envío de mensajes desde `AccountServiceImpl`

### 🔧 Método `sendCommunication`

```java
private void sendCommunication(Account account, Customer customer) {
    var accountsMsgDto = new AccountMsgDto(account.getAccountNumber(), customer.getName(),
            customer.getEmail(), customer.getMobileNumber());
    log.info("Sending Communication request for the details: {}", accountsMsgDto);
    var result = streamBridge.send("sendCommunication-out-0", accountsMsgDto);
    log.info("Is the Communication request successfully triggered ? : {}", result);
}
```

### 📥 Ejemplo de uso en `createAccount`

```java

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
```

---

## 🐇 Ejecutar RabbitMQ con Docker

```bash
docker run -d -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.12-management
```

---

## 🔄 Función de recepción en `accounts`

### 📦 `AccountFunctions`

```java

@Configuration
public class AccountFunctions {

    private static final Logger log = LoggerFactory.getLogger(AccountFunctions.class);

    @Bean
    public Consumer<Long> updateCommunication(IAccountService accountsService) {
        return accountNumber -> {
            log.info("Updating Communication status for the account number : " + accountNumber.toString());
            accountsService.updateCommunicationStatus(accountNumber);
        };
    }

}
```

### 🧠 Método `updateCommunicationStatus`

```java
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
```

---

## ✅ Resultado Esperado

1. Cuando se crea una cuenta en el microservicio `accounts`, se envía un mensaje al topic
   `send-communication`.
2. El microservicio `message` procesa ese mensaje, simula envío de email y SMS, y reenvía el número de cuenta
   al topic `communication-sent`.
3. `accounts` recibe ese mensaje y actualiza el estado de comunicación de la cuenta correspondiente.