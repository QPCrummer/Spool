package io.github.qpcrummer.spool.gui;

import io.github.qpcrummer.spool.Constants;
import io.github.qpcrummer.spool.Data;
import io.github.qpcrummer.spool.database.DBUtils;
import io.github.qpcrummer.spool.utils.FileConverter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class FileManagerPanel extends JPanel {
    private JPanel fileList;
    private JButton uploadButton;

    public FileManagerPanel(Runnable closePopupCallback) {
        setLayout(new BorderLayout());

        // ---- Upload panel ----
        FileUpload uploadPanel = new FileUpload(this::addFileDescriptor);
        add(uploadPanel, BorderLayout.NORTH);

        // ---- File list panel ----
        fileList = new JPanel();
        fileList.setLayout(new BoxLayout(fileList, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(fileList);
        scroll.setPreferredSize(new Dimension(600, 400));
        add(scroll, BorderLayout.CENTER);

        // ---- Upload button ----
        uploadButton = new JButton("Upload");
        uploadButton.addActionListener(e -> handleUpload(closePopupCallback));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(uploadButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFileDescriptor(File file) {
        FileDescriptor desc = new FileDescriptor(file, Data.FILE_TAGS.tags(), this::removeFileDescriptor);
        fileList.add(desc);
        fileList.revalidate();
        fileList.repaint();
    }

    private void copyFile(Path file) {
        try {
            Files.copy(file, Constants.FILES.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void removeFileDescriptor(FileDescriptor desc) {
        fileList.remove(desc);
        fileList.revalidate();
        fileList.repaint();
    }

    private void handleUpload(Runnable closePopupCallback) {
        Component[] components = fileList.getComponents();
        List<FileDescriptor> toRemove = new ArrayList<>();

        for (Component c : components) {
            if (c instanceof FileDescriptor desc) {
                String author = desc.getAuthor().trim();
                if (!author.isEmpty()) {
                    // Collect for removal
                    toRemove.add(desc);

                    try {
                        DBUtils.addFile(desc.getFileName(), desc.getFileType(), desc.getAuthor(), desc.getSelectedTags());
                        // Copy file
                        copyFile(desc.getFilePath());
                        // Generate thumbnail
                        FileConverter.generateThumbnailAsync(desc.getFileName());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        // Remove the entries that had an author
        for (FileDescriptor desc : toRemove) {
            removeFileDescriptor(desc);
        }

        // Close the popup if all entries are gone
        if (fileList.getComponentCount() == 0) {
            if (closePopupCallback != null) {
                closePopupCallback.run();
            }
        }
    }
}
