CREATE TABLE IF NOT EXISTS `users` (
                         `user_id` bigint NOT NULL AUTO_INCREMENT,
                         `created_at` datetime(6) DEFAULT NULL,
                         `email` varchar(255) DEFAULT NULL,
                         `name` varchar(255) DEFAULT NULL,
                         `role` varchar(255) NOT NULL,
                         `updated_at` datetime(6) DEFAULT NULL,
                         `username` varchar(255) NOT NULL,
                         PRIMARY KEY (`user_id`),
                         UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`),
                         UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
);

CREATE TABLE IF NOT EXISTS `stocks` (
                          `stock_code` varchar(10) NOT NULL,
                          `logo_url` text,
                          `market_type` varchar(255) DEFAULT NULL,
                          `stock_name` varchar(255) DEFAULT NULL,
                          PRIMARY KEY (`stock_code`)
);

CREATE TABLE IF NOT EXISTS `accounts` (
                            `user_id` bigint NOT NULL,
                            `balance` decimal(20,2) DEFAULT '0.00',
                            `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                            `account_id` bigint NOT NULL AUTO_INCREMENT,
                            PRIMARY KEY (`user_id`),
                            UNIQUE KEY `account_id` (`account_id`),
                            CONSTRAINT `FKnjuop33mo69pd79ctplkck40n` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);

CREATE TABLE IF NOT EXISTS `exchange` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `base_code` varchar(255) NOT NULL,
                            `date` date NOT NULL,
                            `rate` double NOT NULL,
                            `target_code` varchar(255) NOT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `UKcl5fj0iub9cmxmgxqx83uov07` (`base_code`,`target_code`,`date`)
);

CREATE TABLE IF NOT EXISTS `market_index` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `change_amount` double DEFAULT NULL,
                                `change_direction` varchar(255) DEFAULT NULL,
                                `change_rate` double DEFAULT NULL,
                                `created_at` datetime(6) DEFAULT NULL,
                                `current_value` double DEFAULT NULL,
                                `date` date DEFAULT NULL,
                                `index_name` varchar(255) DEFAULT NULL,
                                PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `orders` (
                          `order_id` bigint NOT NULL AUTO_INCREMENT,
                          `order_type` varchar(10) DEFAULT NULL,
                          `order_price` decimal(20,2) DEFAULT NULL,
                          `status` varchar(20) DEFAULT NULL,
                          `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                          `field` varchar(255) DEFAULT NULL,
                          `user_id` bigint NOT NULL,
                          `price` decimal(20,2) NOT NULL,
                          `quantity` int NOT NULL,
                          `stock_code` varchar(10) NOT NULL,
                          PRIMARY KEY (`order_id`),
                          KEY `FKh4x4bjhsmjrk2jqereojpnroc` (`stock_code`),
                          KEY `FK32ql8ubntj5uh44ph9659tiih` (`user_id`),
                          CONSTRAINT `FK32ql8ubntj5uh44ph9659tiih` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
                          CONSTRAINT `FKh4x4bjhsmjrk2jqereojpnroc` FOREIGN KEY (`stock_code`) REFERENCES `stocks` (`stock_code`)
);

CREATE TABLE IF NOT EXISTS `holdings` (
                            `holdings_id` bigint NOT NULL AUTO_INCREMENT,
                            `quantity` int DEFAULT NULL,
                            `avg_buy_price` decimal(20,2) DEFAULT NULL,
                            `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                            `stock_code` varchar(10) DEFAULT NULL,
                            `user_id` bigint NOT NULL,
                            PRIMARY KEY (`holdings_id`),
                            KEY `FKf0pyvp8vemrxnxu9h244lv916` (`stock_code`),
                            KEY `FKo0m56qvi5yyl5ikolvm7ih20o` (`user_id`),
                            CONSTRAINT `FKf0pyvp8vemrxnxu9h244lv916` FOREIGN KEY (`stock_code`) REFERENCES `stocks` (`stock_code`),
                            CONSTRAINT `FKo0m56qvi5yyl5ikolvm7ih20o` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);

CREATE TABLE IF NOT EXISTS `refresh_entity` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `expiration` varchar(255) DEFAULT NULL,
                                  `refresh` varchar(255) DEFAULT NULL,
                                  `username` varchar(255) DEFAULT NULL,
                                  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `stock_detail` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `close_price` decimal(38,2) DEFAULT NULL,
                                `created_at` datetime(6) DEFAULT NULL,
                                `high_price` decimal(38,2) DEFAULT NULL,
                                `low_price` decimal(38,2) DEFAULT NULL,
                                `open_price` decimal(38,2) DEFAULT NULL,
                                `prev_close_price` decimal(38,2) DEFAULT NULL,
                                `price_diff` decimal(38,2) DEFAULT NULL,
                                `price_rate` decimal(38,2) DEFAULT NULL,
                                `price_sign` int DEFAULT NULL,
                                `stock_code` varchar(20) DEFAULT NULL,
                                `volume` bigint DEFAULT NULL,
                                PRIMARY KEY (`id`),
                                KEY `FK9bqvlrs8vmf8ug9cwa3a7r1dp` (`stock_code`)
);

CREATE TABLE IF NOT EXISTS `test_entity` (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `name` varchar(255) DEFAULT NULL,
                               PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `trade` (
                         `trade_id` bigint NOT NULL AUTO_INCREMENT,
                         `trade_type` varchar(10) DEFAULT NULL,
                         `trade_price` decimal(20,2) DEFAULT NULL,
                         `trade_qty` int DEFAULT NULL,
                         `quantity` int DEFAULT NULL,
                         `sell_order_id` bigint DEFAULT NULL,
                         `buy_order_id` bigint DEFAULT NULL,
                         `executed_at` datetime(6) DEFAULT NULL,
                         `executed_price` decimal(20,2) NOT NULL,
                         `executed_quantity` int NOT NULL,
                         `order_id` bigint DEFAULT NULL,
                         `order_type` enum('BUY','SELL') NOT NULL,
                         PRIMARY KEY (`trade_id`),
                         KEY `trade_ibfk_2` (`buy_order_id`),
                         KEY `trade_ibfk_1` (`sell_order_id`),
                         KEY `FKbag1x46ksbbtcea85ji48fn77` (`order_id`),
                         CONSTRAINT `FKbag1x46ksbbtcea85ji48fn77` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`)
);