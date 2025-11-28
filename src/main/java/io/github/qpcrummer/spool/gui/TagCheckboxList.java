package io.github.qpcrummer.spool.gui;

import io.github.qpcrummer.spool.Data;
import io.github.qpcrummer.spool.database.DBUtils;
import io.github.qpcrummer.spool.database.Database;

import javax.swing.*;
import java.awt.*;

public class TagCheckboxList extends JPanel {

    public TagCheckboxList(int fileId) {

        setLayout(new BorderLayout());

        // Scrollable container
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(checkboxPanel);
        scrollPane.setPreferredSize(new Dimension(200, 150));
        add(scrollPane, BorderLayout.CENTER);

        // Load all tags
        java.util.List<String> allTags = Data.FILE_TAGS.tags().stream().toList();
        // Load tags for this specific file
        java.util.List<String> fileTags = Database.getTagsForFile(fileId);

        for (String tag : allTags) {
            JCheckBox box = new JCheckBox(tag);
            box.setSelected(fileTags.contains(tag));

            // Update tag relation when clicked
            box.addActionListener(_ -> {
                try {
                    if (box.isSelected()) {
                        DBUtils.addTagToFile(fileId, tag);
                    } else {
                        DBUtils.removeTagFromFile(fileId, tag);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            checkboxPanel.add(box);
        }
    }
}
