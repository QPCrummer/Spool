package io.github.qpcrummer.spool.gui;

import io.github.qpcrummer.spool.Constants;
import io.github.qpcrummer.spool.utils.FileUtils;
import io.github.qpcrummer.spool.utils.LoggerUtils;
import io.github.qpcrummer.spool.utils.Theme;
import net.lingala.zip4j.ZipFile;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class FileUpload extends JPanel {
    public interface FileUploadListener {
        void onFileUploaded(File file);
    }

    public FileUpload(FileUploadListener listener) {
        setBorder(BorderFactory.createDashedBorder(Color.GRAY));
        setPreferredSize(new Dimension(400, 120));
        setLayout(new BorderLayout());
        setBackground(Theme.ACCENT);

        JLabel label = new JLabel("Drag files here to upload", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
        add(label, BorderLayout.CENTER);

        setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    java.util.List<File> files = (java.util.List<File>) support.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);

                    for (File f : files) {
                        String fileExt = FileUtils.getFileExt(f.getName()).toLowerCase(Locale.ROOT);
                        // Handle zip files by extracting them
                        if (fileExt.equals("zip")) {
                            Path unzippedFolder = FileUtils.unzip(f, f.toPath());
                            try (DirectoryStream<Path> stream = Files.newDirectoryStream(unzippedFolder)) {
                                for (Path path : stream) {
                                    String subFileExt = FileUtils.getFileExt(path.getFileName().toString()).toLowerCase(Locale.ROOT);
                                    if (Constants.SUPPORTED_READING_FILE_TYPES.contains(subFileExt)) {
                                        File file = path.toFile();
                                        listener.onFileUploaded(file);
                                    }
                                }
                            }
                        } else if (Constants.SUPPORTED_READING_FILE_TYPES.contains(fileExt)) {
                            listener.onFileUploaded(f);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "Unsupported File Type: " + fileExt,
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    return true;

                } catch (Exception e) {
                    LoggerUtils.LOGGER.warn("Failed to upload file(s)", e);
                }
                return false;
            }
        });
    }
}
