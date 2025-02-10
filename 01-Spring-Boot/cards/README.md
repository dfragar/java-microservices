# Card Service API

## Description

This project is a microservice developed with **Spring Boot** that provides a REST API for card management. It
includes CRUD operations to create, retrieve, update, and delete card details.

## Technologies Used

- **Java** with **Spring Boot**
- **Lombok** to reduce boilerplate code
- **Spring Validation** for input validation
- **Swagger (OpenAPI 3)** for API documentation
- **Microservices** with REST-based architecture

## Endpoints

### Create a Card

- **Method:** `POST`
- **URL:** `/api/create`
- **Description:** Creates a new card for a customer.
- **Parameters:**
    - `mobileNumber`: Mobile number (must be 10 digits)
- **Response Codes:**
    - `201 CREATED`: Card successfully created
    - `500 INTERNAL SERVER ERROR`: Operation failed

### Fetch Card Details

- **Method:** `GET`
- **URL:** `/api/fetch?mobileNumber={number}`
- **Description:** Retrieves card details associated with a mobile number.
- **Parameters:**
    - `mobileNumber`: Mobile number (must be 10 digits)
- **Response Codes:**
    - `200 OK`: Data retrieved successfully
    - `500 INTERNAL SERVER ERROR`: Operation failed

### Update Card Details

- **Method:** `PUT`
- **URL:** `/api/update`
- **Description:** Updates card details.
- **Response Codes:**
    - `200 OK`: Update successful
    - `417 EXPECTATION FAILED`: Update failed
    - `500 INTERNAL SERVER ERROR`: Operation failed

### Delete a Card

- **Method:** `DELETE`
- **URL:** `/api/delete?mobileNumber={number}`
- **Description:** Deletes card details associated with a mobile number.
- **Parameters:**
    - `mobileNumber`: Mobile number (must be 10 digits)
- **Response Codes:**
    - `200 OK`: Deletion successful
    - `417 EXPECTATION FAILED`: Deletion failed
    - `500 INTERNAL SERVER ERROR`: Operation failed

## API Documentation with Swagger

The API includes automatically generated documentation with **Swagger**. You can access it at:

```
http://localhost:9000/swagger-ui.html
```