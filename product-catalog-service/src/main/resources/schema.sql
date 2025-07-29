CREATE TABLE `categories` (
  `id` varchar(36) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_categories_name` (`name`)
)

CREATE TABLE `products` (
  `id` varchar(36) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `price` decimal(38,2) NOT NULL,
  `stock_quantity` int NOT NULL,
  `version` bigint DEFAULT NULL,
  `cover_imageurl` varchar(255) DEFAULT NULL,
  `category_id` varchar(36) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_category_id` (`category_id`),
  CONSTRAINT `FK_category_id` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
);