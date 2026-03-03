USE jjoncketing;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE reservations;
TRUNCATE TABLE subscriptions;
TRUNCATE TABLE desserts;
TRUNCATE TABLE users;
TRUNCATE TABLE stores;
TRUNCATE TABLE notification_template;
SET FOREIGN_KEY_CHECKS = 1;

LOAD DATA INFILE '/var/lib/mysql-files/csv/stores.csv'
INTO TABLE stores
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
(store_id, name, latitude, longitude);

LOAD DATA INFILE '/var/lib/mysql-files/csv/users.csv'
INTO TABLE users
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
(user_id, name, email, password, push_token, deleted_dt, created_dt);

LOAD DATA INFILE '/var/lib/mysql-files/csv/desserts.csv'
INTO TABLE desserts
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
(dessert_id, store_id, name, price, inventory, purchase_limit, open_dt, open_status);

LOAD DATA INFILE '/var/lib/mysql-files/csv/reservations.csv'
INTO TABLE reservations
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
(reservation_id, user_id, dessert_id, count, total_price, reserve_status, created_dt);

LOAD DATA INFILE '/var/lib/mysql-files/csv/subscriptions.csv'
INTO TABLE subscriptions
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
(user_id, store_id, deleted_dt, created_dt);

LOAD DATA INFILE '/var/lib/mysql-files/csv/notification_template.csv'
INTO TABLE notification_template
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
(template_key, title, body, noti_type, url, created_dt);
