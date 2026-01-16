package io.github.qpcrummer.spool.file;

import io.github.qpcrummer.spool.Constants;
import io.qt.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

public record FileRecord(int id, String path, String fileType, String seller) {

    @Nullable
    public String getThumbnailPath() {
        Path path = Constants.FILES.resolve(path().replaceAll("\\.[^.]+$", "") + ".png");
        if (Files.exists(path)) {
            return path.toString();
        } else {
            return null;
        }
    }

    public static FileRecord partialClone(FileRecord fileRecord, String seller) {
        return new FileRecord(fileRecord.id, fileRecord.path, fileRecord.fileType, seller);
    }
}
