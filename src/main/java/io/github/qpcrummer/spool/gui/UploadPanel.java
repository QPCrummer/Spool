package io.github.qpcrummer.spool.gui;

import io.github.qpcrummer.spool.Constants;
import io.github.qpcrummer.spool.Data;
import io.github.qpcrummer.spool.file.FileRecord;
import io.github.qpcrummer.spool.file.UploadRecord;
import io.github.qpcrummer.spool.gui.upload.FileDropZone;
import io.github.qpcrummer.spool.utils.FileIOUtils;
import io.github.qpcrummer.spool.utils.FileUtils;
import io.qt.core.Qt;
import io.qt.gui.QFont;
import io.qt.widgets.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UploadPanel {
    private static final List<UploadRecord> UPLOAD_RECORDS = new ArrayList<>();

    public static QWidget init() {
        QWidget panel = new QWidget();
        panel.setStyleSheet("background-color: #2e2e2e");

        QVBoxLayout layout = new QVBoxLayout(panel);
        layout.setSpacing(8);

        QLabel label = new QLabel("File Upload");
        QFont font = new QFont();
        font.setPointSize(16);
        font.setBold(true);
        label.setFont(font);
        label.setAlignment(Qt.AlignmentFlag.AlignCenter);
        layout.addWidget(label);

        QVBoxLayout dropWrapper = new QVBoxLayout();
        dropWrapper.setAlignment(Qt.AlignmentFlag.AlignCenter);

        QWidget listContainer = new QWidget();
        QVBoxLayout listLayout = new QVBoxLayout(listContainer);
        listLayout.setSpacing(6);
        listLayout.setAlignment(Qt.AlignmentFlag.AlignTop);

        FileDropZone dropZone = new FileDropZone(paths -> paths.forEach(path -> handleFileUpload(path, listLayout)));

        dropWrapper.addWidget(dropZone);
        layout.addLayout(dropWrapper);

        QScrollArea scrollArea = new QScrollArea();
        scrollArea.setWidgetResizable(true);
        scrollArea.setWidget(listContainer);
        scrollArea.setFrameShape(QFrame.Shape.NoFrame);

        layout.addWidget(scrollArea, 1);

        QPushButton uploadButton = new QPushButton("Upload");
        uploadButton.clicked.connect(() -> {
            List<UploadRecord> trimmed = UPLOAD_RECORDS.stream()
                    .filter(f -> !f.getFileRecord().seller().trim().isEmpty())
                    .toList();

            FileIOUtils.handleFileSUpload(trimmed);
            clearLayout(listLayout);
            UPLOAD_RECORDS.clear();
        });

        layout.addWidget(uploadButton);

        return panel;
    }

    private static void clearLayout(QLayout layout) {
        for (int i = layout.count() - 1; i >= 0; i--) {
            QLayoutItem item = layout.itemAt(i);
            QWidget widget = item.widget();
            if (widget != null) {
                layout.removeWidget(widget);
                widget.setParent(null);
            }
        }
    }

    private static void handleFileUpload(String pathStr, QVBoxLayout listLayout) {
        String fileName = FileUtils.getFileName(pathStr);
        String fileExt = FileUtils.getFileExt(fileName).toLowerCase(Locale.ROOT);
        if (fileExt.equals("zip")) {
            File file = new File(pathStr);
            Path unzippedFolder = FileUtils.unzip(file, file.toPath());
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(unzippedFolder)) {
                for (Path path : stream) {
                    String subFileExt = FileUtils.getFileExt(path.getFileName().toString()).toLowerCase(Locale.ROOT);
                    if (Constants.SUPPORTED_READING_FILE_TYPES.contains(subFileExt)) {
                        String subFileName = FileUtils.getFileName(path.getFileName().toString());
                        FileRecord fRecord = new FileRecord(-1, subFileName, subFileExt, "");
                        UploadRecord record = new UploadRecord(fRecord, path);
                        addUploadEntry(listLayout, record);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (Constants.SUPPORTED_READING_FILE_TYPES.contains(fileExt)) {
            FileRecord fRecord = new FileRecord(-1, fileName, fileExt, "");
            UploadRecord record = new UploadRecord(fRecord, Paths.get(pathStr));
            addUploadEntry(listLayout, record);
        }
    }

    private static QWidget createUploadEntry(UploadRecord record) {
        QWidget row = new QWidget();
        row.setStyleSheet("""
            QWidget {
                background-color: #1e1e1e;
                border-radius: 4px;
            }
        """);

        QVBoxLayout mainLayout = new QVBoxLayout(row);
        mainLayout.setContentsMargins(6, 6, 6, 6);
        mainLayout.setSpacing(4);

        QLineEdit fileName = new QLineEdit(record.getFileRecord().path());
        fileName.setReadOnly(true);
        mainLayout.addWidget(fileName);

        QHBoxLayout authorLayout = new QHBoxLayout();
        authorLayout.setSpacing(4);

        QLabel authorLabel = new QLabel("Author:");
        authorLabel.setStyleSheet("color: white;");

        QLineEdit author = new QLineEdit();
        author.editingFinished.connect(() -> {
            FileRecord fileRecord = FileRecord.partialClone(record.getFileRecord(), author.text());
            record.setFileRecord(fileRecord);
        });
        author.setStyleSheet("""
        background-color: #444444;
        color: white;
    """);

        authorLayout.addWidget(authorLabel);
        authorLayout.addWidget(author, 1);
        mainLayout.addLayout(authorLayout);

        QHBoxLayout bottomLayout = new QHBoxLayout();
        bottomLayout.setSpacing(4);

        QToolButton tagButton = new QToolButton();
        record.setTagButton(tagButton);
        tagButton.setText("Tags");
        tagButton.setPopupMode(QToolButton.ToolButtonPopupMode.InstantPopup);
        tagButton.setMinimumWidth(60);

        QMenu tagMenu = new QMenu(tagButton);
        tagButton.setMenu(tagMenu);

        for (String tag : Data.FILE_TAGS.toList()) {
            QCheckBox checkBox = new QCheckBox(tag);
            checkBox.toggled.connect(b -> {
                if (b) record.getTags().add(tag);
                else record.getTags().remove(tag);
            });
            QWidgetAction action = new QWidgetAction(tagMenu);
            action.setDefaultWidget(checkBox);
            tagMenu.addAction(action);
        }

        QPushButton remove = new QPushButton("✕");
        remove.setStyleSheet("""
            color: red;
            font-weight: bold;
        """);
        remove.setFixedSize(24, 24);
        remove.setSizePolicy(QSizePolicy.Policy.Fixed, QSizePolicy.Policy.Fixed);

        bottomLayout.addWidget(tagButton, 0);
        bottomLayout.addStretch(1);
        bottomLayout.addWidget(remove, 0);

        mainLayout.addLayout(bottomLayout);

        return row;
    }

    private static void addUploadEntry(
            QVBoxLayout listLayout,
            UploadRecord record
    ) {
        QWidget row = createUploadEntry(record);

        UploadPanel.UPLOAD_RECORDS.add(record);

        Runnable removeAction = () -> {
            listLayout.removeWidget(row);
            row.dispose();
            UploadPanel.UPLOAD_RECORDS.remove(record);
        };

        QPushButton removeButton = row.findChild(QPushButton.class);
        removeButton.clicked.connect(removeAction::run);

        listLayout.addWidget(row);
    }

    public static void updateAllUploadTagMenus() {
        for (UploadRecord record : UPLOAD_RECORDS) {
            if (record.getTagButton() != null) {
                rebuildUploadTagsMenu(record, record.getTagButton());
            }
        }
    }

    private static void rebuildUploadTagsMenu(UploadRecord record, QToolButton tagButton) {
        QMenu tagMenu = tagButton.menu();
        tagMenu.clear();

        for (String tag : Data.FILE_TAGS.toList()) {
            QCheckBox checkBox = new QCheckBox(tag);
            checkBox.setChecked(record.getTags().contains(tag));

            checkBox.toggled.connect(b -> {
                if (b) record.getTags().add(tag);
                else record.getTags().remove(tag);
            });

            QWidgetAction action = new QWidgetAction(tagMenu);
            action.setDefaultWidget(checkBox);
            tagMenu.addAction(action);
        }
    }
}
