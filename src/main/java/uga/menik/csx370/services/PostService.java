package uga.menik.csx370.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;
import uga.menik.csx370.models.ExpandedPost;
import uga.menik.csx370.models.Comment;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;


@Service
public class PostService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void createPost(Long userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Post content cannot be empty.");
        }
        
        System.out.println("DEBUG: Creating post for user " + userId + " with content: " + content);
        
        String insertPostSql = "INSERT INTO post (user_id, content) VALUES (?, ?)";
        jdbcTemplate.update(insertPostSql, userId, content.trim());
        System.out.println("DEBUG: Post inserted successfully");

        Long postId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(content);
        Set<String> hashtags = new HashSet<>();

        while (matcher.find()) {
            hashtags.add(matcher.group(1).toLowerCase()); 
        }

        for (String tag : hashtags) {
            jdbcTemplate.update("INSERT IGNORE INTO hashtag (tag) VALUES (?)", tag);

            jdbcTemplate.update(
                    "INSERT INTO post_hashtag (post_id, hashtag_id) " +
                    "SELECT ?, hashtag_id FROM hashtag WHERE tag = ?",
                    postId, tag
            );
        }
    }

    /**
     * Gets posts from users that the logged-in user follows, ordered by most recent first.
     */
    public List<Post> getPostsFromFollowing(Long userId) {
    String sql = "SELECT p.post_id, p.content, p.created_at, " +
                 "u.userId, u.firstName, u.lastName, " +
                 "COALESCE(like_count.likes, 0) AS hearts_count, " +
                 "COALESCE(comment_count.comments, 0) AS comments_count, " +
                 "0 AS is_hearted, 0 AS is_bookmarked " +
                 "FROM post p " +
                 "LEFT JOIN user u ON p.user_id = u.userId " +  
                 "LEFT JOIN (SELECT post_id, COUNT(*) AS likes FROM `like` GROUP BY post_id) like_count ON p.post_id = like_count.post_id " +
                 "LEFT JOIN (SELECT post_id, COUNT(*) AS comments FROM comment GROUP BY post_id) comment_count ON p.post_id = comment_count.post_id " +
                 "ORDER BY p.created_at DESC";
    return executePostQuery(sql);
}


    /**
     * Gets posts from a specific user, ordered by most recent first.
     */
    public List<Post> getPostsByUser(Long userId, Long currentUserId) {
        String sql = "SELECT p.post_id, p.content, p.created_at, " +
                    "u.userId, u.firstName, u.lastName, " +
                    "COALESCE(like_count.likes, 0) as hearts_count, " +
                    "COALESCE(comment_count.comments, 0) as comments_count, " +
                    "CASE WHEN user_like.user_id IS NOT NULL THEN 1 ELSE 0 END as is_hearted, " +
                    "CASE WHEN user_bookmark.user_id IS NOT NULL THEN 1 ELSE 0 END as is_bookmarked " +
                    "FROM post p " +
                    "JOIN user u ON p.user_id = u.userId " +
                    "LEFT JOIN (SELECT post_id, COUNT(*) as likes FROM `like` GROUP BY post_id) like_count ON p.post_id = like_count.post_id " +
                    "LEFT JOIN (SELECT post_id, COUNT(*) as comments FROM comment GROUP BY post_id) comment_count ON p.post_id = comment_count.post_id " +
                    "LEFT JOIN (SELECT post_id, user_id FROM `like` WHERE user_id = ?) user_like ON p.post_id = user_like.post_id " +
                    "LEFT JOIN (SELECT post_id, user_id FROM bookmark WHERE user_id = ?) user_bookmark ON p.post_id = user_bookmark.post_id " +
                    "WHERE p.user_id = ? " +
                    "ORDER BY p.created_at DESC";

        return executePostQuery(sql, currentUserId, currentUserId, userId);
    }

    /**
     * Gets bookmarked posts for a user, ordered by most recent first.
     */
    public List<Post> getBookmarkedPosts(Long userId) {
        String sql = "SELECT p.post_id, p.content, p.created_at, " +
                    "u.userId, u.firstName, u.lastName, " +
                    "COALESCE(like_count.likes, 0) as hearts_count, " +
                    "COALESCE(comment_count.comments, 0) as comments_count, " +
                    "CASE WHEN user_like.user_id IS NOT NULL THEN 1 ELSE 0 END as is_hearted, " +
                    "CASE WHEN user_bookmark.user_id IS NOT NULL THEN 1 ELSE 0 END as is_bookmarked " +
                    "FROM post p " +
                    "JOIN user u ON p.user_id = u.userId " +
                    "JOIN bookmark b ON p.post_id = b.post_id " +
                    "LEFT JOIN (SELECT post_id, COUNT(*) as likes FROM `like` GROUP BY post_id) like_count ON p.post_id = like_count.post_id " +
                    "LEFT JOIN (SELECT post_id, COUNT(*) as comments FROM comment GROUP BY post_id) comment_count ON p.post_id = comment_count.post_id " +
                    "LEFT JOIN (SELECT post_id, user_id FROM `like` WHERE user_id = ?) user_like ON p.post_id = user_like.post_id " +
                    "LEFT JOIN (SELECT post_id, user_id FROM bookmark WHERE user_id = ?) user_bookmark ON p.post_id = user_bookmark.post_id " +
                    "WHERE b.user_id = ? " +
                    "ORDER BY p.created_at DESC";

        return executePostQuery(sql, userId, userId, userId);
    }

    /**
     * Gets posts by hashtags, ordered by most recent first.
     */
    public List<Post> getPostsByHashtags(List<String> hashtags, Long currentUserId) {
        if (hashtags.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.post_id, p.content, p.created_at, ");
        sql.append("u.userId, u.firstName, u.lastName, ");
        sql.append("COALESCE(like_count.likes, 0) as hearts_count, ");
        sql.append("COALESCE(comment_count.comments, 0) as comments_count, ");
        sql.append("CASE WHEN user_like.user_id IS NOT NULL THEN 1 ELSE 0 END as is_hearted, ");
        sql.append("CASE WHEN user_bookmark.user_id IS NOT NULL THEN 1 ELSE 0 END as is_bookmarked ");
        sql.append("FROM post p ");
        sql.append("JOIN user u ON p.user_id = u.userId ");
        sql.append("JOIN post_hashtag ph ON p.post_id = ph.post_id ");
        sql.append("JOIN hashtag h ON ph.hashtag_id = h.hashtag_id ");
        sql.append("LEFT JOIN (SELECT post_id, COUNT(*) as likes FROM `like` GROUP BY post_id) like_count ON p.post_id = like_count.post_id ");
        sql.append("LEFT JOIN (SELECT post_id, COUNT(*) as comments FROM comment GROUP BY post_id) comment_count ON p.post_id = comment_count.post_id ");
        sql.append("LEFT JOIN (SELECT post_id, user_id FROM `like` WHERE user_id = ?) user_like ON p.post_id = user_like.post_id ");
        sql.append("LEFT JOIN (SELECT post_id, user_id FROM bookmark WHERE user_id = ?) user_bookmark ON p.post_id = user_bookmark.post_id ");
        sql.append("WHERE h.tag IN (");
        
        for (int i = 0; i < hashtags.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
        }
        sql.append(") ");
        sql.append("GROUP BY p.post_id, p.content, p.created_at, u.userId, u.firstName, u.lastName ");
        sql.append("HAVING COUNT(DISTINCT h.tag) = ? ");
        sql.append("ORDER BY p.created_at DESC");

        return executePostQueryWithHashtags(sql.toString(), hashtags, currentUserId, currentUserId, hashtags.size());
    }

    /**
     * Helper method to execute post queries and return Post objects.
     */
    private List<Post> executePostQuery(String sql, Object... params) {
        List<Post> posts = new ArrayList<>();
        
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String postId = String.valueOf(rs.getLong("post_id"));
                    String content = rs.getString("content");
                    String postDate = formatDate(rs.getTimestamp("created_at"));
                    
                    String userId = String.valueOf(rs.getInt("userId"));
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
                    User user = new User(userId, firstName, lastName);
                    
                    int heartsCount = rs.getInt("hearts_count");
                    int commentsCount = rs.getInt("comments_count");
                    boolean isHearted = rs.getInt("is_hearted") == 1;
                    boolean isBookmarked = rs.getInt("is_bookmarked") == 1;
                    
                    posts.add(new Post(postId, content, postDate, user, heartsCount, commentsCount, isHearted, isBookmarked));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return posts;
    }

    /**
     * Helper method to execute post queries with hashtags.
     */
    private List<Post> executePostQueryWithHashtags(String sql, List<String> hashtags, Object... params) {
        List<Post> posts = new ArrayList<>();
        
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            for (Object param : params) {
                if (param instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> hashtagList = (List<String>) param;
                    for (String hashtag : hashtagList) {
                        stmt.setString(paramIndex++, hashtag);
                    }
                } else {
                    stmt.setObject(paramIndex++, param);
                }
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String postId = String.valueOf(rs.getLong("post_id"));
                    String content = rs.getString("content");
                    String postDate = formatDate(rs.getTimestamp("created_at"));
                    
                    String userId = String.valueOf(rs.getInt("userId"));
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
                    User user = new User(userId, firstName, lastName);
                    
                    int heartsCount = rs.getInt("hearts_count");
                    int commentsCount = rs.getInt("comments_count");
                    boolean isHearted = rs.getInt("is_hearted") == 1;
                    boolean isBookmarked = rs.getInt("is_bookmarked") == 1;
                    
                    posts.add(new Post(postId, content, postDate, user, heartsCount, commentsCount, isHearted, isBookmarked));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return posts;
    }

    /**
     * Gets a specific post with its comments, ordered from oldest to latest.
     */
    public ExpandedPost getPostWithComments(Long postId, Long currentUserId) {
        // First get the post details
        String postSql = "SELECT p.post_id, p.content, p.created_at, " +
                        "u.userId, u.firstName, u.lastName, " +
                        "COALESCE(like_count.likes, 0) as hearts_count, " +
                        "COALESCE(comment_count.comments, 0) as comments_count, " +
                        "CASE WHEN user_like.user_id IS NOT NULL THEN 1 ELSE 0 END as is_hearted, " +
                        "CASE WHEN user_bookmark.user_id IS NOT NULL THEN 1 ELSE 0 END as is_bookmarked " +
                        "FROM post p " +
                        "JOIN user u ON p.user_id = u.userId " +
                        "LEFT JOIN (SELECT post_id, COUNT(*) as likes FROM `like` GROUP BY post_id) like_count ON p.post_id = like_count.post_id " +
                        "LEFT JOIN (SELECT post_id, COUNT(*) as comments FROM comment GROUP BY post_id) comment_count ON p.post_id = comment_count.post_id " +
                        "LEFT JOIN (SELECT post_id, user_id FROM `like` WHERE user_id = ?) user_like ON p.post_id = user_like.post_id " +
                        "LEFT JOIN (SELECT post_id, user_id FROM bookmark WHERE user_id = ?) user_bookmark ON p.post_id = user_bookmark.post_id " +
                        "WHERE p.post_id = ?";

        Post post = null;
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(postSql)) {
            
            stmt.setLong(1, currentUserId);
            stmt.setLong(2, currentUserId);
            stmt.setLong(3, postId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String postIdStr = String.valueOf(rs.getLong("post_id"));
                    String content = rs.getString("content");
                    String postDate = formatDate(rs.getTimestamp("created_at"));
                    
                    String userId = String.valueOf(rs.getInt("userId"));
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
                    User user = new User(userId, firstName, lastName);
                    
                    int heartsCount = rs.getInt("hearts_count");
                    int commentsCount = rs.getInt("comments_count");
                    boolean isHearted = rs.getInt("is_hearted") == 1;
                    boolean isBookmarked = rs.getInt("is_bookmarked") == 1;
                    
                    post = new Post(postIdStr, content, postDate, user, heartsCount, commentsCount, isHearted, isBookmarked);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        if (post == null) {
            return null;
        }

        // Now get the comments for this post
        String commentsSql = "SELECT c.comment_id, c.content, c.created_at, " +
                           "u.userId, u.firstName, u.lastName " +
                           "FROM comment c " +
                           "JOIN user u ON c.user_id = u.userId " +
                           "WHERE c.post_id = ? " +
                           "ORDER BY c.created_at ASC";

        List<Comment> comments = new ArrayList<>();
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(commentsSql)) {
            
            stmt.setLong(1, postId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String commentId = String.valueOf(rs.getLong("comment_id"));
                    String content = rs.getString("content");
                    String commentDate = formatDate(rs.getTimestamp("created_at"));
                    
                    String userId = String.valueOf(rs.getInt("userId"));
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
                    User user = new User(userId, firstName, lastName);
                    
                    comments.add(new Comment(commentId, content, commentDate, user));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ExpandedPost(post.getPostId(), post.getContent(), post.getPostDate(), 
                               post.getUser(), post.getHeartsCount(), post.getCommentsCount(), 
                               post.getHearted(), post.isBookmarked(), comments);
    }

    /**
     * Adds or removes a like for a post by a user.
     */
    public void toggleLike(Long userId, Long postId, boolean isAdd) {
        if (isAdd) {
            String sql = "INSERT IGNORE INTO `like` (user_id, post_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, userId, postId);
        } else {
            String sql = "DELETE FROM `like` WHERE user_id = ? AND post_id = ?";
            jdbcTemplate.update(sql, userId, postId);
        }
    }

    /**
     * Adds or removes a bookmark for a post by a user.
     */
    public void toggleBookmark(Long userId, Long postId, boolean isAdd) {
        if (isAdd) {
            String sql = "INSERT IGNORE INTO bookmark (user_id, post_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, userId, postId);
        } else {
            String sql = "DELETE FROM bookmark WHERE user_id = ? AND post_id = ?";
            jdbcTemplate.update(sql, userId, postId);
        }
    }

    /**
     * Formats a timestamp to the required format: "Mar 07, 2025, 10:54 PM"
     */
    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return "Unknown";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.US);
        return formatter.format(new Date(timestamp.getTime()));
    }

}


