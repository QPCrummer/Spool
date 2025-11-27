package io.github.qpcrummer.spool.gui;

import io.github.qpcrummer.spool.utils.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FileDescriptor extends JPanel {

    public interface DeleteListener {
        void onDelete(FileDescriptor descriptor);
    }

    private final JTextField authorField;
    private final JButton tagDropdownButton;
    private final StayOpenPopupMenu tagMenu;
    private final JLabel fileNameLabel;
    private final List<String> selectedTags = new ArrayList<>();
    private final Path filePath;

    public FileDescriptor(File file, Set<String> allTags, DeleteListener deleteListener) {
        filePath = file.toPath();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); // nice uniform height

        // Left: File name
        fileNameLabel = new JLabel(file.getName());

        // Author
        authorField = new JTextField(15);

        // Tag dropdown
        tagMenu = new StayOpenPopupMenu(allTags, selectedTags);

        tagDropdownButton = new JButton("Tags ▼");
        tagDropdownButton.addActionListener(e ->
                tagMenu.show(tagDropdownButton, 0, tagDropdownButton.getHeight())
        );

        // Delete button
        JButton deleteBtn = new JButton("X");
        deleteBtn.setForeground(Color.RED);
        deleteBtn.addActionListener(e -> deleteListener.onDelete(this));

        // Layout
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(fileNameLabel);
        leftPanel.add(new JLabel("Author:"));
        leftPanel.add(authorField);
        leftPanel.add(tagDropdownButton);

        add(leftPanel, BorderLayout.CENTER);
        add(deleteBtn, BorderLayout.EAST);
    }

    public String getAuthor() {
        return authorField.getText();
    }

    public List<String> getSelectedTags() {
        return selectedTags;
    }

    public String getFileName() {
        return this.fileNameLabel.getText();
    }

    public String getFileType() {
        String ext = FileUtils.getFileExt(this.getFileName());
        return ext.toUpperCase(Locale.ROOT);
    }

    public Path getFilePath() {
        return this.filePath;
    }
}

