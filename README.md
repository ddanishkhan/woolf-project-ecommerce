# woolf-project-ecommerce-product-service
Product Service is responsible for managing product-related functionalities, such as retrieving product details, updating inventory, and handling product searches.


Package has MVC like structure:   

config: Contains configuration classes, like database configuration (AppConfig).  
controller: Houses the REST API controllers (ApiController).  
dao: Data Access Object layer for handling database interactions. Includes repository interfaces (e.g., UserRepository) for JPA/Hibernate.  
dto: Data Transfer Objects for communication between layers. Includes request and response DTOs.  
model(entity): Entity classes representing database tables (e.g., User, OtherEntity).  
exception: Custom exception classes for handling application-specific exceptions.  
service: Business logic layer, containing service classes (e.g., UserService, OtherEntityService).  
util: Utility classes for various purposes (e.g., DbUtil, OtherUtility).  
resources: Configuration files, such as application properties and logging configuration.  
