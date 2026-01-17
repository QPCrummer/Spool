package io.github.qpcrummer.spool.gui;

import io.github.qpcrummer.spool.Data;
import io.github.qpcrummer.spool.database.DBUtils;
import io.github.qpcrummer.spool.database.Database;
import io.github.qpcrummer.spool.file.FileRecord;
import io.github.qpcrummer.spool.gui.conversion.ConversionDialog;
import io.github.qpcrummer.spool.gui.file_list.FileItemDelegate;
import io.qt.core.Qt;
import io.qt.gui.QFont;
import io.qt.gui.QTextOption;
import io.qt.widgets.*;

public class InfoPanel {
    private static QTextEdit fileNameEdit;
    private static QLineEdit sellerEdit;
    private static QMenu tagMenu;

    public static QWidget init() {
        QWidget panel = new QWidget();
        panel.setMinimumHeight(120);
        panel.setStyleSheet("background-color: #2e2e2e");

        QVBoxLayout layout = new QVBoxLayout(panel);
        layout.setContentsMargins(6, 6, 6, 6);
        layout.setSpacing(6);

        QLabel label = new QLabel("Selected File");
        QFont font = new QFont();
        font.setPointSize(16);
        font.setBold(true);
        label.setFont(font);
        label.setAlignment(Qt.AlignmentFlag.AlignCenter);
        layout.addWidget(label);

        fileNameEdit = new QTextEdit();
        fileNameEdit.setReadOnly(true);
        fileNameEdit.setWordWrapMode(QTextOption.WrapMode.WrapAnywhere);
        fileNameEdit.setFixedHeight(50);
        fileNameEdit.setStyleSheet("""
            background-color: #3a3a3a;
            color: white;
            border-radius: 4px;
        """);
        layout.addWidget(new QLabel("File:"));
        layout.addWidget(fileNameEdit);

        sellerEdit = new QLineEdit();
        sellerEdit.setStyleSheet("""
            background-color: #444444;
            color: white;
            border-radius: 4px;
        """);
        sellerEdit.editingFinished.connect(() -> {
            if (Data.SELECTED_FILE != null) {
                Data.SELECTED_FILE = FileRecord.partialClone(Data.SELECTED_FILE, sellerEdit.text());
                DBUtils.updateFile(Data.SELECTED_FILE.id(), null, Data.SELECTED_FILE.seller(), null);
                FilePanel.getModel().updateFile(Data.SELECTED_INDEX, Data.SELECTED_FILE);
            }
        });
        layout.addWidget(new QLabel("Seller:"));
        layout.addWidget(sellerEdit);

        QHBoxLayout tagLayout = new QHBoxLayout();
        QToolButton tagButton = new QToolButton();
        tagButton.setText("Tags");
        tagButton.setPopupMode(QToolButton.ToolButtonPopupMode.InstantPopup);

        tagMenu = new QMenu(tagButton);
        tagButton.setMenu(tagMenu);
        tagLayout.addWidget(tagButton);

        layout.addLayout(tagLayout);

        QHBoxLayout buttonLayout = new QHBoxLayout();
        QPushButton convertButton = new QPushButton("Convert");
        QPushButton deleteButton = new QPushButton("Delete");

        convertButton.clicked.connect(() -> {
            if (Data.SELECTED_FILE != null) {
                ConversionDialog dialog = new ConversionDialog(
                        convertButton
                );
                dialog.exec();
            }
        });

        deleteButton.clicked.connect(() -> {
            if (Data.SELECTED_FILE != null) {
                FileItemDelegate.removeThumbnailFromCache(Data.SELECTED_FILE.id());
                DBUtils.removeFile(Data.SELECTED_FILE.id());
                FilePanel.getModel().removeFileById(Data.SELECTED_FILE.id());
                clear();
            }
        });

        buttonLayout.addWidget(convertButton);
        buttonLayout.addWidget(deleteButton);
        layout.addLayout(buttonLayout);

        return panel;
    }

    /**
     * Call this whenever a new file is selected in the File List Panel
     * @param file File to set selected
     */
    public static void setSelectedFile(FileRecord file) {
        Data.SELECTED_FILE = file;
        Data.SELECTED_FILE_TAGS.clear();
        Data.SELECTED_FILE_TAGS.addAll(Database.getTagsForFile(file.id()));
        fileNameEdit.setText(file.path());
        sellerEdit.setText(file.seller());
        rebuildTagMenu();
    }

    /**
     * Rebuild tag dropdown, preserving selected tags
     */
    public static void rebuildTagMenu() {
        tagMenu.clear();
        if (Data.SELECTED_FILE == null) {
            return;
        }

        for (String tag : Data.FILE_TAGS.toList()) {
            QCheckBox checkBox = new QCheckBox(tag);
            checkBox.setChecked(Data.SELECTED_FILE_TAGS.contains(tag));

            checkBox.toggled.connect(checked -> {
                if (checked) {
                    Data.SELECTED_FILE_TAGS.add(tag);
                    DBUtils.addTagToFile(Data.SELECTED_FILE.id(), tag);
                } else {
                    Data.SELECTED_FILE_TAGS.remove(tag);
                    DBUtils.removeTagFromFile(Data.SELECTED_FILE.id(), tag);
                }
            });

            QWidgetAction action = new QWidgetAction(tagMenu);
            action.setDefaultWidget(checkBox);
            tagMenu.addAction(action);
        }
    }

    /**
     * Clear panel when no file is selected
     */
    public static void clear() {
        Data.SELECTED_FILE = null;
        Data.SELECTED_FILE_TAGS.clear();
        fileNameEdit.clear();
        sellerEdit.clear();
        tagMenu.clear();
    }
}
