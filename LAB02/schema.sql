CREATE DATABASE IF NOT EXISTS scm;
USE scm;

-- Identity Service owns users and password-reset data.
CREATE TABLE users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    about VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    email_verified TINYINT NOT NULL DEFAULT 0,
    enabled TINYINT NOT NULL DEFAULT 1,
    user_name VARCHAR(100) NOT NULL,
    password VARCHAR(255),
    phone_number VARCHAR(255),
    phone_verified TINYINT NOT NULL DEFAULT 0,
    profile_pic VARCHAR(255),
    provider ENUM('FACEBOOK','GITHUB','GOOGLE','SELF'),
    provider_user_id VARCHAR(255),
    PRIMARY KEY (user_id)
);

CREATE TABLE password_reset_token (
    token VARCHAR(255) NOT NULL,
    expiry_date DATETIME NOT NULL,
    user_user_id BIGINT NOT NULL,
    PRIMARY KEY (token),
    CONSTRAINT fk_reset_user FOREIGN KEY (user_user_id) REFERENCES users(user_id)
);

-- Contact Service owns contact records. user_user_id is an opaque reference
-- to Identity Service; no cross-service DB FK is created.
CREATE TABLE contact (
    id INT NOT NULL AUTO_INCREMENT,
    address VARCHAR(255),
    description VARCHAR(255),
    email VARCHAR(255),
    facebook_link VARCHAR(255),
    favorite TINYINT,
    linkedin_link VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    picture VARCHAR(255),
    website_link VARCHAR(255),
    user_user_id BIGINT NOT NULL,
    PRIMARY KEY (id)
);

-- Social Link Service owns social links. contact_id is an opaque reference
-- to Contact Service; no cross-service DB FK is created.
CREATE TABLE social_link (
    id INT NOT NULL AUTO_INCREMENT,
    link VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    contact_id INT NOT NULL,
    PRIMARY KEY (id)
);
