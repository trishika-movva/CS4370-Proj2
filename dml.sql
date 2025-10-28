-- ==============================================
-- DML STATEMENTS FOR MICROBLOGGING PLATFORM
-- ==============================================
-- All the SQL queries used in the web app, organized by feature

-- ==============================================
-- User Management
-- ==============================================

-- Register a new user
-- URL: /register (POST)
INSERT INTO user (username, password, firstName, lastName) VALUES (?, ?, ?, ?);

-- Login user
-- URL: /login (POST)
SELECT userId, username, password, firstName, lastName FROM user WHERE username = ?;

-- ==============================================
-- Post Management
-- ==============================================

-- Create a new post
-- URL: /home (POST)
INSERT INTO post (user_id, content, created_at) VALUES (?, ?, ?);

-- Get the ID of the post we just created
-- URL: /home (POST)
SELECT LAST_INSERT_ID();

-- Add hashtag to database (ignore if already exists)
-- URL: /home (POST)
INSERT INTO hashtag (tag) VALUES (?) ON DUPLICATE KEY UPDATE tag = tag;

-- Find hashtag ID by name
-- URL: /home (POST)
SELECT hashtag_id FROM hashtag WHERE tag = ?;

-- Link post to hashtag
-- URL: /home (POST)
INSERT INTO post_hashtag (post_id, hashtag_id) VALUES (?, ?);

-- ==============================================
-- Home Page
-- ==============================================

-- Show posts from people you follow
-- URL: / (GET)
SELECT p.post_id, p.content, p.created_at, 
       u.userId, u.firstName, u.lastName, 
       COALESCE(like_count.likes, 0) AS hearts_count, 
       COALESCE(comment_count.comments, 0) AS comments_count, 
       CASE WHEN user_like.user_id IS NOT NULL THEN 1 ELSE 0 END AS is_hearted, 
       CASE WHEN user_bookmark.user_id IS NOT NULL THEN 1 ELSE 0 END AS is_bookmarked 
FROM post p 
JOIN follow f ON p.user_id = f.followee_id 
JOIN user u ON p.user_id = u.userId  
LEFT JOIN (SELECT post_id, COUNT(*) AS likes FROM `like` GROUP BY post_id) like_count ON p.post_id = like_count.post_id 
LEFT JOIN (SELECT post_id, COUNT(*) AS comments FROM comment GROUP BY post_id) comment_count ON p.post_id = comment_count.post_id 
LEFT JOIN (SELECT post_id, user_id FROM `like` WHERE user_id = ?) user_like ON p.post_id = user_like.post_id 
LEFT JOIN (SELECT post_id, user_id FROM bookmark WHERE user_id = ?) user_bookmark ON p.post_id = user_bookmark.post_id 
WHERE f.follower_id = ? 
ORDER BY p.created_at DESC;

-- ==============================================
-- Profile Page
-- ==============================================

-- Show posts by a specific user
-- URL: /profile/{userId} (GET)
SELECT p.post_id, p.content, p.created_at, 
       u.userId, u.firstName, u.lastName, 
       COALESCE(like_count.likes, 0) AS hearts_count, 
       COALESCE(comment_count.comments, 0) AS comments_count, 
       CASE WHEN user_like.user_id IS NOT NULL THEN 1 ELSE 0 END AS is_hearted, 
       CASE WHEN user_bookmark.user_id IS NOT NULL THEN 1 ELSE 0 END AS is_bookmarked 
FROM post p 
JOIN user u ON p.user_id = u.userId 
LEFT JOIN (SELECT post_id, COUNT(*) AS likes FROM `like` GROUP BY post_id) like_count ON p.post_id = like_count.post_id 
LEFT JOIN (SELECT post_id, COUNT(*) AS comments FROM comment GROUP BY post_id) comment_count ON p.post_id = comment_count.post_id 
LEFT JOIN (SELECT post_id, user_id FROM `like` WHERE user_id = ?) user_like ON p.post_id = user_like.post_id 
LEFT JOIN (SELECT post_id, user_id FROM bookmark WHERE user_id = ?) user_bookmark ON p.post_id = user_bookmark.post_id 
WHERE p.user_id = ? 
ORDER BY p.created_at DESC;

-- ==============================================
-- Bookmarks Page
-- ==============================================

