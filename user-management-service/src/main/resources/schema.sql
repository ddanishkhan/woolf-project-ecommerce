-- user_management.app_roles definition

CREATE TABLE app_roles (
  id INT NOT NULL IDENTITY,
  name VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (name)
);

-- user_management.app_users definition

CREATE TABLE app_users (
  id BIGINT NOT NULL IDENTITY,
  display_name VARCHAR(255) DEFAULT NULL,
  email VARCHAR(255) NOT NULL,
  password VARCHAR(255) DEFAULT NULL,
  provider VARCHAR(255) DEFAULT NULL,
  provider_id VARCHAR(255) DEFAULT NULL,
  username VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (username),
  UNIQUE (email)
);

-- user_management.password_reset_tokens definition

CREATE TABLE password_reset_tokens (
  id BIGINT NOT NULL IDENTITY,
  expiry_date TIMESTAMP NOT NULL,
  token VARCHAR(255) NOT NULL,
  used BOOLEAN NOT NULL,
  user_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (token),
  UNIQUE (user_id),
  CONSTRAINT FK_password_reset_tokens_user_id FOREIGN KEY (user_id) REFERENCES app_users (id)
);

-- user_management.revoked_tokens definition

CREATE TABLE revoked_tokens (
  jti VARCHAR(255) NOT NULL,
  expiry_time TIMESTAMP NOT NULL,
  PRIMARY KEY (jti)
);

-- user_management.user_active_tokens definition

CREATE TABLE user_active_tokens (
  jti VARCHAR(255) NOT NULL,
  expiry_time TIMESTAMP NOT NULL,
  user_id BIGINT NOT NULL,
  PRIMARY KEY (jti),
  KEY FK_user_active_tokens_user_id (user_id),
  CONSTRAINT FK_user_active_tokens_user_id FOREIGN KEY (user_id) REFERENCES app_users (id)
);

-- user_management.user_roles definition

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id INT NOT NULL,
  PRIMARY KEY (user_id,role_id),
  KEY FK_user_roles_role_id (role_id),
  CONSTRAINT FK_user_roles_app_users FOREIGN KEY (user_id) REFERENCES app_users (id),
  CONSTRAINT FK_user_roles_role_id FOREIGN KEY (role_id) REFERENCES app_roles (id)
);

-- user_management.user_addresses definition

CREATE TABLE user_addresses (
  id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  street VARCHAR(255) NOT NULL,
  city VARCHAR(255) NOT NULL,
  state VARCHAR(255) NOT NULL,
  zip_code VARCHAR(255) NOT NULL,
  country VARCHAR(255) NOT NULL,
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id),
  KEY FK_addresses_user_id (user_id),
  CONSTRAINT FK_addresses_user_id FOREIGN KEY (user_id) REFERENCES app_users (id)
);

