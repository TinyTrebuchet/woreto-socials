package com.woreto.facebook.services.legacy;

import com.woreto.facebook.models.FBAccount;
import com.woreto.facebook.models.FBGroup;
import com.woreto.facebook.models.FBPage;
import com.woreto.facebook.models.FBPagePost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//@Service
public class FBDaoService {

    private static final String SEP = ":";
    private static final String DB_URL = "jdbc:sqlite:facebook.db";
    private static final Logger LOGGER = LoggerFactory.getLogger(FBDaoService.class);

    public FBDaoService() {
    }

    public List<FBPage> fetchAllPages(){
        List<FBPage> pages = new ArrayList<>();
        String sql = "SELECT * FROM fb_page ORDER BY lastPosted ASC;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                List<String> keywords = Arrays.asList(rs.getString("keywords").split(SEP));
                Long lastPosted = rs.getLong("lastPosted");
                String managerId = rs.getString("managerId");
                List<String> groupsToShare = Arrays.asList(rs.getString("groupsToShare").split(SEP));

                FBPage page = new FBPage(id, name, keywords, lastPosted, managerId, groupsToShare);
                pages.add(page);
            }
        } catch (SQLException e) {
            LOGGER.error("Error fetching facebook pages", e);
        }
        return pages;
    }

    public void updateLastPosted(FBPage page) {
        long currentTime = System.currentTimeMillis();
        String sql = "UPDATE fb_page SET lastPosted = ? WHERE id = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, currentTime);
            stmt.setString(2, page.getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.error("No pages updated with id {}", page.getId());
            }
        } catch (Exception e) {
            LOGGER.error("Error saving facebook page {}", page.getId(), e);
        }
    }

    public FBAccount fetchAccount(String emailId) {
        List<FBPage> pages = new ArrayList<>();
        String sql = "SELECT * FROM fb_account WHERE emailId = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            String password = rs.getString("password");
            String fullName = rs.getString("fullName");
            return new FBAccount(emailId, password, fullName);
        } catch (SQLException e) {
            LOGGER.error("Error fetching facebook pages", e);
        }
        return null;
    }

    public void savePagePost(FBPagePost post) {
        String sql = "INSERT INTO fb_page_post VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, post.getStoryId());
            stmt.setString(2, post.getActorId());
            stmt.setString(3, post.getUrl());
            stmt.setString(4, String.join(",", post.getSharedWith()));
            stmt.setLong(5, post.getCreatedTime());
            stmt.setLong(6, post.getModifiedTime());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Error saving {}", post, e);
        }
    }

    public FBPagePost fetchLatestPagePost() {
        String sql = "SELECT * FROM fb_page_post ORDER BY createdTime ASC LIMIT 1;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();

            return new FBPagePost(
                    rs.getString("storyId"),
                    rs.getString("actorId"),
                    rs.getString("url"),
                    Arrays.asList(rs.getString("sharedWith").split(",")),
                    rs.getLong("createdTime"),
                    rs.getLong("modifiedTime")
            );
        } catch (SQLException e) {
            LOGGER.error("Error fetching latest page post", e);
        }
        return null;
    }

    public FBGroup fetchGroup(String groupId) {
        List<FBPage> pages = new ArrayList<>();
        String sql = "SELECT * FROM fb_group WHERE id = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            String name = rs.getString("name");
            String managerId = rs.getString("managerId");
            Long lastPosted = rs.getLong("lastPosted");
            return new FBGroup(groupId, name, managerId, lastPosted);
        } catch (SQLException e) {
            LOGGER.error("Error fetching facebook pages", e);
        }
        return null;
    }
}
