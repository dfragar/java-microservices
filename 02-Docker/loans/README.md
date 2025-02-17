# Loan Service API

## Description

This project is a microservice developed with **Spring Boot** that provides a REST API for loan management. It
includes CRUD operations to create, retrieve, update, and delete loan details.

## Technologies Used

- **Java** with **Spring Boot**
- **Lombok** to reduce boilerplate code
- **Spring Validation** for input validation
- **Swagger (OpenAPI 3)** for API documentation
- **Microservices** with REST-based architecture

## Endpoints

### Create a Loan

- **Method:** POST
- **URL:** /api/create
- **Description:** Creates a new loan for a customer.
- **Parameters:**
    - mobileNumber: Mobile number (must be 10 digits)
- **Response Codes:**
    - 201 CREATED: Loan successfully created
    - 500 INTERNAL SERVER ERROR: Operation failed

### Fetch Loan Details

- **Method:** GET
- **URL:** /api/fetch?mobileNumber={number}
- **Description:** Retrieves loan details associated with a mobile number.
- **Parameters:**
    - mobileNumber: Mobile number (must be 10 digits)
- **Response Codes:**
    - 200 OK: Data retrieved successfully
    - 500 INTERNAL SERVER ERROR: Operation failed

### Update Loan Details

- **Method:** PUT
- **URL:** /api/update
- **Description:** Updates loan details.
- **Response Codes:**
    - 200 OK: Update successful
    - 417 EXPECTATION FAILED: Update failed
    - 500 INTERNAL SERVER ERROR: Operation failed

### Delete a Loan

- **Method:** DELETE
- **URL:** /api/delete?mobileNumber={number}
- **Description:** Deletes loan details associated with a mobile number.
- **Parameters:**
    - mobileNumber: Mobile number (must be 10 digits)
- **Response Codes:**
    - 200 OK: Deletion successful
    - 417 EXPECTATION FAILED: Deletion failed
    - 500 INTERNAL SERVER ERROR: Operation failed

## API Documentation with Swagger

The API includes automatically generated documentation with **Swagger**. You can access it at:

http://localhost:8090/swagger-ui.html

## Docker Image Generation with Buildpack

This project uses **Spring Boot's buildpack** to generate Docker images with the following plugin
configuration:

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

