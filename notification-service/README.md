# Notification Service

The notification-service is a crucial microservice within the e-commerce platform responsible for handling and dispatching various types of notifications to users. It integrates with messaging systems, such as Kafka, to process notification requests asynchronously, ensuring high throughput and reliability.

# Purpose

The primary goals of the notification-service are to:

- Centralize Notification Logic: Provide a single point for managing all outgoing user notifications, including order confirmations, shipping updates, promotional messages, and receipt generation.
- Decouple Notification Sending: Separate the notification dispatching process from core business logic, allowing other services to simply request a notification without needing to know the underlying sending mechanism.
- Ensure Asynchronous Delivery: Utilize message queues (like Kafka) to handle notification requests, preventing delays in the main application flow and ensuring notifications are processed even under heavy load.
- Support Multiple Channels: Be extensible to support various notification channels (e.g., email, SMS, push notifications).

# Features

- Receipt Generation: Automatically generates receipts for orders, potentially triggered by Kafka messages.
- Kafka Integration: Consumes messages from Kafka topics to trigger notification events.
- Email Dispatch: Sends email notifications (e.g., order confirmations, shipping updates).
- Extensible Notification Channels: Designed to easily add support for new notification types or delivery channels.

# Usage
To integrate with the notification-service, other microservices should publish relevant events or data to a designated Kafka topic (e.g., order-events, product-updates). The notification-service will then consume these messages and trigger the appropriate notification logic.

Example Kafka Message
```
{
    "eventType": "ORDER_PAID",
    "orderId": "ORD12345",
    "userEmail": "user@example.com",
}
```

The notification-service would consume this message and send an order confirmation email to user@example.com.

# Building
To build this module independently:

```
cd notification-service
mvn clean install
```

# Running Locally
Ensure Kafka is Running: The notification-service relies on a running Kafka instance. You can start Kafka using Docker or a local installation.

Configure Application Properties: Update src/main/resources/application.properties (or application.yml) with your Kafka broker address and any other necessary configurations.

```
spring.kafka.bootstrap-servers=localhost:9092
# Add email server configurations (e.g., Spring Mail properties)
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=your_email@example.com
spring.mail.password=your_email_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

Run the Service:
```
cd notification-service
mvn spring-boot:run
```
