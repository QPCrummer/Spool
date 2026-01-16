package io.github.qpcrummer.spool.database;

import io.github.qpcrummer.spool.Data;
import io.github.qpcrummer.spool.file.FileRecord;
import io.github.qpcrummer.spool.utils.LoggerUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL = "jdbc:sqlite:files.db";

    /**
     * Initializes the database
     */
    public static void init() {
        try (Connection conn = DriverManager.getConnection(URL)) {

            String createFiles = """
                CREATE TABLE IF NOT EXISTS files (
                    id INTEGER PRIMARY KEY,
                    path TEXT NOT NULL,
                    file_type TEXT,
                    seller TEXT,
                    description TEXT
                );
            """;

            String createTags = """
                CREATE TABLE IF NOT EXISTS tags (
                    id INTEGER PRIMARY KEY,
                    name TEXT UNIQUE NOT NULL
                );
            """;

            String createFileTags = """
                CREATE TABLE IF NOT EXISTS file_tags (
                    file_id INTEGER,
                    tag_id INTEGER,
                    PRIMARY KEY (file_id, tag_id),
                    FOREIGN KEY (file_id) REFERENCES files(id),
                    FOREIGN KEY (tag_id) REFERENCES tags(id)
                );
            """;

            Statement stmt = conn.createStatement();
            stmt.execute(createFiles);
            stmt.execute(createTags);
            stmt.execute(createFileTags);
        } catch (SQLException e) {
            LoggerUtils.LOGGER.warn("Failed to create database", e);
        }
        Data.ACTIVE_FILES = getAllFiles();
    }

    /**
     * Gets all files in the database
     * @return List of all files in the database
     */
    public static List<FileRecord> getAllFiles() {
        List<FileRecord> files = new ArrayList<>();

        String query = "SELECT id, path, file_type, seller, description FROM files";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String path = rs.getString("path");
                String fileType = rs.getString("file_type");
                String seller = rs.getString("seller");

                files.add(new FileRecord(id, path, fileType, seller));
            }

        } catch (SQLException e) {
            LoggerUtils.LOGGER.warn("Failed to get file", e);
        }

        return files;
    }

    /**
     * Gets all the tags for a file
     * @param fileId File's id {@link FileRecord#id()}
     * @return List of file's tags
     */
    public static List<String> getTagsForFile(int fileId) {
        List<String> tags = new ArrayList<>();

        String sql = """
        SELECT t.name
        FROM tags t
        JOIN file_tags ft ON t.id = ft.tag_id
        WHERE ft.file_id = ?
    """;

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:files.db");
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, fileId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tags.add(rs.getString("name"));
            }

        } catch (SQLException e) {
            LoggerUtils.LOGGER.warn("Failed to load tags for file {}", fileId, e);
        }

        return tags;
    }
}
