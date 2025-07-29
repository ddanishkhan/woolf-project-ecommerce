-- payment_db.payments definition

CREATE TABLE `payments` (
  `id` binary(16) NOT NULL,
  `order_id` bigint NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `currency` varchar(3) NOT NULL,
  `failure_reason` text,
  `gateway_payment_id` varchar(255) DEFAULT NULL,
  `gateway_response` text,
  `gateway_session_id` varchar(255) DEFAULT NULL,
  `payment_gateway` varchar(255) NOT NULL,
  `status` enum('CANCELLED','CREATED','EXPIRED','FAILED','PENDING','REFUNDED','SUCCESSFUL') NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);