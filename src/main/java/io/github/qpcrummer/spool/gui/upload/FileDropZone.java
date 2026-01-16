package io.github.qpcrummer.spool.gui.upload;

import io.qt.core.QUrl;
import io.qt.core.Qt;
import io.qt.gui.*;
import io.qt.widgets.QSizePolicy;
import io.qt.widgets.QWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FileDropZone extends QWidget {

    private final Consumer<List<String>> onFilesDropped;

    public FileDropZone(Consumer<List<String>> onFilesDropped) {
        this.onFilesDropped = onFilesDropped;

        setAcceptDrops(true);

        setFixedHeight(50);
        setSizePolicy(QSizePolicy.Policy.Expanding, QSizePolicy.Policy.Fixed);
        setAttribute(Qt.WidgetAttribute.WA_StyledBackground, true);

        setStyleSheet("""
            QWidget {
                border: 2px solid #0078d7;
                border-radius: 6px;
                background-color: #444444;
            }
        """);
    }

    @Override
    protected void dragEnterEvent(QDragEnterEvent event) {
        if (event.mimeData().hasUrls()) {
            event.acceptProposedAction();
        }
    }

    @Override
    protected void dropEvent(QDropEvent event) {
        List<String> paths = new ArrayList<>();
        for (QUrl url : event.mimeData().urls()) {
            paths.add(url.toLocalFile());
        }
        onFilesDropped.accept(paths);
    }

    @Override
    protected void paintEvent(QPaintEvent event) {
        super.paintEvent(event);

        QPainter painter = new QPainter(this);
        painter.setPen(QColor.fromString("lightGray"));
        painter.drawText(rect(), Qt.AlignmentFlag.AlignCenter.value(), "Drag files here");
    }
}