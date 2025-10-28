-- Create the database.
create database if not exists csx370_mb_platform;

-- Use the created database.
use csx370_mb_platform;

-- Create the user table.
create table if not exists user (
    userId int auto_increment,
    username varchar(255) not null,
    password varchar(255) not null,
    firstName varchar(255) not null,
    lastName varchar(255) not null,
    primary key (userId),
    unique (username),
    constraint userName_min_length check (char_length(trim(userName)) >= 2),
    constraint firstName_min_length check (char_length(trim(firstName)) >= 2),
    constraint lastName_min_length check (char_length(trim(lastName)) >= 2)
);

-- Posts
CREATE TABLE IF NOT EXISTS post (
  post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  content VARCHAR(280) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES user(userId) ON DELETE CASCADE,
  INDEX idx_post_user_created (user_id, created_at DESC),
  INDEX idx_post_created (created_at DESC)
);

-- Comments
CREATE TABLE IF NOT EXISTS comment (
  comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  post_id BIGINT NOT NULL,
  user_id INT NOT NULL,
  content VARCHAR(280) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE,
  CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES user(userId) ON DELETE CASCADE,
  INDEX idx_comment_post_created (post_id, created_at)
);

-- Follows
CREATE TABLE IF NOT EXISTS follow (
  follower_id INT NOT NULL,
  followee_id INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (follower_id, followee_id),
  CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) REFERENCES user(userId) ON DELETE CASCADE,
  CONSTRAINT fk_follow_followee FOREIGN KEY (followee_id) REFERENCES user(userId) ON DELETE CASCADE
);

-- Likes
CREATE TABLE IF NOT EXISTS `like` (
  user_id INT NOT NULL,
  post_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, post_id),
  CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES user(userId) ON DELETE CASCADE,
  CONSTRAINT fk_like_post FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE
);

-- Bookmarks
CREATE TABLE IF NOT EXISTS bookmark (
  user_id INT NOT NULL,
  post_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, post_id),
  CONSTRAINT fk_bookmark_user FOREIGN KEY (user_id) REFERENCES user(userId) ON DELETE CASCADE,
  CONSTRAINT fk_bookmark_post FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE
);

-- Hashtags
CREATE TABLE IF NOT EXISTS hashtag (
  hashtag_id INT AUTO_INCREMENT PRIMARY KEY,
  tag VARCHAR(64) NOT NULL UNIQUE
);

-- Post Hashtag
CREATE TABLE IF NOT EXISTS post_hashtag (
  post_id BIGINT NOT NULL,
  hashtag_id INT NOT NULL,
  PRIMARY KEY (post_id, hashtag_id),
  CONSTRAINT fk_ph_post FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE,
  CONSTRAINT fk_ph_hashtag FOREIGN KEY (hashtag_id) REFERENCES hashtag(hashtag_id) ON DELETE CASCADE
);
