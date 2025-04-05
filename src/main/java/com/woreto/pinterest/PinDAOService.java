package com.woreto.pinterest;

import com.woreto.linkedin.models.LinkedinAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.Collection;

@Service
public class PinDAOService {

    private static final String DB_URL = "jdbc:sqlite:pinterest.db";
    private static final Logger LOGGER = LoggerFactory.getLogger(PinDAOService.class);

    public PinDAOService() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String createPinsTable = "CREATE TABLE IF NOT EXISTS pins (" +
                    "id TEXT PRIMARY KEY, " +
                    "keyword TEXT NOT NULL, " +
                    "mediaType TEXT NOT NULL, " + // IMAGE or VIDEO
                    "imageUrl TEXT NOT NULL, " +
                    "imageWidth INTEGER DEFAULT 0, " +
                    "imageHeight INTEGER DEFAULT 0, " +
                    "title TEXT, " +
                    "description TEXT, " +
                    "autoAltText TEXT, " +
                    "reactionCount INTEGER DEFAULT 0, " +
                    "publisherReach INTEGER DEFAULT 0, " +
                    "createdAt TEXT NOT NULL, " +
                    "posted TEXT NOT NULL)";
            stmt.execute(createPinsTable);
        } catch (SQLException e) {
            LOGGER.error("Error setting up connection with {}", DB_URL, e);
        }
    }

    public void save(PinterestPin pin) {
        String pinSql = "INSERT INTO pins VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pinStmt = conn.prepareStatement(pinSql)) {
            pinStmt.setString(1, pin.getId());
            pinStmt.setString(2, pin.getKeyword());
            pinStmt.setString(3, pin.getMediaType().name());
            pinStmt.setString(4, pin.getImageUrl());
            pinStmt.setInt(5, pin.getImageWidth());
            pinStmt.setInt(6, pin.getImageHeight());
            pinStmt.setString(7, pin.getTitle());
            pinStmt.setString(8, pin.getDescription());
            pinStmt.setString(9, pin.getAutoAltText());
            pinStmt.setInt(10, pin.getReactionCount());
            pinStmt.setInt(11, pin.getPublisherReach());
            pinStmt.setString(12, pin.getCreatedAt());
            pinStmt.setBoolean(13, pin.isPosted());
            pinStmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Error saving pin {}", pin.getId(), e);
        }
    }

    public void bulkSave(Collection<PinterestPin> pins) {
        // sqlite3 doesn't have bulk writes
        for (PinterestPin pin : pins) {
            save(pin);
        }
    }

    public PinterestPin fetchUnpostedPin(String keyword) {
        String sql = "SELECT * FROM pins WHERE keyword = ? AND mediaType = 'VIDEO' AND posted = false ORDER BY reactionCount DESC;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, keyword);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return createPin(rs);
        } catch (SQLException e) {
            LOGGER.error("Error fetching pin", e);
        }
        return null;
    }

    public void updatePosted(String pinId) {
        String sql = "UPDATE pins SET posted = true WHERE id = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pinId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.error("No pin updated with id {}", pinId);
            }
        } catch (Exception e) {
            LOGGER.error("Error updating pin {}", pinId, e);
        }
    }

    private PinterestPin createPin(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String keyword = rs.getString("keyword");
        PinterestPin.Type mediaType = PinterestPin.Type.valueOf(rs.getString("mediaType"));
        String imageUrl = rs.getString("imageUrl");
        int imageWidth = rs.getInt("imageWidth");
        int imageHeight = rs.getInt("imageHeight");
        String title = rs.getString("title");
        String description = rs.getString("description");
        String autoAltText = rs.getString("autoAltText");
        int reactionCount = rs.getInt("reactionCount");
        int publisherReach = rs.getInt("publisherReach");
        String createdAt = rs.getString("createdAt");
        boolean posted = rs.getBoolean("posted");
        return new PinterestPin(id, keyword, mediaType, imageUrl, imageWidth, imageHeight, title, description,
                autoAltText, reactionCount, publisherReach, createdAt, posted);
    }

}
