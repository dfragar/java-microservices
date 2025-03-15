# Proyecto Microservicios con MySQL y Docker

Este proyecto está basado en una arquitectura de microservicios utilizando **Spring Boot** y **MySQL**, donde se han configurado tres microservicios (`accounts`, `cards`, `loans`) que interactúan con bases de datos separadas, todas gestionadas a través de Docker.

## Descripción de los cambios realizados

### **1. Migración de H2 a MySQL**
Los microservicios que anteriormente utilizaban **H2** como base de datos han sido migrados a **MySQL** para permitir un entorno más robusto y escalable. Se han hecho los siguientes cambios:
- Configuración de conexión a bases de datos MySQL en cada microservicio.
- Actualización de los archivos `schema.sql` en cada servicio para crear las tablas y definir la estructura de datos.

### **2. Configuración de Docker Compose**
Se ha configurado **Docker Compose** para levantar los servicios necesarios, incluyendo las bases de datos MySQL y el servidor de configuración.

- **Bases de datos MySQL**:
  - `accountsdb`: Base de datos para el microservicio de cuentas.
  - `loansdb`: Base de datos para el microservicio de préstamos.
  - `cardsdb`: Base de datos para el microservicio de tarjetas.

- **Microservicios**:
  - `accounts`: Microservicio para gestionar cuentas.
  - `loans`: Microservicio para gestionar préstamos.
  - `cards`: Microservicio para gestionar tarjetas.

- **Config Server**:
  - Un **Config Server** que proporciona configuración centralizada a todos los microservicios.

#### **Archivo `docker-compose.yml`**:
```yaml
services:
  accountsdb:
    container_name: accountsdb
    ports:
      - 3306:3306
    environment:
      MYSQL_DATABASE: accountsdb
    extends:
      file: common-config.yml
      service: microservice-db-config

  loansdb:
    container_name: loansdb
    ports:
      - 3307:3306
    environment:
      MYSQL_DATABASE: loansdb
    extends:
      file: common-config.yml
      service: microservice-db-config

  cardsdb:
    container_name: cardsdb
    ports:
      - 3308:3306
    environment:
      MYSQL_DATABASE: cardsdb
    extends:
      file: common-config.yml
      service: microservice-db-config

  configserver:
    image: "dfragar/configserver:s3"
    container_name: configserver-ms
    ports:
      - "8071:8071"
    healthcheck:
      test: "curl --fail --silent localhost:8071/actuator/health/readiness | grep UP || exit 1"
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 10s
    extends:
      file: common-config.yml
      service: microservice-base-config

  accounts:
    image: "dfragar/accounts:s3"
    container_name: accounts-ms
    ports:
      - "8080:8080"
    environment:
      SPRING_APPLICATION_NAME: "accounts"
      SPRING_DATASOURCE_URL: "jdbc:mysql://accountsdb:3306/accountsdb"
    depends_on:
      accountsdb:
        condition: service_healthy
      configserver:
        condition: service_healthy
    extends:
      file: common-config.yml
      service: microservice-configserver-config

  loans:
    image: "dfragar/loans:s3"
    container_name: loans-ms
    ports:
      - "8090:8090"
    environment:
      SPRING_APPLICATION_NAME: "loans"
      SPRING_DATASOURCE_URL: "jdbc:mysql://loansdb:3306/loansdb"
    depends_on:
      loansdb:
        condition: service_healthy
      configserver:
        condition: service_healthy
    extends:
      file: common-config.yml
      service: microservice-configserver-config

  cards:
    image: "dfragar/cards:s3"
    container_name: cards-ms
    ports:
      - "9000:9000"
    environment:
      SPRING_APPLICATION_NAME: "cards"
      SPRING_DATASOURCE_URL: "jdbc:mysql://cardsdb:3306/cardsdb"
    depends_on:
      cardsdb:
        condition: service_healthy
      configserver:
        condition: service_healthy
    extends:
      file: common-config.yml
      service: microservice-configserver-config

networks:
  bankdemo:
    driver: "bridge"
```

---

### **3. Configuración Común**
El archivo `common-config.yml` define configuraciones comunes que se aplican a todos los servicios, como las configuraciones de la base de datos y el servidor de configuración. Esto facilita la administración y el mantenimiento de las configuraciones.

```yaml
services:
  network-deploy-service:
    networks:
      - bankdemo

  microservice-db-config:
    extends:
      service: network-deploy-service
    image: mysql
    healthcheck:
      test: [ "CMD", "mysqladmin" ,"ping", "-h", "localhost" ]
      timeout: 10s
      retries: 10
      interval: 10s
      start_period: 10s
    environment:
      MYSQL_ROOT_PASSWORD: root

  microservice-base-config:
    extends:
      service: network-deploy-service
    deploy:
      resources:
        limits:
          memory: 700m

  microservice-configserver-config:
    extends:
      service: microservice-base-config
    environment:
      SPRING_PROFILES_ACTIVE: default
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
```

---

### **4. Uso del Proyecto**
Para ejecutar este proyecto, solo necesitas tener **Docker** y **Docker Compose** instalados. Luego, puedes levantar todos los servicios con los siguientes comandos:

1. **Levantar los servicios**:
    ```bash
    docker compose up
    ```

2. **Acceder a los microservicios**:
  - `accounts`: [http://localhost:8080](http://localhost:8080)
  - `loans`: [http://localhost:8090](http://localhost:8090)
  - `cards`: [http://localhost:9000](http://localhost:9000)

3. **Config Server**: [http://localhost:8071](http://localhost:8071)