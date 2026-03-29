-- Initial schema created by Flyway.
--
-- UUID columns use binary(16), which is the default mapping for Hibernate 6/7 on MySQL/MariaDB.
-- If your existing schema uses a different type (e.g. char(36) or the native uuid type on
-- MariaDB 10.7+), adjust the column definitions here to match before running on a fresh database.
--
-- For databases that already have these tables (created by Hibernate's ddl-auto=update), Flyway
-- will skip this migration because baseline-on-migrate=true marks those databases at version 0.

CREATE TABLE users (
    id       binary(16)   NOT NULL,
    username varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT username UNIQUE (username)
) ENGINE = InnoDB;

CREATE TABLE variables (
    id      binary(16)   NOT NULL,
    user_id binary(16)   NOT NULL,
    name    varchar(255) NOT NULL,
    value   varchar(255),
    PRIMARY KEY (id),
    CONSTRAINT user_name UNIQUE (user_id, name),
    CONSTRAINT fk_variables_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;
