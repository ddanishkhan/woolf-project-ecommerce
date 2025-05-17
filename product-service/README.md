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

Using external database  
Project could use PostgreSql via Docker, as below  
`docker pull postgres:16.2`  
`docker image ls`
`docker run –-name ecommerce -e POSTGRES_PASSWORD=password -p 5433:5432 postgres`

Project could use MySQL database via Docker
`docker pull mysql:9.0`
`docker run -d --name ecommerce-product -e MYSQL_ROOT_PASSWORD=strong_password -p 3307:3306 mysql:9.0`

`run`: creates a new container or starts an existing one  
`--name CONTAINER_NAME`: gives the container a name. The name should be readable and short. In our case, the name is test-mysql.  
`-e ENV_VARIABLE=value`: the -e tag creates an environment variable that will be accessible within the container. It is crucial to set MYSQL_ROOT_PASSWORD so that we can run SQL commands later from the container. Make sure to store your strong password somewhere safe (not your brain).  
`-d`: short for detached, the -d tag makes the container run in the background. If you remove this tag, the command will keep printing logs until the container stops.  
`image_name`: the final argument is the image name the container will be built from. In this case, our image is mysql.    
`-p 3307:3306`: Maps the container's port 3306 (the default port for MySQL) to your local port 3307. This means any traffic sent to your local port 3307 will be forwarded to the container's port 3306 and your MySQL server will be accessible on that port.

Good revision for mapping annotations.  
https://dev.to/jhonifaber/hibernate-onetoone-onetomany-manytoone-and-manytomany-8ba  
https://github.com/sndpoffcl/ECOMProductService/commit/55c8c24ef1fbe191a9e8a41d18f53f5809e980cf

