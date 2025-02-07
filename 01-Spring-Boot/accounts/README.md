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
