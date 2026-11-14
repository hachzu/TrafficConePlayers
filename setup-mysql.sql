-- Import in MySQL Workbench using a local administrator connection.
CREATE DATABASE IF NOT EXISTS trafficplayers_db CHARACTER SET utf8mb4;
USE trafficplayers_db;

CREATE TABLE IF NOT EXISTS `user` (
    user_id       INT NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login    DATETIME DEFAULT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_user_username (username)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS usage_session (
    session_id       INT NOT NULL AUTO_INCREMENT,
    user_id          INT NOT NULL,
    started_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at         DATETIME DEFAULT NULL,
    duration_seconds INT GENERATED ALWAYS AS (
        IF(ended_at IS NOT NULL, TIMESTAMPDIFF(SECOND, started_at, ended_at), 0)
    ) STORED,
    PRIMARY KEY (session_id),
    CONSTRAINT fk_session_user FOREIGN KEY (user_id)
        REFERENCES `user` (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS media (
    media_id        INT NOT NULL AUTO_INCREMENT,
    user_id         INT NOT NULL,
    title           VARCHAR(255) NOT NULL,
    media_type      ENUM('MP3', 'MP4', 'YOUTUBE') NOT NULL,
    source_location VARCHAR(500) NOT NULL,
    date_added      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (media_id),
    CONSTRAINT fk_media_user FOREIGN KEY (user_id)
        REFERENCES `user` (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS playlist (
    playlist_id   INT NOT NULL AUTO_INCREMENT,
    user_id       INT NOT NULL,
    playlist_name VARCHAR(100) NOT NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (playlist_id),
    CONSTRAINT fk_playlist_user FOREIGN KEY (user_id)
        REFERENCES `user` (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS achievement (
    achievement_id    INT NOT NULL AUTO_INCREMENT,
    achievement_name  VARCHAR(100) NOT NULL,
    description       TEXT,
    requirement_value INT NOT NULL DEFAULT 1,
    currency_reward   INT NOT NULL DEFAULT 0,
    PRIMARY KEY (achievement_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_achievement (
    user_id        INT NOT NULL,
    achievement_id INT NOT NULL,
    unlocked_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, achievement_id),
    CONSTRAINT fk_ua_user        FOREIGN KEY (user_id)
        REFERENCES `user` (user_id)              ON DELETE CASCADE,
    CONSTRAINT fk_ua_achievement FOREIGN KEY (achievement_id)
        REFERENCES achievement (achievement_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS skin (
    skin_id         INT NOT NULL AUTO_INCREMENT,
    skin_name       VARCHAR(100) NOT NULL,
    rarity          ENUM('COMMON','RARE','EPIC','LEGENDARY') NOT NULL DEFAULT 'COMMON',
    skin_asset_path VARCHAR(500) NOT NULL,
    PRIMARY KEY (skin_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_skin (
    user_id     INT NOT NULL,
    skin_id     INT NOT NULL,
    obtained_at DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_equipped TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, skin_id),
    CONSTRAINT fk_us_user FOREIGN KEY (user_id)
        REFERENCES `user` (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_us_skin FOREIGN KEY (skin_id)
        REFERENCES skin (skin_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS playlist_media (
    playlist_media_id INT NOT NULL AUTO_INCREMENT,
    playlist_id       INT NOT NULL,
    media_id          INT NOT NULL,
    position          INT NOT NULL DEFAULT 0,
    added_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (playlist_media_id),
    UNIQUE KEY uq_playlist_media (playlist_id, media_id),
    CONSTRAINT fk_pm_playlist FOREIGN KEY (playlist_id)
        REFERENCES playlist (playlist_id) ON DELETE CASCADE,
    CONSTRAINT fk_pm_media    FOREIGN KEY (media_id)
        REFERENCES media (media_id)       ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS gacha_transaction (
    gacha_id       INT NOT NULL AUTO_INCREMENT,
    user_id        INT NOT NULL,
    skin_id        INT NOT NULL,
    currency_spent INT NOT NULL DEFAULT 0,
    drawn_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (gacha_id),
    CONSTRAINT fk_gacha_user FOREIGN KEY (user_id)
        REFERENCES `user` (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_gacha_skin FOREIGN KEY (skin_id)
        REFERENCES skin (skin_id) ON DELETE CASCADE,
    CONSTRAINT chk_gacha_currency
        CHECK (currency_spent >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS currency_wallet (
    user_id    INT NOT NULL,
    balance    INT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id)
        REFERENCES `user` (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_wallet_balance
        CHECK (balance >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS currency_transaction (
    transaction_id     INT NOT NULL AUTO_INCREMENT,
    user_id            INT NOT NULL,
    amount             INT NOT NULL,
    transaction_type   ENUM('EARN','SPEND') NOT NULL,
    transaction_reason VARCHAR(255) NOT NULL,
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (transaction_id),
    CONSTRAINT fk_currency_user FOREIGN KEY (user_id)
        REFERENCES `user` (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_transaction_amount
        CHECK (amount > 0)
) ENGINE=InnoDB;