-- Show posts you've bookmarked
-- URL: /bookmarks (GET)
SELECT p.post_id, p.content, p.created_at, 
       u.userId, u.firstName, u.lastName, 
       COALESCE(like_count.likes, 0) AS hearts_count, 
       COALESCE(comment_count.comments, 0) AS comments_count, 
       CASE WHEN user_like.user_id IS NOT NULL THEN 1 ELSE 0 END AS is_hearted, 
       CASE WHEN user_bookmark.user_id IS NOT NULL THEN 1 ELSE 0 END AS is_bookmarked 
FROM post p 
JOIN bookmark b ON p.post_id = b.post_id 
JOIN user u ON p.user_id = u.userId 
LEFT JOIN (SELECT post_id, COUNT(*) AS likes FROM `like` GROUP BY post_id) like_count ON p.post_id = like_count.post_id 
LEFT JOIN (SELECT post_id, COUNT(*) AS comments FROM comment GROUP BY post_id) comment_count ON p.post_id = comment_count.post_id 
LEFT JOIN (SELECT post_id, user_id FROM `like` WHERE user_id = ?) user_like ON p.post_id = user_like.post_id 
LEFT JOIN (SELECT post_id, user_id FROM bookmark WHERE user_id = ?) user_bookmark ON p.post_id = user_bookmark.post_id 
WHERE b.user_id = ? 
ORDER BY p.created_at DESC;

-- ==============================================
-- Hashtag Search
-- ==============================================

-- Find posts with specific hashtags (any of them)
-- URL: /hashtagsearch?hashtags=#tag1 #tag2 (GET)
SELECT p.post_id, p.content, p.created_at, 
       u.userId, u.firstName, u.lastName, 
       COALESCE(like_count.likes, 0) AS hearts_count, 
       COALESCE(comment_count.comments, 0) AS comments_count, 
       CASE WHEN user_like.user_id IS NOT NULL THEN 1 ELSE 0 END AS is_hearted, 
       CASE WHEN user_bookmark.user_id IS NOT NULL THEN 1 ELSE 0 END AS is_bookmarked 
FROM post p 
JOIN user u ON p.user_id = u.userId 
JOIN post_hashtag ph ON p.post_id = ph.post_id 
JOIN hashtag h ON ph.hashtag_id = h.hashtag_id 
LEFT JOIN (SELECT post_id, COUNT(*) AS likes FROM `like` GROUP BY post_id) like_count ON p.post_id = like_count.post_id 
LEFT JOIN (SELECT post_id, COUNT(*) AS comments FROM comment GROUP BY post_id) comment_count ON p.post_id = comment_count.post_id 
LEFT JOIN (SELECT post_id, user_id FROM `like` WHERE user_id = ?) user_like ON p.post_id = user_like.post_id 
LEFT JOIN (SELECT post_id, user_id FROM bookmark WHERE user_id = ?) user_bookmark ON p.post_id = user_bookmark.post_id 
WHERE h.tag IN (?, ?, ?, ?, ?) 
GROUP BY p.post_id, p.content, p.created_at, u.userId, u.firstName, u.lastName 
ORDER BY p.created_at DESC;

-- ==============================================
-- People Page
-- ==============================================

-- Show all users you can follow (with follow status and last post time)
-- URL: /people (GET)
SELECT u.userId, u.firstName, u.lastName, 
       COALESCE(DATE_FORMAT(MAX(p.created_at), '%b %d, %Y, %h:%i %p'), 'Unknown') AS last_post_time, 
       CASE WHEN EXISTS ( 
         SELECT 1 FROM follow f 
          WHERE f.follower_id = ? AND f.followee_id = u.userId 
       ) THEN 1 ELSE 0 END AS is_followed 
FROM user u 
LEFT JOIN post p ON p.user_id = u.userId 
WHERE u.userId <> ? 
GROUP BY u.userId, u.firstName, u.lastName 
ORDER BY COALESCE(MAX(p.created_at), TIMESTAMP('1970-01-01 00:00:00')) DESC;

-- ==============================================
-- Follow/Unfollow
-- ==============================================

-- Follow someone
-- URL: /people/{userId}/follow (POST)
INSERT INTO follow (follower_id, followee_id, created_at) VALUES (?, ?, ?);

-- Unfollow someone
-- URL: /people/{userId}/unfollow (POST)
DELETE FROM follow WHERE follower_id = ? AND followee_id = ?;

-- ==============================================
-- Like/Unlike
-- ==============================================

