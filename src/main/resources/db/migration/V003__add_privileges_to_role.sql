CREATE TABLE role_privileges
(
    role_id   BIGINT       NOT NULL,
    privilege VARCHAR(255) NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles (id)
);
