package io.github.qpcrummer.spool.gui;

import io.github.qpcrummer.spool.Data;
import io.github.qpcrummer.spool.database.DBUtils;
import io.qt.core.Qt;
import io.qt.gui.QFont;
import io.qt.widgets.*;

public class TagManagerPanel {

    public static QWidget init() {
        QWidget panel = new QWidget();
        panel.setStyleSheet("background-color: #3a3a3a");

        QVBoxLayout mainLayout = new QVBoxLayout(panel);
        mainLayout.setContentsMargins(6, 6, 6, 6);
        mainLayout.setSpacing(6);

        QLabel label = new QLabel("Tag Manager");
        QFont font = new QFont();
        font.setPointSize(16);
        font.setBold(true);
        label.setFont(font);
        label.setAlignment(Qt.AlignmentFlag.AlignCenter);
        mainLayout.addWidget(label);

        QScrollArea scrollArea = new QScrollArea();
        scrollArea.setWidgetResizable(true);
        scrollArea.setFrameShape(QFrame.Shape.NoFrame);

        QWidget scrollContainer = new QWidget();
        QVBoxLayout tagListLayout = new QVBoxLayout(scrollContainer);
        tagListLayout.setSpacing(4);
        tagListLayout.addStretch(1);

        scrollContainer.setLayout(tagListLayout);
        scrollArea.setWidget(scrollContainer);

        mainLayout.addWidget(scrollArea, 1);

        QPushButton addButton = new QPushButton("Add Tag");
        addButton.setStyleSheet("""
            background-color: #0078d7;
            color: white;
            border-radius: 4px;
            padding: 4px 8px;
        """);

        addButton.clicked.connect(() -> {
            String newTag = "New Tag";
            int suffix = 1;
            while (Data.FILE_TAGS.tags().contains(newTag)) {
                newTag = "New Tag " + suffix++;
            }

            Data.FILE_TAGS.tags().add(newTag);
            Data.FILE_TAGS.serialize();
            addTagRow(tagListLayout, newTag);
            broadcastUpdates();
        });

        mainLayout.addWidget(addButton);

        for (String tag : Data.FILE_TAGS.tags()) {
            addTagRow(tagListLayout, tag);
        }

        return panel;
    }

    private static void addTagRow(QVBoxLayout layout, String tagName) {
        QWidget row = new QWidget();
        QHBoxLayout rowLayout = new QHBoxLayout(row);
        rowLayout.setContentsMargins(0, 0, 0, 0);
        rowLayout.setSpacing(4);

        final String[] currentName = { tagName };

        QLineEdit tagEdit = new QLineEdit(currentName[0]);
        tagEdit.setStyleSheet("""
            background-color: #3a3a3a;
            color: white;
            border-radius: 4px;
        """);

        tagEdit.editingFinished.connect(() -> {
            String newName = tagEdit.text().trim();
            if (newName.isEmpty()) {
                tagEdit.setText(currentName[0]);
                return;
            }

            if (!newName.equals(currentName[0]) && Data.FILE_TAGS.tags().contains(newName)) {
                tagEdit.setText(currentName[0]);
                return;
            }

            // Update
            DBUtils.updateTagName(currentName[0], newName);
            Data.FILE_TAGS.tags().remove(currentName[0]);
            Data.FILE_TAGS.tags().add(newName);
            Data.FILE_TAGS.serialize();
            currentName[0] = newName;
            broadcastUpdates();
        });

        QPushButton remove = new QPushButton("✕");
        remove.setStyleSheet("color: red;");
        remove.setFixedSize(24, 24);

        remove.clicked.connect(() -> {
            layout.removeWidget(row);
            row.dispose();

            // Update
            Data.FILE_TAGS.tags().remove(currentName[0]);
            DBUtils.removeTagFromFiles(currentName[0]);
            DBUtils.repeatLastSearch();
            Data.FILE_TAGS.serialize();
            broadcastUpdates();
        });

        rowLayout.addWidget(tagEdit, 1);
        rowLayout.addWidget(remove, 0);

        layout.insertWidget(layout.count() - 1, row);
    }

    private static void broadcastUpdates() {
        FilePanel.rebuildFilterMenu();
        UploadPanel.updateAllUploadTagMenus();
        InfoPanel.rebuildTagMenu();
    }
}
