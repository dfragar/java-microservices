# Proyecto de Microservicios con Spring Cloud Config

Este proyecto consta de tres microservicios: `accounts`, `cards` y `loans`, además de un `configserver` que
centraliza la configuración de estos servicios mediante Spring Cloud Config.

## Configuración del Config Server

El `configserver` está configurado para obtener los archivos de configuración desde un repositorio Git. Su
configuración se encuentra en `application.yml`:

```yaml
spring:
  application:
    name: "configserver"
  profiles:
    active: git
  cloud:
    config:
      server:
        git:
          uri: https://github.com/dfragar/bank-config.git
          default-label: main
          timeout: 5
          clone-on-start: true
          force-pull: true
encrypt:
  key: "45D81EC1EF61DF9AD8D3E5BB397F9"
server:
  port: 8071
```

### Configuración Git vs. Native

- Se usa `cloud.config.server.git` para almacenar los archivos en un repositorio remoto.
- Se puede usar `cloud.config.server.native` si se desea almacenar los archivos en el classpath del
  configserver o en una ruta local.

### Seguridad y Cifrado de Propiedades

- `encrypt.key` permite cifrar propiedades sensibles en los archivos `accounts.yml`, `accounts-prod.yml`,
  `accounts-qa.yml`, `cards.yml`, `cards-prod.yml`, `cards-qa.yml`, `loans.yml`, `loans-prod.yml`,
  `loans-qa.yml`.
- Para encriptar/desencriptar valores se pueden usar los endpoints:
    - `http://localhost:8071/encrypt`
    - `http://localhost:8071/decrypt`

## Configuración de los Microservicios

Cada microservicio tiene su propio `application.yml` con configuraciones comunes a todos los entornos. Ejemplo
para `accounts`:

```yaml
server:
  port: 8080
spring:
  application:
    name: "accounts"
  profiles:
    active: "prod"
  datasource:
    url: jdbc:h2:mem:testdb
    driverClassName: org.h2.Driver
    username: sa
    password: ''
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
  config:
    import: "optional:configserver:http://localhost:8071/"
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

### Detección de Cambios en Tiempo Real

- `management.endpoints.web.exposure.include: "*"` permite detectar cambios en los archivos de configuración
  remotos.
- Después de modificar archivos en Git, se debe ejecutar:
    - `http://localhost:8080/actuator/refresh` para cada microservicio.
    - Alternativamente, con Spring Cloud Bus y RabbitMQ, solo se necesita invocar
      `http://localhost:8080/actuator/busrefresh`.

## Integración con Spring Cloud Bus y RabbitMQ

Para evitar ejecutar `refresh` en cada instancia, se usa Spring Cloud Bus con RabbitMQ:

### Instalación de RabbitMQ con Docker:

```sh
docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:4.0-management
```

### Dependencia en cada microservicio:

```xml

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
```

### Configuración en `application.yml` de cada microservicio y configserver:

```yaml
rabbitmq:
  host: "localhost"
  port: 5672
  username: "guest"
  password: "guest"
```

## Automatización con Webhooks de GitHub

Para actualizar automáticamente los microservicios tras un cambio en Git:

### Agregar la dependencia en `configserver`:

```xml

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-monitor</artifactId>
</dependency>
```

### Configurar en `application.yml` de `configserver`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

### Configurar Webhook en GitHub

Dado que `localhost` no es accesible desde GitHub, se puede usar `https://console.hookdeck.com/` para
configurar un webhook.

## Dependencias Requeridas

Todos los microservicios deben incluir:

```xml

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependencyManagement>
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>${spring-cloud.version}</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
</dependencies>
</dependencyManagement>
```

