-- Отключаем проверку FK на время создания таблиц
SET REFERENTIAL_INTEGRITY FALSE;

-- USERS
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(512) NOT NULL UNIQUE
);

-- REQUESTS
CREATE TABLE IF NOT EXISTS requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(512) NOT NULL,
    requestor_id BIGINT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ITEMS
CREATE TABLE IF NOT EXISTS items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(512) NOT NULL,
    available BOOLEAN NOT NULL,
    owner_id BIGINT NOT NULL,
    request_id BIGINT
);

-- BOOKINGS
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    item_id BIGINT NOT NULL,
    booker_id BIGINT NOT NULL
);

-- COMMENTS
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    text VARCHAR(512) NOT NULL,
    item_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Включаем проверку FK и добавляем ключи после создания таблиц
ALTER TABLE requests ADD CONSTRAINT fk_requestor FOREIGN KEY (requestor_id) REFERENCES users(id);
ALTER TABLE items ADD CONSTRAINT fk_owner FOREIGN KEY (owner_id) REFERENCES users(id);
ALTER TABLE items ADD CONSTRAINT fk_request FOREIGN KEY (request_id) REFERENCES requests(id);
ALTER TABLE bookings ADD CONSTRAINT fk_item FOREIGN KEY (item_id) REFERENCES items(id);
ALTER TABLE bookings ADD CONSTRAINT fk_booker FOREIGN KEY (booker_id) REFERENCES users(id);
ALTER TABLE comments ADD CONSTRAINT fk_item_comment FOREIGN KEY (item_id) REFERENCES items(id);
ALTER TABLE comments ADD CONSTRAINT fk_author_comment FOREIGN KEY (author_id) REFERENCES users(id);
