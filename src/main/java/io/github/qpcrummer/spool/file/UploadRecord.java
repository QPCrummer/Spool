package io.github.qpcrummer.spool.file;

import io.qt.widgets.QToolButton;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class UploadRecord {
    private FileRecord fileRecord;
    private Path path;
    private final List<String> tags = new ArrayList<>();
    private QToolButton tagButton; // add getter/setter

    public UploadRecord(FileRecord fileRecord, Path path) {
        this.fileRecord = fileRecord;
        this.path = path;
    }

    public FileRecord getFileRecord() {
        return fileRecord;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void setFileRecord(FileRecord fileRecord) {
        this.fileRecord = fileRecord;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTagButton(QToolButton button) {
        this.tagButton = button;
    }

    public QToolButton getTagButton() {
        return tagButton;
    }
}
