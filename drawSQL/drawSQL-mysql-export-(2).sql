CREATE TABLE `oauth2_clients`(
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `client_id` VARCHAR(100) NOT NULL,
    `client_secret` VARCHAR(255) NULL,
    `client_name` VARCHAR(200) NOT NULL,
    `description` TEXT NULL,
    `require_authorization_consent` TINYINT NULL,
    `require_proof_key` TINYINT NULL,
    `access_token_time_to_live` INT NULL,
    `refresh_token_time_to_live` INT NULL,
    `is_active` TINYINT NULL DEFAULT 1,
    `created_by` BIGINT NULL,
    `created_at` TIMESTAMP NULL,
    `updated_at` TIMESTAMP NULL
);
ALTER TABLE
    `oauth2_clients` ADD UNIQUE `oauth2_clients_client_id_unique`(`client_id`);
ALTER TABLE
    `oauth2_clients` ADD INDEX `oauth2_clients_client_name_index`(`client_name`);
ALTER TABLE
    `oauth2_clients` ADD INDEX `oauth2_clients_is_active_index`(`is_active`);
ALTER TABLE
    `oauth2_clients` ADD INDEX `oauth2_clients_created_by_index`(`created_by`);
CREATE TABLE `oauth2_client_auth_methods`(
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `client_id` BIGINT NOT NULL,
    `auth_method` VARCHAR(50) NOT NULL
);
ALTER TABLE
    `oauth2_client_auth_methods` ADD INDEX `oauth2_client_auth_methods_client_id_index`(`client_id`);
ALTER TABLE
    `oauth2_client_auth_methods` ADD INDEX `oauth2_client_auth_methods_auth_method_index`(`auth_method`);
CREATE TABLE `oauth2_client_grant_types`(
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `client_id` BIGINT NOT NULL,
    `grant_type` VARCHAR(50) NOT NULL
);
ALTER TABLE
    `oauth2_client_grant_types` ADD INDEX `oauth2_client_grant_types_client_id_index`(`client_id`);
ALTER TABLE
    `oauth2_client_grant_types` ADD INDEX `oauth2_client_grant_types_grant_type_index`(`grant_type`);
CREATE TABLE `oauth2_client_redirect_uris`(
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `client_id` BIGINT NOT NULL,
    `redirect_uri` VARCHAR(500) NOT NULL
);
ALTER TABLE
    `oauth2_client_redirect_uris` ADD INDEX `oauth2_client_redirect_uris_client_id_index`(`client_id`);
CREATE TABLE `oauth2_client_scopes`(
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `client_id` BIGINT NOT NULL,
    `scope` VARCHAR(100) NOT NULL
);
ALTER TABLE
    `oauth2_client_scopes` ADD INDEX `oauth2_client_scopes_client_id_index`(`client_id`);
ALTER TABLE
    `oauth2_client_scopes` ADD INDEX `oauth2_client_scopes_scope_index`(`scope`);
ALTER TABLE
    `oauth2_client_auth_methods` ADD CONSTRAINT `oauth2_client_auth_methods_client_id_foreign` FOREIGN KEY(`client_id`) REFERENCES `oauth2_clients`(`id`);
ALTER TABLE
    `oauth2_client_grant_types` ADD CONSTRAINT `oauth2_client_grant_types_client_id_foreign` FOREIGN KEY(`client_id`) REFERENCES `oauth2_clients`(`id`);
ALTER TABLE
    `oauth2_client_scopes` ADD CONSTRAINT `oauth2_client_scopes_client_id_foreign` FOREIGN KEY(`client_id`) REFERENCES `oauth2_clients`(`id`);
ALTER TABLE
    `oauth2_client_redirect_uris` ADD CONSTRAINT `oauth2_client_redirect_uris_client_id_foreign` FOREIGN KEY(`client_id`) REFERENCES `oauth2_clients`(`id`);