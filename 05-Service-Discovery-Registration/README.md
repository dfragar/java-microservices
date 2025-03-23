# Proyecto Spring Boot con Eureka Server, Eureka Client y OpenFeign

Este proyecto configura un sistema de microservicios basado en **Spring Boot**, que incluye:
- **Eureka Server**: Para el registro y descubrimiento de servicios.
- **Eureka Client**: Para registrar y descubrir otros microservicios.
- **OpenFeign**: Para la comunicación entre microservicios mediante HTTP.

---

## 🏁 **Configuración del Eureka Server**

1. **Crear el proyecto Eureka Server**
  - Crear un nuevo proyecto Spring Boot desde [Spring Initializr](https://start.spring.io) con las siguientes dependencias:
    - Spring Boot DevTools
    - Spring Web
    - Spring Cloud Discovery (Eureka Server)

2. **Añadir la anotación `@EnableEurekaServer`**  
   En la clase principal `EurekaServerApplication.java`, añade la anotación:

```java
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

3. **Configurar el archivo `application.yml`**  
   Crear el archivo `application.yml` en `src/main/resources` y añadir las siguientes propiedades:

```yaml
spring:
  application:
    name: "eurekaserver"
  config:
    import: "optional:configserver:http://localhost:8071/"

management:
  endpoints:
    web:
      exposure:
        include: "*"
  health:
    readiness-state:
      enabled: true
    liveness-state:
      enabled: true
  endpoint:
    health:
      probes:
        enabled: true
```

4. **Crear la configuración en el Config Server**  
   En el proyecto `configserver`, crea el archivo `eurekaserver.yml`:

```yaml
server:
  port: 8070

eureka:
  instance:
    hostname: localhost
  client:
    fetchRegistry: false
    registerWithEureka: false
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

---

## 🚀 **Configuración de los microservicios (accounts, cards, loans)**

1. **Añadir la dependencia de Eureka Client**  
   En el archivo `pom.xml` de cada microservicio:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

2. **Configurar el `application.yml`**  
   En cada microservicio (`accounts`, `cards`, `loans`), añade lo siguiente:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    shutdown:
      access: unrestricted
  info:
    env:
      enabled: true

eureka:
  instance:
    preferIpAddress: true
  client:
    fetchRegistry: true
    registerWithEureka: true
    serviceUrl:
      defaultZone: http://localhost:8070/eureka/

info:
  app:
    name: "accounts"
    description: "Bank Accounts Application"
    version: "1.0.0"
```

3. **Verificar el registro en Eureka**  
   Abre el navegador y accede a:

```
http://localhost:8070/eureka/apps
```

4. **Apagar un microservicio mediante Actuator**  
   Usa el siguiente comando para apagar un microservicio:

```bash
curl -X POST http://localhost:9000/actuator/shutdown
```

---

## 🔗 **Configuración de OpenFeign**

1. **Añadir la dependencia de OpenFeign**  
   En el archivo `pom.xml` del microservicio que hará las llamadas (por ejemplo, `accounts`):

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

2. **Habilitar Feign en la clase principal**  
   Añade la anotación `@EnableFeignClients` en `AccountsApplication.java`:

```java
@EnableFeignClients
@SpringBootApplication
public class AccountsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountsApplication.class, args);
    }
}
```

3. **Crear un Feign Client para comunicarse con `cards`**  
   Crea la clase `CardFeignClient.java`:

```java
@FeignClient("cards")
public interface CardFeignClient {

    @GetMapping(value = "/api/fetch", consumes = "application/json")
    ResponseEntity<CardDto> fetchCardDetails(@RequestParam String mobileNumber);
}
```

- El valor `"cards"` en `@FeignClient("cards")` debe coincidir con el valor de `spring.application.name` en el microservicio `cards`.

4. **Crear un Feign Client para comunicarse con `loans`**  
   Crea la clase `LoanFeignClient.java`:

```java
@FeignClient("loans")
public interface LoanFeignClient {

    @GetMapping(value = "/api/fetch", consumes = "application/json")
    ResponseEntity<LoanDto> fetchLoanDetails(@RequestParam String mobileNumber);
}
```

---

## ✅ **Pruebas**

1. Inicia el Config Server.
2. Inicia el Eureka Server.
3. Inicia cada microservicio (`accounts`, `cards`, `loans`).
4. Accede a `http://localhost:8070/eureka/apps` para verificar los microservicios registrados.
5. Realiza una petición para obtener los detalles de un cliente:

```bash
curl "http://localhost:9000/api/fetchCustomerDetails?mobileNumber=1234567890"
```

---

## 🎯 **Conclusión**
Este proyecto demuestra cómo crear un ecosistema de microservicios con **Eureka** para el descubrimiento de servicios y **OpenFeign** para la comunicación interna.

