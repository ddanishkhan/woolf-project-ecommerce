# Payment Service

This `payment-service` is a core component of the E-commerce ecosystem, responsible for handling all payment-related operations. It integrates with external payment gateways (e.g., Stripe) to process transactions, manage payment statuses, and communicate payment events within the system via Kafka.

## Features

*   **Payment Processing**: Securely processes payments using integrated payment gateways.
*   **Payment Status Management**: Tracks and updates the status of payments (e.g., pending, successful, failed).
*   **Stripe Integration**: Handles payment initiation and webhook events from Stripe.
*   **Kafka Eventing**: Publishes payment-related events (e.g., `PaymentProcessedEvent`, `OrderConfirmedEvent`) to Kafka for other services to consume.
*   **Payment Reconciliation**: Includes a job for reconciling payment statuses.
*   **RESTful API**: Provides endpoints for initiating and querying payment information.

## Technologies Used

*   **Java 21+**
*   **Spring Boot**: Framework for building the application.
*   **Maven**: Dependency management and build automation.
*   **Stripe API**: For payment gateway integration.
*   **Apache Kafka**: For asynchronous event communication.
*   **H2 Database**: In-memory database for development and testing (configured via `schema.sql`).
*   **Lombok**: To reduce boilerplate code.

## Prerequisites

Before you begin, ensure you have the following installed:

*   Java Development Kit (JDK) 21 or higher
*   Maven 3.6.0 or higher
*   A running Kafka instance (or access to one)
*   A Stripe account with API keys

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/woolf-project-ecommerce.git
cd woolf-project-ecommerce/payment-service
```

### 2. Configure Application Properties

Create an `application.properties` file (if it doesn't exist) in `src/main/resources/` or modify the existing one with your configurations.

```properties
# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092 # Replace with your Kafka broker address
payment.kafka.producer.topic.payment-processed=payment-processed-events
payment.kafka.producer.topic.order-confirmed=order-confirmed-events

# Stripe API Configuration
stripe.api.secret-key=${STRIPE_API_SECRET_KEY}
stripe.api.public-key=${STRIPE_API_PUBLIC_KEY}
stripe.api.webhook.sg-key=${STRIPE_API_WEBHOOK_SG_KEY}  # Used for webhook signature verification

# Database Configuration (H2 in-memory for development)
spring.datasource.url=jdbc:h2:mem:paymentdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=always # Ensures schema.sql is run on startup
```

**Note**: For production environments, it is highly recommended to use a persistent database and manage sensitive credentials (like API keys and secrets) securely. While `application.properties` can accept environment variables (e.g., `STRIPE_API_SECRET_KEY`), for robust production deployments, consider using a dedicated secret manager like AWS Secrets Manager or HashiCorp Vault to inject these values securely into the application runtime.

### 3. Build the Project

```bash
./mvnw clean install
```

### 4. Run the Application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8010` by default.

## Kafka Integration

This service produces the following Kafka events:

*   `payment-processed-events`: Published after a payment has been successfully processed or failed. Contains `PaymentProcessedEvent` details.
*   `order-confirmed-events`: Published when an's payment is confirmed, leading to order fulfillment. Contains `OrderConfirmedEvent` details.

## Stripe Webhooks

To receive real-time updates from Stripe (e.g., payment success/failure, refunds), you need to configure a webhook endpoint in your Stripe Dashboard. Point the webhook URL to `http://your-service-url/api/payments/stripe/webhook`. Ensure you set the `stripe.webhook.secret` in your `application.properties` to the signing secret provided by Stripe for your webhook endpoint.

## Database Schema

The `src/main/resources/schema.sql` file defines the initial database schema.
