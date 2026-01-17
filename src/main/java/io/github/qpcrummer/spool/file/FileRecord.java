package io.github.qpcrummer.spool.file;

import io.github.qpcrummer.spool.Constants;

import java.nio.file.Files;
import java.nio.file.Path;

public record FileRecord(int id, String path, String fileType, String seller) {

    /**
     * Gets the path to the thumbnail
     * @return Thumbnail path or null if nonexistent
     */
    public String getThumbnailPath() {
        // Thumbnail for >= v2.0.2
        Path path = Constants.FILES.resolve(path() + ".png");
        if (Files.exists(path)) {
            return path.toString();
        } else {
            // Thumbnail for < v2.0.2
            path = Constants.FILES.resolve(path().replaceAll("\\.[^.]+$", "") + ".png");
            if (Files.exists(path)) {
                return path.toString();
            }
            return null;
        }
    }

    /**
     * Clones everything but the seller's name
     * @param fileRecord FileRecord to clone
     * @param seller The new seller's name
     * @return FileRecord instance with new seller's name
     */
    public static FileRecord partialClone(FileRecord fileRecord, String seller) {
        return new FileRecord(fileRecord.id, fileRecord.path, fileRecord.fileType, seller);
    }
}
