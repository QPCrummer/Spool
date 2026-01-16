package io.github.qpcrummer.spool.gui_2;

import io.github.qpcrummer.spool.Data;
import io.github.qpcrummer.spool.database.DBUtils;
import io.qt.widgets.*;

public class TagManagerPanel {

    public static QWidget init() {
        QWidget panel = new QWidget();
        panel.setStyleSheet("background-color: #3a3a3a");

        QVBoxLayout mainLayout = new QVBoxLayout(panel);
        mainLayout.setContentsMargins(6, 6, 6, 6);
        mainLayout.setSpacing(6);

        // --- Scroll area for tags ---
        QScrollArea scrollArea = new QScrollArea();
        scrollArea.setWidgetResizable(true);
        scrollArea.setFrameShape(QFrame.Shape.NoFrame);

        QWidget scrollContainer = new QWidget();
        QVBoxLayout tagListLayout = new QVBoxLayout(scrollContainer);
        tagListLayout.setSpacing(4);
        tagListLayout.addStretch(1); // push items to top

        scrollContainer.setLayout(tagListLayout);
        scrollArea.setWidget(scrollContainer);

        mainLayout.addWidget(scrollArea, 1);

        // --- Add tag button ---
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
            while (Data.FILE_TAGS.tags().contains(newTag)) { // ensure unique
                newTag = "New Tag " + suffix++;
            }

            Data.FILE_TAGS.tags().add(newTag); // add to backing store
            Data.FILE_TAGS.serialize();
            addTagRow(tagListLayout, newTag);
            broadcastUpdates();
        });

        mainLayout.addWidget(addButton);

        // --- populate existing tags ---
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

        final String[] currentName = { tagName }; // mutable wrapper

        QLineEdit tagEdit = new QLineEdit(currentName[0]);
        tagEdit.setStyleSheet("""
        background-color: #3a3a3a;
        color: white;
        border-radius: 4px;
    """);

        // Update backing store on editing
        tagEdit.editingFinished.connect(() -> {
            String newName = tagEdit.text().trim();
            if (newName.isEmpty()) {
                tagEdit.setText(currentName[0]); // revert if empty
                return;
            }

            if (!newName.equals(currentName[0]) && Data.FILE_TAGS.tags().contains(newName)) {
                // duplicate detected, revert
                tagEdit.setText(currentName[0]);
                return;
            }

            // update backing store
            DBUtils.updateTagName(currentName[0], newName);
            Data.FILE_TAGS.tags().remove(currentName[0]);
            Data.FILE_TAGS.tags().add(newName);
            Data.FILE_TAGS.serialize();

            // update local holder
            currentName[0] = newName;
            broadcastUpdates();
        });

        QPushButton remove = new QPushButton("✕");
        remove.setStyleSheet("color: red;");
        remove.setFixedSize(24, 24);

        remove.clicked.connect(() -> {
            layout.removeWidget(row);
            row.dispose();
            Data.FILE_TAGS.tags().remove(currentName[0]);
            DBUtils.removeTagFromFiles(currentName[0]);
            Data.FILE_TAGS.serialize();
            broadcastUpdates();
        });

        rowLayout.addWidget(tagEdit, 1); // stretch
        rowLayout.addWidget(remove, 0);

        // insert before stretch
        layout.insertWidget(layout.count() - 1, row);
    }

    private static void broadcastUpdates() {
        FilePanel.rebuildFilterMenu();
        UploadPanel.updateAllUploadTagMenus();
        InfoPanel.rebuildTagMenu();
    }
}
