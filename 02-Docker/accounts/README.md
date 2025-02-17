# Account Service API

## Description

This project is a microservice developed with **Spring Boot** that provides a REST API for account and
customer management. It includes CRUD operations to create, retrieve, update, and delete accounts.

## Technologies Used

- **Java** with **Spring Boot**
- **Lombok** to reduce boilerplate code
- **Spring Validation** for input validation
- **Swagger (OpenAPI 3)** for API documentation
- **Microservices** with REST-based architecture
- **Docker** for containerization

## Endpoints

### Create an Account

- **Method:** `POST`
- **URL:** `/api/create`
- **Description:** Creates a new customer and associated account.
- **Response Codes:**
    - `201 CREATED`: Account successfully created
    - `500 INTERNAL SERVER ERROR`: Operation failed

### Fetch Account Details

- **Method:** `GET`
- **URL:** `/api/fetch?mobileNumber={number}`
- **Description:** Retrieves account details associated with a mobile number.
- **Parameters:**
    - `mobileNumber`: Mobile number (must be 10 digits)
- **Response Codes:**
    - `200 OK`: Data retrieved successfully
    - `500 INTERNAL SERVER ERROR`: Operation failed

### Update Account Details

- **Method:** `PUT`
- **URL:** `/api/update`
- **Description:** Updates customer and account details.
- **Response Codes:**
    - `200 OK`: Update successful
    - `417 EXPECTATION FAILED`: Update failed
    - `500 INTERNAL SERVER ERROR`: Operation failed

### Delete an Account

- **Method:** `DELETE`
- **URL:** `/api/delete?mobileNumber={number}`
- **Description:** Deletes account and associated customer details.
- **Parameters:**
    - `mobileNumber`: Mobile number (must be 10 digits)
- **Response Codes:**
    - `200 OK`: Deletion successful
    - `417 EXPECTATION FAILED`: Deletion failed
    - `500 INTERNAL SERVER ERROR`: Operation failed

## API Documentation with Swagger

The API includes automatically generated documentation with **Swagger**. You can access it at:

```
http://localhost:8080/swagger-ui.html
```

## Docker Integration

The project includes a **Dockerfile** to build a containerized version of the microservice. The Dockerfile
uses an OpenJDK 21 slim image and copies the application JAR to the container. The application starts with the
following command:

```dockerfile
# Start with a base image containing Java runtime
FROM openjdk:21-jdk-slim

# Information about the image maintainer
LABEL "org.opencontainers.image.authors"="https://github.com/dfragar"

# Add the application's jar to the image
COPY target/accounts-0.0.1-SNAPSHOT.jar accounts-0.0.1-SNAPSHOT.jar

# Execute the application
ENTRYPOINT ["java", "-jar", "accounts-0.0.1-SNAPSHOT.jar"]
```

To build the Docker image, use the command:

```bash
docker build -t dfragar/accounts:s1 .
```

## Docker Compose Integration

The project also includes a `docker-compose.yml` file to manage multiple microservices together. The following
services are defined:

- **accounts**: Account microservice (current project)
- **loans**: Loans microservice
- **cards**: Cards microservice

### docker-compose.yml

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

### Running the Services

To start all services together, use the command:

```bash
docker-compose up -d
```

This will launch the account, loans, and cards microservices in detached mode, allowing them to communicate
over the `bankdemo` network.

To stop the services, use:

```bash
docker-compose down
```

