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

-- Post ↔ Hashtag
CREATE TABLE IF NOT EXISTS post_hashtag (
  post_id BIGINT NOT NULL,
  hashtag_id INT NOT NULL,
  PRIMARY KEY (post_id, hashtag_id),
  CONSTRAINT fk_ph_post FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE,
  CONSTRAINT fk_ph_hashtag FOREIGN KEY (hashtag_id) REFERENCES hashtag(hashtag_id) ON DELETE CASCADE
);

-- ==============================================
-- SAMPLE DATA FOR DEMONSTRATION
-- ==============================================

-- Insert sample users
INSERT INTO user (username, password, firstName, lastName) VALUES
('alice_johnson', '$2a$10$7YjSwjDXsVAFhWQS0/G4xeDrUr3N7GplbRqeaoyiuxU6RfCNbHYwe', 'Alice', 'Johnson'),
('bob_smith', '$2a$10$/if9pEhz20pnoumnZf.wpuqWPdQ47GMXdqoMYO7zoMb5wR1p5abhW', 'Bob', 'Smith'),
('charlie_brown', '$2a$10$sq4kIxMvPRLNmlIN3FlpzO3wi8f4HgYipCXWu7L43BvsTDE05ngeq', 'Charlie', 'Brown'),
('diana_prince', '$2a$10$/GwzCdmyZPG1HQsd1giKrurPRtoSYfo8Jsz7hy/ApcXd7NKDYYQy', 'Diana', 'Prince'),
('eve_wilson', '$2a$10$F7hvx0lIUNpYlj5YoYY3.u5l/hVPqO4Yi254YXYp1pfn6S/RUYoey', 'Eve', 'Wilson');

-- Insert sample posts
INSERT INTO post (user_id, content, created_at) VALUES
(1, 'Just finished my morning coffee! ☕ #coffee #morning #productivity', '2025-01-15 08:30:00'),
(2, 'Beautiful sunset today! 🌅 #sunset #nature #photography', '2025-01-15 18:45:00'),
(3, 'Working on a new project. Excited about the possibilities! #coding #development #tech', '2025-01-15 14:20:00'),
(1, 'Weekend vibes! Time to relax and recharge 🏖️ #weekend #relaxation #selfcare', '2025-01-14 16:00:00'),
(4, 'Amazing workout session today! 💪 #fitness #health #motivation', '2025-01-14 19:30:00'),
(2, 'Trying out a new recipe for dinner tonight 🍝 #cooking #food #recipe', '2025-01-14 17:15:00'),
(5, 'Just read an incredible book! Highly recommend it 📚 #books #reading #recommendation', '2025-01-13 21:00:00'),
(3, 'Team meeting went great! Great collaboration today 👥 #teamwork #collaboration #work', '2025-01-13 15:30:00'),
(1, 'Rainy day perfect for staying indoors and coding 🌧️ #rain #coding #indoor', '2025-01-13 11:00:00'),
(4, 'Morning run completed! 5 miles done ✅ #running #fitness #morning', '2025-01-12 07:00:00');

-- Insert sample hashtags
INSERT INTO hashtag (tag) VALUES
('coffee'), ('morning'), ('productivity'), ('sunset'), ('nature'), ('photography'),
('coding'), ('development'), ('tech'), ('weekend'), ('relaxation'), ('selfcare'),
('fitness'), ('health'), ('motivation'), ('cooking'), ('food'), ('recipe'),
('books'), ('reading'), ('recommendation'), ('teamwork'), ('collaboration'), ('work'),
('rain'), ('indoor'), ('running');

-- Link posts to hashtags
INSERT INTO post_hashtag (post_id, hashtag_id) VALUES
-- Post 1: coffee, morning, productivity
(1, 1), (1, 2), (1, 3),
-- Post 2: sunset, nature, photography
(2, 4), (2, 5), (2, 6),
-- Post 3: coding, development, tech
(3, 7), (3, 8), (3, 9),
-- Post 4: weekend, relaxation, selfcare
(4, 10), (4, 11), (4, 12),
-- Post 5: fitness, health, motivation
(5, 13), (5, 14), (5, 15),
-- Post 6: cooking, food, recipe
(6, 16), (6, 17), (6, 18),
-- Post 7: books, reading, recommendation
(7, 19), (7, 20), (7, 21),
-- Post 8: teamwork, collaboration, work
(8, 22), (8, 23), (8, 24),
-- Post 9: rain, coding, indoor
(9, 25), (9, 7), (9, 26),
-- Post 10: running, fitness, morning
(10, 27), (10, 13), (10, 2);

-- Insert sample follows (users following each other)
INSERT INTO follow (follower_id, followee_id, created_at) VALUES
(1, 2, '2025-01-10 10:00:00'),  -- Alice follows Bob
(1, 3, '2025-01-10 10:30:00'),  -- Alice follows Charlie
(1, 4, '2025-01-11 09:00:00'),  -- Alice follows Diana
(2, 1, '2025-01-10 11:00:00'),  -- Bob follows Alice
(2, 5, '2025-01-11 14:00:00'),  -- Bob follows Eve
(3, 1, '2025-01-10 12:00:00'),  -- Charlie follows Alice
(3, 2, '2025-01-11 08:00:00'),  -- Charlie follows Bob
(4, 1, '2025-01-11 10:00:00'),  -- Diana follows Alice
(4, 3, '2025-01-12 16:00:00'),  -- Diana follows Charlie
(5, 1, '2025-01-12 09:00:00'),  -- Eve follows Alice
(5, 2, '2025-01-12 11:00:00');  -- Eve follows Bob

