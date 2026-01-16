package io.github.qpcrummer.spool.gui;

import io.github.qpcrummer.spool.utils.LoggerUtils;
import io.qt.core.Qt;
import io.qt.gui.QIcon;
import io.qt.gui.QPixmap;
import io.qt.widgets.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainGUI {
    public static void init(String[] args) {
        QApplication.initialize(args);

        QMainWindow window = createMainWindow();
        window.show();

        window.destroyed.connect(() -> System.exit(0));

        QApplication.exec();
    }

    private static QMainWindow createMainWindow() {
        QMainWindow window = new QMainWindow();
        window.setWindowTitle("Spool");
        try (InputStream iconStream = MainGUI.class.getResource("/SpoolIcon.png").openStream()) {
            if (iconStream != null) {
                byte[] bytes;
                try {
                    bytes = iconStream.readAllBytes();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                QPixmap pixmap = new QPixmap();
                pixmap.loadFromData(bytes);
                QIcon icon = new QIcon(pixmap);
                window.setWindowIcon(icon);
            } else {
                LoggerUtils.LOGGER.warn("Icon not found");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        window.resize(1200, 800);

        window.setCentralWidget(createRootSplitter());
        return window;
    }

    // Root splitter (left / right)
    private static QSplitter createRootSplitter() {
        QSplitter splitter = new QSplitter(Qt.Orientation.Horizontal);

        splitter.addWidget(createLeftSplitter());
        splitter.addWidget(FilePanel.createFilesWindow());

        splitter.setSizes(List.of(500, 700));
        return splitter;
    }

    // Left side splitter (top / middle / bottom)
    private static QSplitter createLeftSplitter() {
        QSplitter splitter = new QSplitter(Qt.Orientation.Vertical);

        splitter.addWidget(UploadPanel.init());
        splitter.addWidget(TagManagerPanel.init());
        splitter.addWidget(InfoPanel.init());

        splitter.setSizes(List.of(200, 200, 200));
        return splitter;
    }
}
