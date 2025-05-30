# woolf-project-ecommerce-user-authentication-service
Handles user registration, login, and authorization

Class `RoleSeeder.java` is used to populate the predefined Roles available for users. 

## JWT based authentication via /v2/ endponts.

`docker run -d -e MYSQL_ROOT_PASSWORD=user_mngmnt_root -e MYSQL_DATABASE=user_authentication --name ecommerce-user-authentication -p 3309:3306 mysql:9.3`

//TODO read from here.
https://medium.com/@akhileshanand/spring-boot-api-security-with-jwt-and-role-based-authorization-fea1fd7c9e32


## Keycloak local server  
`docker run -p 8088:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak start-dev` 