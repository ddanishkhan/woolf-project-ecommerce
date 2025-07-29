# Order Management Service

This service is a core component of the e-commerce platform, responsible for managing customer orders. It handles the complete lifecycle of an order, from creation and payment processing to stock reservation and order fulfillment.

## Features

*   Order Creation and Management
*   Integration with Payment Processing
*   Stock Reservation and Release
*   Event-Driven Communication via Kafka
*   JWT-based Security
*   Order Reconciliation

## Technologies Used

*   Java 21+
*   Spring Boot
*   Apache Kafka
*   Maven
*   Spring Data JPA
*   H2 Database (for development/testing, can be configured for others)
*   Lombok
*   OpenAPI (Swagger UI)


## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/woolf-project-ecommerce.git
cd woolf-project-ecommerce/order-management-service
```

### 2. Build the Project

Use Maven to build the project:

```bash
mvn clean install
```

### 3. Configure Application Properties

Edit the `src/main/resources/application.properties` file to configure database, Kafka, and other settings. For local development, the default H2 database configuration should work out-of-the-box.

```properties
# Example Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092

# Example Security Configuration (adjust as needed)
security.jwt.secret=your_jwt_secret_key
security.jwt.expiration=3600000 # 1 hour in milliseconds
```

## API Endpoints

The API documentation is available via Swagger UI once the application is running:

`http://localhost:8009/swagger-ui.html`

## Event-Driven Architecture

This service leverages Apache Kafka for asynchronous communication:

*   **Producers**: `OrderEventPublisher`, `OrderReceiptGenerationEventPublisher`
*   **Consumers**: `PaymentResultListener`, `StockReservationResultListener`

Events like `OrderEvent`, `PaymentProcessedEvent`, `StockReservationEvent`, `ReleaseStockEvent`, and `OrderReceiptGenerationEvent` are used for inter-service communication.

## Security

The service uses JWT (JSON Web Token) for authentication and authorization. The `CustomJwtDecoder` and `SecurityConfig` classes handle the security configuration. Ensure your JWT secret key is securely managed.

## Error Handling

Global exception handling is implemented via `GlobalExceptionHandler` to provide consistent error responses.
