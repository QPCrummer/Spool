package io.github.qpcrummer.spool.database;

import io.github.qpcrummer.spool.file.FileRecord;
import io.github.qpcrummer.spool.gui_2.FilePanel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DBUtils {
    private static FileQuery previousQuery;

    public static void addFile(String path, String fileType, String seller, List<String> tags) throws Exception {
        String url = "jdbc:sqlite:files.db";
        int fileId;
        try (Connection conn = DriverManager.getConnection(url)) {

            // Insert the file
            PreparedStatement psFile = conn.prepareStatement(
                    "INSERT INTO files(path, file_type, seller) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            psFile.setString(1, path);
            psFile.setString(2, fileType);
            psFile.setString(3, seller);
            psFile.executeUpdate();

            ResultSet rs = psFile.getGeneratedKeys();
            rs.next();
            fileId = rs.getInt(1);

            // Insert tags
            for (String tag : tags) {
                PreparedStatement psTag = conn.prepareStatement(
                        "INSERT OR IGNORE INTO tags(name) VALUES (?)"
                );
                psTag.setString(1, tag);
                psTag.executeUpdate();

                // Get tag id
                PreparedStatement psGetTagId = conn.prepareStatement(
                        "SELECT id FROM tags WHERE name = ?"
                );
                psGetTagId.setString(1, tag);
                ResultSet rsTag = psGetTagId.executeQuery();
                rsTag.next();
                int tagId = rsTag.getInt("id");

                // Insert into file_tags
                PreparedStatement psFileTag = conn.prepareStatement(
                        "INSERT INTO file_tags(file_id, tag_id) VALUES (?, ?)"
                );
                psFileTag.setInt(1, fileId);
                psFileTag.setInt(2, tagId);
                psFileTag.executeUpdate();
            }
        }

        // Update UI
        repeatLastSearch();
    }

    public static void removeFile(int fileId) throws Exception {
        String url = "jdbc:sqlite:files.db";
        try (Connection conn = DriverManager.getConnection(url)) {

            // Delete tag relations
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM file_tags WHERE file_id = ?"
            )) {
                ps.setInt(1, fileId);
                ps.executeUpdate();
            }

            // Delete file
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM files WHERE id = ?"
            )) {
                ps.setInt(1, fileId);
                ps.executeUpdate();
            }
        }

        // Update UI
        repeatLastSearch();
    }

    /**
     * Updates information about a file
     * @param fileId The new file id; Null keeps the original
     * @param newPath The new filename for the file; Null keeps the original
     * @param newSeller The new seller's name; Null keeps the original
     * @param newTags New set of tags; Empty removes all tags; Null keeps the original
     */
    public static void updateFile(
            int fileId,
            String newPath,
            String newSeller,
            List<String> newTags
    ) throws Exception {

        String url = "jdbc:sqlite:files.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            StringBuilder sql = new StringBuilder("UPDATE files SET ");
            List<Object> params = new ArrayList<>();

            if (newPath != null) {
                sql.append("path = ?, ");
                params.add(newPath);
            }

            if (newSeller != null) {
                sql.append("seller = ?, ");
                params.add(newSeller);
            }

            // If no fields are set AND tags are null → no updates
            if (params.isEmpty() && newTags == null) {
                return;
            }

            // Remove trailing comma
            if (!params.isEmpty()) {
                sql.setLength(sql.length() - 2);
                sql.append(" WHERE id = ?");
            }

            if (!params.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                    int index = 1;

                    for (Object o : params) {
                        ps.setObject(index++, o);
                    }

                    // File ID
                    ps.setInt(index, fileId);

                    ps.executeUpdate();
                }
            }


            if (newTags != null) {
                // Remove old tags
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM file_tags WHERE file_id = ?"
                )) {
                    ps.setInt(1, fileId);
                    ps.executeUpdate();
                }

                // Insert new tags (empty list = leave no tags)
                for (String tag : newTags) {

                    // Insert tag if it's new
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT OR IGNORE INTO tags(name) VALUES (?)"
                    )) {
                        ps.setString(1, tag);
                        ps.executeUpdate();
                    }

                    // Get tag ID
                    int tagId;
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT id FROM tags WHERE name = ?"
                    )) {
                        ps.setString(1, tag);
                        ResultSet rs = ps.executeQuery();
                        rs.next();
                        tagId = rs.getInt("id");
                    }

                    // Associate tag with this file
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO file_tags(file_id, tag_id) VALUES (?, ?)"
                    )) {
                        ps.setInt(1, fileId);
                        ps.setInt(2, tagId);
                        ps.executeUpdate();
                    }
                }
            }
        }
        repeatLastSearch();
    }

    private static FileRecord mapFile(ResultSet rs) throws SQLException {
        return new FileRecord(
                rs.getInt("id"),
                rs.getString("path"),
                rs.getString("file_type"),
                rs.getString("seller")
        );
    }

    /**
     * Searches for a list of matching files
     * @param seller The person or company where the file was purchased or downloaded
     * @param fileNameLike Close matches to a given name
     * @param fileType The file type (PES, DST, etc)
     * @param requiredTags Tags that the search has
     */
    public static void searchFiles(
            String seller,
            String fileNameLike,
            String fileType,
            List<String> requiredTags
    ) throws Exception {
        previousQuery = new FileQuery(seller, fileNameLike, fileType, requiredTags);

        StringBuilder sql = new StringBuilder("""
        SELECT f.*
        FROM files f
    """);

        boolean filterTags = requiredTags != null && !requiredTags.isEmpty();

        if (filterTags) {
            sql.append("""
            JOIN file_tags ft ON f.id = ft.file_id
            JOIN tags t ON ft.tag_id = t.id
        """);
        }

        sql.append(" WHERE 1=1 ");

        // Dynamic filters
        if (seller != null && !seller.isEmpty()) {
            sql.append(" AND f.seller = ? ");
        }

        if (fileNameLike != null && !fileNameLike.isEmpty()) {
            sql.append(" AND f.path LIKE '%' || ? || '%' ");
        }

        if (fileType != null && !fileType.isEmpty()) {
            sql.append(" AND f.file_type = ? ");
        }

        if (filterTags) {
            String placeholders = requiredTags.stream()
                    .map(_ -> "?")
                    .collect(Collectors.joining(","));

            sql.append(" AND t.name IN (").append(placeholders).append(") ");
            sql.append(" GROUP BY f.id HAVING COUNT(DISTINCT t.name) = ").append(requiredTags.size());
        }

        List<FileRecord> results = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:files.db");
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;

            // Fill parameters in the same order they were added
            if (seller != null && !seller.isEmpty()) {
                ps.setString(idx++, seller);
            }

            if (fileNameLike != null && !fileNameLike.isEmpty()) {
                ps.setString(idx++, fileNameLike);
            }

            if (fileType != null && !fileType.isEmpty()) {
                ps.setString(idx++, fileType);
            }

            if (filterTags) {
                for (String tag : requiredTags) {
                    ps.setString(idx++, tag);
                }
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                results.add(mapFile(rs));
            }
        }

        // Update main UI
        FilePanel.updateSearchList(results);
    }

    public static void repeatLastSearch() {
        if (previousQuery == null) {
            FilePanel.updateSearchList(Database.getAllFiles());
        } else {
            try {
                searchFiles(previousQuery.seller(), previousQuery.fileNameLike(), previousQuery.fileType(), previousQuery.requiredTags());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void incrementalSearch(  String seller,
                                           String fileNameLike,
                                           String fileType,
                                           List<String> requiredTags
    ) {
        if (previousQuery == null) {
            try {
                searchFiles(seller, fileNameLike, fileType, requiredTags);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            String finalSeller = seller == null ? previousQuery.seller() : seller;
            String finalFileNameLike = fileNameLike == null ? previousQuery.fileNameLike() : fileNameLike;
            String finalFileType = fileType == null ? previousQuery.fileType() : fileType;
            List<String> finalRequiredTags = requiredTags == null ? previousQuery.requiredTags() : requiredTags;
            try {
                searchFiles(finalSeller, finalFileNameLike, finalFileType, finalRequiredTags);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void addTagToFile(int fileId, String tag) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:files.db")) {

            // Insert tag if missing
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT OR IGNORE INTO tags(name) VALUES (?)")) {
                ps.setString(1, tag);
                ps.executeUpdate();
            }

            // Get tag id
            int tagId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM tags WHERE name = ?")) {
                ps.setString(1, tag);
                ResultSet rs = ps.executeQuery();
                rs.next();
                tagId = rs.getInt("id");
            }

            // Insert relation
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT OR IGNORE INTO file_tags(file_id, tag_id) VALUES (?, ?)")) {
                ps.setInt(1, fileId);
                ps.setInt(2, tagId);
                ps.executeUpdate();
            }
        }
    }

    public static void removeTagFromFile(int fileId, String tag) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:files.db")) {

            // Get tag id
            int tagId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM tags WHERE name = ?")) {
                ps.setString(1, tag);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return; // no tag
                tagId = rs.getInt("id");
            }

            // Delete relation
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM file_tags WHERE file_id = ? AND tag_id = ?")) {
                ps.setInt(1, fileId);
                ps.setInt(2, tagId);
                ps.executeUpdate();
            }
        }
    }

    public static void removeTagFromFiles(String tag) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:files.db")) {

            // Get tag id
            Integer tagId = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM tags WHERE name = ?"
            )) {
                ps.setString(1, tag);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    tagId = rs.getInt("id");
                }
            }

            if (tagId == null) {
                return; // Tag doesn't exist
            }

            // Delete all relations for this tag
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM file_tags WHERE tag_id = ?"
            )) {
                ps.setInt(1, tagId);
                ps.executeUpdate();
            }
        }
    }

    public static void updateTagName(String oldName, String newName) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:files.db")) {

            // Check that oldName exists
            Integer tagId = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM tags WHERE name = ?"
            )) {
                ps.setString(1, oldName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    tagId = rs.getInt("id");
                }
            }

            if (tagId == null) {
                // Tag isn't found
                return;
            }

            // Ensure newName isn't already used
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) AS count FROM tags WHERE name = ?"
            )) {
                ps.setString(1, newName);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt("count") > 0) {
                    throw new Exception("Tag name already exists: " + newName);
                }
            }

            // Update tag name
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE tags SET name = ? WHERE id = ?"
            )) {
                ps.setString(1, newName);
                ps.setInt(2, tagId);
                ps.executeUpdate();
            }
        }
    }

}
