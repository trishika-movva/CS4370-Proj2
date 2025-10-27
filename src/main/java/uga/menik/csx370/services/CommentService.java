package uga.menik.csx370.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import uga.menik.csx370.models.Comment;
import uga.menik.csx370.models.User;

@Service
public class CommentService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void addComment(Long userId, Long postId, String content) {
        if (content == null || content.trim().isEmpty())
            throw new IllegalArgumentException("Comment cannot be empty.");

        String sql = "INSERT INTO comment (user_id, post_id, content, created_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setLong(2, postId);
            stmt.setString(3, content.trim());
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();

            System.out.println("✅ Comment inserted successfully!");

        } catch (SQLException e) {
            System.err.println("❌ SQL Error inserting comment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to insert comment into database", e);
        }
    }

    public List<Comment> getCommentsForPost(Long postId) {
        String sql = "SELECT c.comment_id, c.content, c.created_at, " +
                     "u.userId, u.firstName, u.lastName " +
                     "FROM comment c JOIN user u ON c.user_id = u.userId " +
                     "WHERE c.post_id = ? ORDER BY c.created_at ASC";

        List<Comment> comments = new ArrayList<>();
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String commentId = String.valueOf(rs.getLong("comment_id"));
                    String text = rs.getString("content");
                    String date = new java.text.SimpleDateFormat("MMM dd, yyyy, hh:mm a")
                            .format(rs.getTimestamp("created_at"));
                    User user = new User(
                        String.valueOf(rs.getInt("userId")),
                        rs.getString("firstName"),
                        rs.getString("lastName"));
                    comments.add(new Comment(commentId, text, date, user));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }
}