-- Insert sample likes
INSERT INTO `like` (user_id, post_id, created_at) VALUES
(2, 1, '2025-01-15 09:00:00'),  -- Bob likes Alice's coffee post
(3, 1, '2025-01-15 09:15:00'),  -- Charlie likes Alice's coffee post
(4, 1, '2025-01-15 09:30:00'),  -- Diana likes Alice's coffee post
(1, 2, '2025-01-15 19:00:00'),  -- Alice likes Bob's sunset post
(3, 2, '2025-01-15 19:15:00'),  -- Charlie likes Bob's sunset post
(5, 2, '2025-01-15 19:30:00'),  -- Eve likes Bob's sunset post
(1, 3, '2025-01-15 15:00:00'),  -- Alice likes Charlie's coding post
(2, 3, '2025-01-15 15:15:00'),  -- Bob likes Charlie's coding post
(4, 3, '2025-01-15 15:30:00'),  -- Diana likes Charlie's coding post
(2, 4, '2025-01-14 17:00:00'),  -- Bob likes Alice's weekend post
(3, 4, '2025-01-14 17:15:00'),  -- Charlie likes Alice's weekend post
(1, 5, '2025-01-14 20:00:00'),  -- Alice likes Diana's workout post
(2, 5, '2025-01-14 20:15:00'),  -- Bob likes Diana's workout post
(5, 5, '2025-01-14 20:30:00'),  -- Eve likes Diana's workout post
(1, 6, '2025-01-14 18:00:00'),  -- Alice likes Bob's cooking post
(3, 6, '2025-01-14 18:15:00'),  -- Charlie likes Bob's cooking post
(4, 6, '2025-01-14 18:30:00'),  -- Diana likes Bob's cooking post
(1, 7, '2025-01-13 22:00:00'),  -- Alice likes Eve's book post
(2, 7, '2025-01-13 22:15:00'),  -- Bob likes Eve's book post
(3, 7, '2025-01-13 22:30:00'),  -- Charlie likes Eve's book post
(1, 8, '2025-01-13 16:00:00'),  -- Alice likes Charlie's team post
(2, 8, '2025-01-13 16:15:00'),  -- Bob likes Charlie's team post
(4, 8, '2025-01-13 16:30:00'),  -- Diana likes Charlie's team post
(2, 9, '2025-01-13 12:00:00'),  -- Bob likes Alice's rain post
(3, 9, '2025-01-13 12:15:00'),  -- Charlie likes Alice's rain post
(5, 9, '2025-01-13 12:30:00'),  -- Eve likes Alice's rain post
(1, 10, '2025-01-12 08:00:00'), -- Alice likes Diana's running post
(2, 10, '2025-01-12 08:15:00'), -- Bob likes Diana's running post
(3, 10, '2025-01-12 08:30:00'); -- Charlie likes Diana's running post

-- Insert sample bookmarks
INSERT INTO bookmark (user_id, post_id, created_at) VALUES
(1, 2, '2025-01-15 20:00:00'),  -- Alice bookmarks Bob's sunset post
(1, 7, '2025-01-13 23:00:00'),  -- Alice bookmarks Eve's book post
(2, 3, '2025-01-15 16:00:00'),  -- Bob bookmarks Charlie's coding post
(2, 5, '2025-01-14 21:00:00'),  -- Bob bookmarks Diana's workout post
(3, 1, '2025-01-15 10:00:00'),  -- Charlie bookmarks Alice's coffee post
(3, 6, '2025-01-14 19:00:00'),  -- Charlie bookmarks Bob's cooking post
(4, 2, '2025-01-15 21:00:00'),  -- Diana bookmarks Bob's sunset post
(4, 8, '2025-01-13 17:00:00'),  -- Diana bookmarks Charlie's team post
(5, 1, '2025-01-15 11:00:00'),  -- Eve bookmarks Alice's coffee post
(5, 3, '2025-01-15 17:00:00');  -- Eve bookmarks Charlie's coding post

-- Insert sample comments
INSERT INTO comment (post_id, user_id, content, created_at) VALUES
(1, 2, 'Love my morning coffee too! ☕', '2025-01-15 08:45:00'),
(1, 3, 'Coffee is life! What brand do you use?', '2025-01-15 09:00:00'),
(2, 1, 'Stunning photo! 📸', '2025-01-15 19:00:00'),
(2, 3, 'The colors are amazing!', '2025-01-15 19:15:00'),
(3, 1, 'What kind of project are you working on?', '2025-01-15 14:45:00'),
(3, 2, 'Sounds exciting! Keep us updated!', '2025-01-15 15:00:00'),
(4, 2, 'Enjoy your weekend! 😊', '2025-01-14 16:30:00'),
(4, 3, 'You deserve it!', '2025-01-14 16:45:00'),
(5, 1, 'Great job! 💪', '2025-01-14 20:00:00'),
(5, 2, 'Inspiring! What was your workout?', '2025-01-14 20:15:00'),
(6, 1, 'What recipe are you trying?', '2025-01-14 17:45:00'),
(6, 3, 'Hope it turns out delicious!', '2025-01-14 18:00:00'),
(7, 1, 'What book was it? I need recommendations!', '2025-01-13 21:30:00'),
(7, 2, 'Always looking for good books!', '2025-01-13 21:45:00'),
(8, 1, 'Great to hear the meeting went well!', '2025-01-13 16:00:00'),
(8, 2, 'Teamwork makes the dream work!', '2025-01-13 16:15:00'),
(9, 2, 'Perfect weather for coding!', '2025-01-13 11:30:00'),
(9, 3, 'Rainy days are the best for productivity!', '2025-01-13 11:45:00'),
(10, 1, 'Amazing! I need to start running too!', '2025-01-12 08:00:00'),
(10, 2, '5 miles is impressive! 🏃‍♀️', '2025-01-12 08:15:00');
