package com.woreto.reddit.service;

import com.woreto.reddit.models.RedditAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class RedditDaoService {

    private static final String DB_URL = "jdbc:sqlite:reddit.db";
    private static final Logger LOGGER = LoggerFactory.getLogger(RedditDaoService.class);

    public RedditAccount fetchAccount(String emailId) {
        String sql = "SELECT * FROM reddit_account WHERE emailId = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            String username = rs.getString("username");
            String password = rs.getString("password");
            return new RedditAccount(username, emailId, password);
        } catch (SQLException e) {
            LOGGER.error("Error fetching reddit account", e);
        }
        return null;
    }
}
