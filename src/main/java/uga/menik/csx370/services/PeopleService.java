/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.csx370.models.FollowableUser;

/**
 * This service contains people related functions.
 */
@Service
public class PeopleService {
    @Autowired
    private DataSource dataSource;

    /**
     * Returns a list of followable users (excluding the current user),
     */
    public List<FollowableUser> getFollowableUsers(String userIdToExclude) {
        String sql =
            "SELECT u.userId, u.firstName, u.lastName, " +
            "       COALESCE(DATE_FORMAT(MAX(p.created_at), '%b %d, %Y, %h:%i %p'), 'Unknown') AS last_post_time, " +
            "       CASE WHEN EXISTS ( " +
            "         SELECT 1 FROM follow f " +
            "          WHERE f.follower_id = ? AND f.followee_id = u.userId " +
            "       ) THEN 1 ELSE 0 END AS is_followed " +
            "FROM user u " +
            "LEFT JOIN post p ON p.user_id = u.userId " +   
            "WHERE u.userId <> ? " +
            "GROUP BY u.userId, u.firstName, u.lastName " +
            "ORDER BY COALESCE(MAX(p.created_at), TIMESTAMP('1970-01-01 00:00:00')) DESC";

        List<FollowableUser> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int me = Integer.parseInt(userIdToExclude);
            stmt.setInt(1, me);
            stmt.setInt(2, me);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String id = String.valueOf(rs.getInt("userId"));
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
                    String lastActive = rs.getString("last_post_time");
                    boolean isFollowed = rs.getInt("is_followed") == 1;

                    users.add(new FollowableUser(id, firstName, lastName, isFollowed, lastActive));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    /** 
     * Adds a follow record to the database. 
    */
    public void follow(String followerId, String followeeId) {
        String sql = "INSERT IGNORE INTO follow(follower_id, followee_id) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(followerId));
            ps.setInt(2, Integer.parseInt(followeeId));
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 
     * Removes a follow record from the database. 
    */
    public void unfollow(String followerId, String followeeId) {
        String sql = "DELETE FROM follow WHERE follower_id = ? AND followee_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(followerId));
            ps.setInt(2, Integer.parseInt(followeeId));
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