-- Like a post
-- URL: /post/{postId}/heart/true (GET)
INSERT INTO `like` (user_id, post_id, created_at) VALUES (?, ?, ?);

-- Unlike a post
-- URL: /post/{postId}/heart/false (GET)
DELETE FROM `like` WHERE user_id = ? AND post_id = ?;

-- ==============================================
-- Bookmark/Unbookmark
-- ==============================================

-- Bookmark a post
-- URL: /post/{postId}/bookmark/true (GET)
INSERT INTO bookmark (user_id, post_id, created_at) VALUES (?, ?, ?);

-- Unbookmark a post
-- URL: /post/{postId}/bookmark/false (GET)
DELETE FROM bookmark WHERE user_id = ? AND post_id = ?;

-- ==============================================
-- Comments
-- ==============================================

-- Add a comment to a post
-- URL: /post/{postId}/comment (POST)
INSERT INTO comment (user_id, post_id, content, created_at) VALUES (?, ?, ?, ?);

-- Get all comments for a post (oldest first)
-- URL: /post/{postId} (GET)
SELECT c.comment_id, c.content, c.created_at, 
       u.userId, u.firstName, u.lastName 
FROM comment c 
JOIN user u ON c.user_id = u.userId 
WHERE c.post_id = ? 
ORDER BY c.created_at ASC;

-- ==============================================
-- Individual Post Page
-- ==============================================

-- Show a single post with all its details
-- URL: /post/{postId} (GET)
SELECT p.post_id, p.content, p.created_at, 
       u.userId, u.firstName, u.lastName, 
       COALESCE(like_count.likes, 0) AS hearts_count, 
       COALESCE(comment_count.comments, 0) AS comments_count, 
       CASE WHEN user_like.user_id IS NOT NULL THEN 1 ELSE 0 END AS is_hearted, 
       CASE WHEN user_bookmark.user_id IS NOT NULL THEN 1 ELSE 0 END AS is_bookmarked 
FROM post p 
JOIN user u ON p.user_id = u.userId 
LEFT JOIN (SELECT post_id, COUNT(*) AS likes FROM `like` GROUP BY post_id) like_count ON p.post_id = like_count.post_id 
LEFT JOIN (SELECT post_id, COUNT(*) AS comments FROM comment GROUP BY post_id) comment_count ON p.post_id = comment_count.post_id 
LEFT JOIN (SELECT post_id, user_id FROM `like` WHERE user_id = ?) user_like ON p.post_id = user_like.post_id 
LEFT JOIN (SELECT post_id, user_id FROM bookmark WHERE user_id = ?) user_bookmark ON p.post_id = user_bookmark.post_id 
WHERE p.post_id = ?;

-- ==============================================
-- Utility Queries
-- ==============================================

-- Check if username exists
-- URL: /login (POST)
SELECT COUNT(*) FROM user WHERE username = ?;

-- Get user info by ID
-- URL: Various pages
SELECT userId, username, firstName, lastName FROM user WHERE userId = ?;

-- Count total posts
-- URL: Various pages
SELECT COUNT(*) FROM post;

-- Count total users
-- URL: Various pages
SELECT COUNT(*) FROM user;

-- Count total comments
-- URL: Various pages
SELECT COUNT(*) FROM comment;

-- ==============================================
-- Debug Queries
-- ==============================================

-- See all users
-- URL: Database inspection
SELECT userId, username, firstName, lastName, created_at FROM user ORDER BY userId;

-- See all posts
-- URL: Database inspection
SELECT post_id, user_id, content, created_at FROM post ORDER BY created_at DESC;

-- See all follows
-- URL: Database inspection
SELECT follower_id, followee_id, created_at FROM follow ORDER BY created_at DESC;

-- See all likes
-- URL: Database inspection
SELECT user_id, post_id, created_at FROM `like` ORDER BY created_at DESC;

-- See all bookmarks
-- URL: Database inspection
SELECT user_id, post_id, created_at FROM bookmark ORDER BY created_at DESC;

-- See all comments
-- URL: Database inspection
SELECT comment_id, post_id, user_id, content, created_at FROM comment ORDER BY created_at DESC;

-- See all hashtags
-- URL: Database inspection
SELECT hashtag_id, tag FROM hashtag ORDER BY tag;

-- See all post-hashtag links
-- URL: Database inspection
SELECT post_id, hashtag_id FROM post_hashtag ORDER BY post_id, hashtag_id;
