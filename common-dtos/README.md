# Common Dtos
This module serves as a central repository for Data Transfer Objects (DTOs) and common utilities shared across various microservices within the e-commerce platform. Its primary purpose is to ensure data consistency, reduce code duplication, and facilitate seamless communication between independent services.

# Purpose
The common-dtos module aims to:

* Standardize Data Structures: Define common data structures (DTOs) that represent entities and messages exchanged between services, such as `OrderResponse`, etc.
* Promote Consistency: Ensure that all services use the same definitions for shared data, preventing discrepancies and integration issues.
* Reduce Duplication: Avoid recreating the same DTOs or utility classes in multiple microservice projects.
* Simplify Communication: Provide a clear and consistent contract for inter-service communication.

# Contents  
This module typically contains:

* DTO Classes: Plain Old Java Objects (POJOs) or records representing data models used across services. These often include fields with appropriate data types and annotations for serialization/deserialization (e.g., Jackson annotations).
* Enums: Shared enumeration types (e.g., `OrderStatus`, `PaymentStatus`).

# Usage  
To use the `common-dtos` module in another microservice, you need to include it as a Maven dependency in that service's `pom.xml` file.
```
<dependency>
    <groupId>com.ecommerce</groupId>
    <artifactId>common-dtos</artifactId>
    <version>1.0.0-SNAPSHOT</version> <!-- Match the version of common-dtos -->
</dependency>
```
Once added as a dependency, you can import and use the DTOs and utility classes directly in your service's code:
```
import com.ecommerce.common.dtos.product.ProductRequest;
import com.ecommerce.common.dtos.product.ProductResponse;
// ... other imports

public class SomeService {
    public ProductResponse processOrder(ProductRequest product) {
        // Use the DTOs
        ProductResponse product = new ProductResponse();
        product.setProductId(product.getId());
        // ...
        return product;
    }
}
```
# Building
To build this module independently:
```
cd common-dtos
mvn clean install
```
This will compile the classes and install the .jar file into your local Maven repository, making it available for other services to depend on.

# Contribution
If we need to add new shared DTOs, enums, or common utilities, they should be added to this module. ensure that any additions are truly generic and applicable to multiple services to maintain the module's purpose.

* Create a new branch for your changes.
* Add your new DTOs/utilities.
* Run `mvn clean install` to ensure everything compiles.
* Commit your changes and open a pull request.

  
