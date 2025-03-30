# 🚀 **Bank Gateway Server**

Este proyecto configura un **Gateway Server** usando **Spring Cloud Gateway** para enrutar y filtrar
solicitudes hacia varios microservicios (Accounts, Cards y Loans). Además, se integra con **Eureka Server**
para la gestión de servicios y con **Config Server** para la configuración centralizada.

---

## 📂 **Estructura del Proyecto**

El sistema está compuesto por los siguientes microservicios:

- **Accounts** → Servicio de cuentas bancarias.
- **Cards** → Servicio de tarjetas bancarias.
- **Loans** → Servicio de préstamos bancarios.

También se incluyen otros servicios de infraestructura:

- **Config Server** → Gestión centralizada de configuración para todos los microservicios.
- **Eureka Server** → Registro y descubrimiento de servicios.
- **Gateway Server** → Punto de entrada unificado para todas las solicitudes externas hacia los
  microservicios.

---

## 🌐 **Configuración del Gateway Server**

### **Archivo `application.yml`**

El archivo `application.yml` configura el comportamiento del Gateway Server.

```yaml
spring:
  application:
    name: "gatewayserver"
  config:
    import: "optional:configserver:http://localhost:8071/"
  cloud:
    gateway:
      discovery:
        locator:
          enabled: false
          lowerCaseServiceId: true

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    gateway:
      access: unrestricted
  info:
    env:
      enabled: true

info:
  app:
    name: "gatewayserver"
    description: "Bank Gateway Server Application"
    version: "1.0.0"

logging:
  level:
    com:
      eazybytes:
        gatewayserver: DEBUG
```

### ✅ **Explicación de las Propiedades Clave**

| Propiedad                                                   | Descripción                                                                                                                                                                                    |
|-------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `spring.application.name`                                   | Nombre del microservicio.                                                                                                                                                                      |
| `config.import`                                             | Ruta para importar configuración desde el Config Server.                                                                                                                                       |
| `spring.cloud.gateway.discovery.locator.enabled`            | Si está en `true`, el Gateway detecta automáticamente los servicios registrados en Eureka. En este caso está desactivado (`false`) porque se configuran rutas manualmente.                     |
| `spring.cloud.gateway.discovery.locator.lowerCaseServiceId` | Si está en `true`, convierte los nombres de los servicios descubiertos a **minúsculas**. Esto evita conflictos debido a diferencias en mayúsculas y minúsculas al definir rutas en el gateway. |
| `management.endpoints.web.exposure.include`                 | Expone todos los endpoints de gestión (incluyendo las rutas configuradas).                                                                                                                     |
| `logging.level`                                             | Nivel de logging para el servicio gateway.                                                                                                                                                     |

---

## 🏗️ **Dependencias (pom.xml)**

Añade las siguientes dependencias al archivo `pom.xml` para habilitar Spring Cloud Gateway, Eureka Client y
Config Server:

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-starter-config</artifactId>
</dependency>
<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-devtools</artifactId>
<scope>runtime</scope>
<optional>true</optional>
</dependency>
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-test</artifactId>
<scope>test</scope>
</dependency>
<dependency>
<groupId>io.projectreactor</groupId>
<artifactId>reactor-test</artifactId>
<scope>test</scope>
</dependency>
```

### ✅ **Explicación de las dependencias**

| Dependencia                                  | Descripción                                                         |
|----------------------------------------------|---------------------------------------------------------------------|
| `spring-boot-starter-actuator`               | Actuator para exponer endpoints de gestión y supervisión.           |
| `spring-cloud-starter-config`                | Permite importar configuraciones desde Config Server.               |
| `spring-cloud-starter-gateway`               | Habilita Spring Cloud Gateway para gestionar el enrutamiento.       |
| `spring-cloud-starter-netflix-eureka-client` | Permite registrar el microservicio en Eureka.                       |
| `spring-boot-devtools`                       | Proporciona herramientas para desarrollo (como recarga automática). |
| `spring-boot-starter-test`                   | Librería de pruebas para Spring Boot.                               |
| `reactor-test`                               | Librería para pruebas de aplicaciones reactivas.                    |

---

## 🚀 **Configuración de Rutas (GatewayServerApplication)**

### **Clase Principal `GatewayserverApplication`**

```java

@Bean
public RouteLocator eazyBankRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
    return routeLocatorBuilder.routes()
            .route(p -> p
                    .path("/bank/accounts/**")
                    .filters(f -> f.rewritePath("/bank/accounts/(?<segment>.*)", "/${segment}")
                            .addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
                    .uri("lb://ACCOUNTS"))
            .route(p -> p
                    .path("/bank/loans/**")
                    .filters(f -> f.rewritePath("/bank/loans/(?<segment>.*)", "/${segment}")
                            .addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
                    .uri("lb://LOANS"))
            .route(p -> p
                    .path("/bank/cards/**")
                    .filters(f -> f.rewritePath("/bank/cards/(?<segment>.*)", "/${segment}")
                            .addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
                    .uri("lb://CARDS"))
            .build();
}
```

### ✅ **Explicación**

- `lb://ACCOUNTS` → El gateway utiliza el **Load Balancer** para enrutar hacia el microservicio
  correspondiente registrado en Eureka.
- `rewritePath()` → Reescribe la URL entrante antes de enviarla al microservicio.
- `addResponseHeader()` → Añade una cabecera personalizada `X-Response-Time` en la respuesta.

---

## 🌐 **Actuator (Ver rutas)**

Para ver las rutas configuradas en el Gateway:  
👉 [http://localhost:8072/actuator/gateway/routes](http://localhost:8072/actuator/gateway/routes)

---

## 🔥 **Cambios en los Microservicios**

1. Asegúrate de que cada microservicio esté registrado en **Eureka**.
2. Configura cada microservicio para aceptar las nuevas rutas definidas en el Gateway.
3. Asegúrate de que el `bank-correlation-id` se propague correctamente entre los servicios.

---

## 🎯 **Objetivo**

✅ Centralizar la configuración y el enrutamiento de las solicitudes.  
✅ Implementar trazabilidad entre servicios mediante `bank-correlation-id`.  
✅ Reescribir rutas para simplificar la comunicación interna entre servicios.

---
