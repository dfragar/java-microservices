# Resiliencia en Microservicios con Spring Cloud y Resilience4j

Este proyecto aplica patrones de resiliencia a una arquitectura basada en microservicios. Los servicios
involucrados son:

- `accounts`
- `loans`
- `cards`
- `configserver`
- `eurekaserver`
- `gatewayserver`

Se implementan los siguientes patrones de resiliencia usando **Resilience4j**:

- Circuit Breaker
- Retry
- Rate Limiter
- (Preparado para Bulkhead)

---

## 🔌 Circuit Breaker en Gateway

1. **Agregar dependencia en `pom.xml`:**

```xml

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
</dependency>
```

2. **Configuración en la clase principal `GatewayserverApplication`:**

```java
.route(p ->p
        .

path("/bank/accounts/**")
  .

filters(f ->f
        .

rewritePath("/bank/accounts/(?<segment>.*)","/${segment}")
    .

addResponseHeader("X-Response-Time",LocalDateTime.now().

toString())
        .

circuitBreaker(config ->config.

setName("accountsCircuitBreaker")
      .

setFallbackUri("forward:/contactSupport")))
        .

uri("lb://ACCOUNTS"))
```

3. **Configuración en `application.yml`:**

```yaml
resilience4j.circuitbreaker:
  configs:
    default:
      slidingWindowSize: 10
      permittedNumberOfCallsInHalfOpenState: 2
      failureRateThreshold: 50
      waitDurationInOpenState: 10000
```

4. **Controlador de fallback en Gateway:**

```java

@RestController
public class FallbackController {

    @RequestMapping("/contactSupport")
    public Mono<String> contactSupport() {
        return Mono.just("An error occurred. Please try after some time or contact support team!!!");
    }

}
```

---

## 🔌 Circuit Breaker en el microservicio `accounts`

1. **Agregar dependencia:**

```xml

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

2. **Habilitar Circuit Breaker con OpenFeign en `application.yml`:**

```yaml
spring:
  cloud:
    openfeign:
      circuitbreaker:
        enabled: true
```

3. **Clases Fallback para Feign Clients:**

```java

@Component
public class CardFallback implements CardFeignClient {

    @Override
    public ResponseEntity<CardDto> fetchCardDetails(String correlationId, String mobileNumber) {
        return null;
    }

}

@Component
public class LoanFallback implements LoanFeignClient {

    @Override
    public ResponseEntity<LoanDto> fetchLoanDetails(String correlationId, String mobileNumber) {
        return null;
    }

}
```

4. **Declaración de Feign Clients:**

```java
@FeignClient(name = "loans", fallback = LoanFallback.class)
@FeignClient(name = "cards", fallback = CardFallback.class)
```

---

## ⏳ Configuración de timeouts en Gateway

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 1000
        response-timeout: 2s
```

Estas propiedades configuran el tiempo máximo de espera al establecer una conexión y al recibir una respuesta,
para evitar bloqueos prolongados.

---

## 🔁 Retry Pattern en Gateway

```java
.route(p ->p
        .

path("/bank/loans/**")
  .

filters(f ->f
        .

rewritePath("/bank/loans/(?<segment>.*)","/${segment}")
    .

addResponseHeader("X-Response-Time",LocalDateTime.now().

toString())
        .

retry(retryConfig ->retryConfig.

setRetries(3)
      .

setMethods(HttpMethod.GET)
      .

setBackoff(Duration.ofMillis(100),Duration.

ofMillis(1000), 2,true)))
        .

uri("lb://LOANS"))
```

---

## 🔁 Retry Pattern en `accounts`

1. **Anotar método con `@Retry`:**

```java

@Retry(name = "getBuildInfo", fallbackMethod = "getBuildInfoFallback")
public ResponseEntity<String> getBuildVersion() {
    // lógica principal
}
```

2. **Método de fallback:**

