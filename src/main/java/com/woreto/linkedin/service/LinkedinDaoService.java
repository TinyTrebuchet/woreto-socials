package com.woreto.linkedin.service;

import com.woreto.linkedin.models.LinkedinAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class LinkedinDaoService {

    private static final String DB_URL = "jdbc:sqlite:linkedin.db";
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkedinDaoService.class);

    public LinkedinAccount fetchAccount(String emailId) {
        String sql = "SELECT * FROM linkedin_account WHERE emailId = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            String fullName = rs.getString("fullName");
            String password = rs.getString("password");
            return new LinkedinAccount(emailId, password, fullName);
        } catch (SQLException e) {
            LOGGER.error("Error fetching linkedin account for emailId {}", emailId, e);
        }
        return null;
    }
}
