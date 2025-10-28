package uga.menik.csx370.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class TrendingService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // DTOs
    public static class TagTrend {
        public final String tag;
        public final long usageCount;
        public TagTrend(String tag, long usageCount) {
            this.tag = tag;
            this.usageCount = usageCount;
        }
    }

    public static class PopularPost {
        public final long postId;
        public final String content;
        public final Timestamp createdAt;
        public final int likeCount;
        public final int commentCount;
        public final int userId;
        public final String firstName;
        public final String lastName;

        public PopularPost(long postId, String content, Timestamp createdAt, int likeCount,
                           int commentCount, int userId, String firstName, String lastName) {
            this.postId = postId;
            this.content = content;
            this.createdAt = createdAt;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
            this.userId = userId;
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }

    /** Top hashtags in the last N days. */
    public List<TagTrend> getTopHashtags(int days, int limit) {
        // MySQL allows binding for the interval value, but LIMIT is finicky across drivers.
        // Safer: bind days; inject validated LIMIT.
        int safeLimit = Math.max(1, Math.min(limit, 50));

        String sql =
            "SELECT h.tag, COUNT(*) AS usage_count " +
            "FROM post_hashtag ph " +
            "JOIN hashtag h ON h.hashtag_id = ph.hashtag_id " +
            "JOIN post p ON p.post_id = ph.post_id " +
            "WHERE p.created_at >= (CURRENT_TIMESTAMP - INTERVAL ? DAY) " +
            "GROUP BY h.tag " +
            "ORDER BY usage_count DESC, h.tag ASC " +
            "LIMIT " + safeLimit;

        List<TagTrend> out = new ArrayList<>();
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new TagTrend(
                        rs.getString("tag"),
                        rs.getLong("usage_count")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    /** Most liked posts in the last N days (ties broken by newest). */
    public List<PopularPost> getMostLikedPosts(int days, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));

        String sql =
            "SELECT p.post_id, p.content, p.created_at, u.userId, u.firstName, u.lastName, " +
            "       COALESCE(lc.likes, 0)  AS like_count, " +
            "       COALESCE(cc.comments, 0) AS comment_count " +
            "FROM post p " +
            "JOIN user u ON u.userId = p.user_id " +
            "LEFT JOIN (SELECT post_id, COUNT(*) AS likes FROM `like` GROUP BY post_id) lc ON lc.post_id = p.post_id " +
            "LEFT JOIN (SELECT post_id, COUNT(*) AS comments FROM comment GROUP BY post_id) cc ON cc.post_id = p.post_id " +
            "WHERE p.created_at >= (CURRENT_TIMESTAMP - INTERVAL ? DAY) " +
            "ORDER BY like_count DESC, p.created_at DESC " +
            "LIMIT " + safeLimit;

        List<PopularPost> out = new ArrayList<>();
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new PopularPost(
                        rs.getLong("post_id"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at"),
                        rs.getInt("like_count"),
                        rs.getInt("comment_count"),
                        rs.getInt("userId"),
                        rs.getString("firstName"),
                        rs.getString("lastName")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }
}