```java
public ResponseEntity<String> getBuildInfoFallback(Throwable throwable) {
    logger.debug("getBuildInfoFallback() method Invoked");
    return ResponseEntity.status(HttpStatus.OK).body("0.9");
}
```

3. **Configuración en `application.yml`:**

```yaml
resilience4j.retry:
  configs:
    default:
      maxAttempts: 3
      waitDuration: 500
      enableExponentialBackoff: true
      exponentialBackoffMultiplier: 2
      ignoreExceptions:
        - java.lang.NullPointerException
      retryExceptions:
        - java.util.concurrent.TimeoutException
```

---

## ⚙️ Configuración global del Circuit Breaker

```java

@Bean
public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
    return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
            .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build())
            .build());
}
```

---

## 🚦 Rate Limiter en Gateway

1. **Dependencia en `pom.xml`:**

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

2. **Configuración en `GatewayserverApplication`:**

```java

@Bean
public RedisRateLimiter redisRateLimiter() {
    return new RedisRateLimiter(1, 1, 1);
}

@Bean
KeyResolver userKeyResolver() {
    return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("user"))
            .defaultIfEmpty("anonymous");
}
```

3. **Ruta con Rate Limiter:**

```java
.route(p ->p
        .

path("/bank/cards/**")
  .

filters(f ->f
        .

rewritePath("/bank/cards/(?<segment>.*)","/${segment}")
    .

addResponseHeader("X-Response-Time",LocalDateTime.now().

toString())
        .

requestRateLimiter(config ->config.

setRateLimiter(redisRateLimiter())
        .

setKeyResolver(userKeyResolver())))
        .

uri("lb://CARDS"))
```

4. **Ejecutar Redis en Docker:**

```bash
docker run -p 6379:6379 --name bankredis -d redis
```

5. **Configurar Redis en `application.yml`:**

```yaml
spring:
  data:
    redis:
      connect-timeout: 2s
      host: localhost
      port: 6379
      timeout: 1s
```

---

## 🚦 Rate Limiter en `accounts`

1. **Anotar método:**

```java

@RateLimiter(name = "getJavaVersion", fallbackMethod = "getJavaVersionFallback")
@GetMapping("/java-version")
public ResponseEntity<String> getJavaVersion() {
    return ResponseEntity.status(HttpStatus.OK)
            .body(environment.getProperty("JAVA_HOME"));
}
```

2. **Fallback:**

```java
public ResponseEntity<String> getJavaVersionFallback(Throwable throwable) {
    return ResponseEntity.status(HttpStatus.OK).body("Java 21");
}
```

3. **Configuración en `application.yml`:**

```yaml
resilience4j.ratelimiter:
  configs:
    default:
      timeoutDuration: 1000
      limitRefreshPeriod: 5000
      limitForPeriod: 1
```

---

## 📚 Explicación de patrones

- **Circuit Breaker**: Evita llamadas repetidas a un servicio que ha fallado. Abre el "circuito" cuando se
  detectan errores consecutivos.
- **Retry**: Reintenta la operación un número de veces antes de fallar, útil para errores transitorios.
- **Rate Limiter**: Limita la cantidad de solicitudes permitidas por unidad de tiempo.
- **Bulkhead**: Aísla recursos para evitar que una falla en una parte del sistema afecte otras partes.

---

## 🧠 Orden de ejecución

Resilience4j permite componer los patrones en orden:

```
Retry (
  CircuitBreaker (
    RateLimiter (
      TimeLimiter (
        Bulkhead (
          Function
        )
      )
    )
  )
)
```

Este orden puede ser personalizado en el `application.yml`:

```yaml
resilience4j.retry.retryAspectOrder: 1
resilience4j.circuitbreaker.circuitBreakerAspectOrder: 2
resilience4j.ratelimiter.rateLimiterAspectOrder: 3
resilience4j.timelimiter.timeLimiterAspectOrder: 4
resilience4j.bulkhead.bulkheadAspectOrder: 5
```

---