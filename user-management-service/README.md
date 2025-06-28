# woolf-project-ecommerce-user-management-service
Handles user registration, login, and authorization

Class `RoleSeeder.java` is used to populate the predefined Roles available for users. 

## JWT based authentication via /v2/ endponts.

## Local Setup
* Docker MYSQL Database
`docker run -d -e MYSQL_ROOT_PASSWORD=user_mngmnt_root -e MYSQL_DATABASE=user_management --name ecommerce-user-management -p 3309:3306 mysql:9.3`
* Kafka local
https://developer.confluent.io/confluent-tutorials/kafka-on-docker
