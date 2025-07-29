# E-commerce Microservices Repository
This repository houses a collection of microservices designed to power a robust and scalable e-commerce platform. Each service is independently deployable and focuses on a specific business capability, promoting modularity, resilience, and ease of development.

# Project Structure
The project is organized into individual folders, with each folder representing a distinct microservice.

# Microservices
Below is a list of the microservices included in this repository, along with a brief description of their responsibilities:

* cart-service: Manages user shopping carts, including adding, removing, and updating items.
* common-dtos: Contains shared Data Transfer Objects (DTOs) and common utilities used across multiple microservices to ensure data consistency.
* notification-service: Handles sending various notifications (e.g., order confirmations, shipping updates, receipt generation) to users, potentially integrating with messaging queues like Kafka.
* order-management-service: Manages the lifecycle of customer orders, from creation to fulfillment, including order cancellation logic.
* payment-service: Responsible for processing payments, validating database structures, and handling payment-related transactions.
* product-catalog-service: Manages product information, categories, and availability. Includes API URL changes to /api/ and user input validation.
* user-management-service: Handles user authentication, authorization, and user profile management. Focuses on database structure validation and user creation.

Prerequisites
* Java Development Kit (JDK) 21 or higher
* Maven 3.6.x or higher
* Docker (for running databases or other dependencies for local setup)
* Kafka (for message queuing for local setup)

