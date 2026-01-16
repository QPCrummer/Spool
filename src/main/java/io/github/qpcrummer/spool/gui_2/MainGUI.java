package io.github.qpcrummer.spool.gui_2;

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

        QApplication.exec();
    }

    private static QMainWindow createMainWindow() {
        QMainWindow window = new QMainWindow();
        window.setWindowTitle("Spool");
        InputStream iconStream;
        try {
            iconStream = MainGUI.class.getResource("/SpoolIcon.png").openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            System.err.println("Icon not found!");
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

        splitter.setSizes(List.of(100, 100, 400));
        return splitter;
    }
}
