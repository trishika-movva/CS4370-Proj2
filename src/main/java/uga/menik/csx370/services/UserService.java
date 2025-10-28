/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import uga.menik.csx370.models.User;

/**
 * Service class to handle user-related operations such as authentication and registration.
 */
@Service
@SessionScope
public class UserService {

    // DataSource for database connections.
    private final DataSource dataSource;
    // Password encoder for hashing passwords.
    private final BCryptPasswordEncoder passwordEncoder;
    private User loggedInUser = null;

    /**
     * Constructor-based dependency injection for DataSource.
     */
    @Autowired
    public UserService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Authenticates a user with the given username and password.
     * Returns true if authentication is successful, false otherwise.
     */
    public boolean authenticate(String username, String password) throws SQLException {
        final String sql = "select * from user where username = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String storedPasswordHash = rs.getString("password");
                    boolean isPassMatch = passwordEncoder.matches(password, storedPasswordHash);
                    if (isPassMatch) {
                        String userId = rs.getString("userId");
                        String firstName = rs.getString("firstName");
                        String lastName = rs.getString("lastName");

                        loggedInUser = new User(userId, firstName, lastName);
                    }
                    return isPassMatch;
                }
            }
        }
        return false;
    }

    /**
     * Logs out the user.
     */
    public void unAuthenticate() {
        loggedInUser = null;
    }

    /**
     * Checks if a user is currently authenticated.
     */
    public boolean isAuthenticated() {
        return loggedInUser != null;
    }

    /**
     * Retrieves the currently logged-in user.
     */
    public User getLoggedInUser() {
        return loggedInUser;
    }

    public User getUserByUsername(String username) throws SQLException {
    String query = "SELECT * FROM user WHERE username = ?";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, username);

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String userId = rs.getString("userId");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");

                return new User(userId, firstName, lastName);
            }
        }
    }
    return null;
}



    /**
     * Registers a new user with the given details. 
     * Returns true if registration is successful, false otherwise.
     */
    public boolean registerUser(String username, String password, String firstName, String lastName)
            throws SQLException {
        final String registerSql = "insert into user (username, password, firstName, lastName) values (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement registerStmt = conn.prepareStatement(registerSql)) {
            registerStmt.setString(1, username);
            registerStmt.setString(2, passwordEncoder.encode(password));
            registerStmt.setString(3, firstName);
            registerStmt.setString(4, lastName);

            int rowsAffected = registerStmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
}
