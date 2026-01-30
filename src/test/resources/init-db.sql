use appdb;

CREATE TABLE `users`
(
    `id`       int(11) NOT NULL AUTO_INCREMENT,
    `name`     varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `email`    varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
