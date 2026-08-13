CREATE TABLE `user`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `firstname` VARCHAR(50) NULL,
    `lastname` VARCHAR(50) NULL,
    `phone_number` VARCHAR(15) NULL,
    `status` VARCHAR(20) NOT NULL,
    `created_at` TIMESTAMP NULL,
    `updated_at` TIMESTAMP NULL,
    `updated_by` BIGINT NULL,
    `created_by` BIGINT NULL
);
CREATE TABLE `role`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `role_name` VARCHAR(50) NOT NULL,
    `description` VARCHAR(255) NOT NULL
);
CREATE TABLE `user_role`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `role_id` BIGINT UNSIGNED NOT NULL,
    `created_at` TIMESTAMP NULL,
    `updated_at` TIMESTAMP NULL,
    `created_by` BIGINT NULL,
    `updated_by` BIGINT NULL
);
CREATE TABLE `user_verification`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `verification_code` VARCHAR(50) NOT NULL,
    `expired_at` TIMESTAMP NOT NULL,
    `channel` VARCHAR(20) NOT NULL,
    `verifi_at` TIMESTAMP NOT NULL,
    `receiver` VARCHAR(255) NOT NULL,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `created_at` TIMESTAMP NULL,
    `updated_at` TIMESTAMP NULL,
    `created_by` BIGINT NULL,
    `updated_by` BIGINT NULL
);
CREATE TABLE `user_profile`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `birthday` DATE NOT NULL,
    `gender` VARCHAR(10) NOT NULL,
    `address` VARCHAR(255) NOT NULL,
    `bio` VARCHAR(255) NOT NULL,
    `avartar` VARCHAR(255) NOT NULL,
    `company` VARCHAR(255) NOT NULL,
    `year_of_experience` TINYINT NOT NULL,
    `education_level` TINYINT NOT NULL,
    `linkedin` VARCHAR(255) NOT NULL,
    `facebook` VARCHAR(255) NOT NULL,
    `personal_website` VARCHAR(255) NOT NULL,
    `created_at` TIMESTAMP NULL,
    `updated_at` TIMESTAMP NULL,
    `created_by` BIGINT NULL,
    `updated_by` BIGINT NULL,
    `user_id` BIGINT UNSIGNED NOT NULL,
    UNIQUE KEY `uk_user_profile_user_id` (`user_id`)
);
CREATE TABLE `user_skill`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `skill_name` VARCHAR(255) NOT NULL,
    `proficiency_level` TINYINT NOT NULL,
    `year_of_experience` TINYINT NOT NULL,
    `created_at` TIMESTAMP NULL,
    `updated_at` TIMESTAMP NULL,
    `created_by` BIGINT NULL,
    `updated_by` BIGINT NULL,
    `profile_id` BIGINT UNSIGNED NOT NULL
);
CREATE TABLE `user_interest`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `interest_name` VARCHAR(255) NOT NULL,
    `interest_level` TINYINT NOT NULL,
    `created_at` TIMESTAMP NULL,
    `updated_at` TIMESTAMP NULL,
    `created_by` BIGINT NULL,
    `updated_by` BIGINT NULL,
    `profile_id` BIGINT UNSIGNED NOT NULL
);
ALTER TABLE
    `user_role` ADD CONSTRAINT `user_role_role_id_foreign` FOREIGN KEY(`role_id`) REFERENCES `role`(`id`);
ALTER TABLE
    `user_profile` ADD CONSTRAINT `user_profile_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `user`(`id`);
ALTER TABLE
    `user_verification` ADD CONSTRAINT `user_verification_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `user`(`id`);
ALTER TABLE
    `user_interest` ADD CONSTRAINT `user_interest_profile_id_foreign` FOREIGN KEY(`profile_id`) REFERENCES `user_profile`(`id`);
ALTER TABLE
    `user_role` ADD CONSTRAINT `user_role_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `user`(`id`);
ALTER TABLE
    `user_skill` ADD CONSTRAINT `user_skill_profile_id_foreign` FOREIGN KEY(`profile_id`) REFERENCES `user_profile`(`id`);