# Bank Microservices Project

Este proyecto consta de tres microservicios: **accounts**, **cards** y **loans**. Cada uno de ellos se ha
empaquetado en una imagen Docker utilizando distintos enfoques.

## Microservicios

### Accounts

- Construcción de la imagen utilizando un **Dockerfile**.
- Dockerfile utilizado:
  ```dockerfile
  # Start with a base image containing Java runtime
  FROM openjdk:21-jdk-slim

  # Información sobre el mantenedor
  LABEL "org.opencontainers.image.authors"="https://github.com/dfragar"

  # Copiar el JAR al contenedor
  COPY target/accounts-0.0.1-SNAPSHOT.jar accounts-0.0.1-SNAPSHOT.jar

  # Ejecutar la aplicación
  ENTRYPOINT ["java", "-jar", "accounts-0.0.1-SNAPSHOT.jar"]
  ```

### Loans

- Construcción de la imagen utilizando **Buildpacks** con `spring-boot-maven-plugin`:
  ```xml
  <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
      <configuration>
          <image>
              <name>dfragar/${project.artifactId}:s1</name>
          </image>
      </configuration>
  </plugin>
  ```

### Cards

- Construcción de la imagen utilizando **Google Jib** con `jib-maven-plugin`:
  ```xml
  <plugin>
      <groupId>com.google.cloud.tools</groupId>
      <artifactId>jib-maven-plugin</artifactId>
      <version>3.4.2</version>
      <configuration>
          <to>
              <image>dfragar/${project.artifactId}:s1</image>
          </to>
      </configuration>
  </plugin>
  ```

## Docker Compose

El archivo `docker-compose.yml` se encuentra en el directorio del microservicio **accounts** y permite
desplegar los tres microservicios en un entorno común:

```yaml
services:
  accounts:
    image: "dfragar/accounts:s1"
    container_name: accounts-ms
    ports:
      - "8080:8080"
    deploy:
      resources:
        limits:
          memory: 700m
    networks:
      - bankdemo

  loans:
    image: "dfragar/loans:s1"
    container_name: loans-ms
    ports:
      - "8090:8090"
    deploy:
      resources:
        limits:
          memory: 700m
    networks:
      - bankdemo

  cards:
    image: "dfragar/cards:s1"
    container_name: cards-ms
    ports:
      - "9000:9000"
    deploy:
      resources:
        limits:
          memory: 700m
    networks:
      - bankdemo

networks:
  bankdemo:
    driver: "bridge"
```

## Despliegue con Docker Compose

Para levantar los microservicios, ejecuta:

```sh
docker-compose up -d
```

Para detener los servicios:

```sh
docker-compose down
```

