package io.github.qpcrummer.spool.gui_2.file_list;

import io.qt.core.Qt;
import io.qt.gui.QPixmap;
import io.qt.widgets.QDialog;
import io.qt.widgets.QLabel;
import io.qt.widgets.QVBoxLayout;
import io.qt.widgets.QWidget;

public class ImagePopup extends QDialog {

    public ImagePopup(QPixmap pixmap, QWidget parent) {
        super(parent);

        setWindowTitle("Preview");
        setModal(true);
        resize(512, 512);

        QLabel imageLabel = new QLabel();
        imageLabel.setAlignment(Qt.AlignmentFlag.AlignCenter);
        imageLabel.setPixmap(
                pixmap.scaled(
                        512, 512,
                        Qt.AspectRatioMode.KeepAspectRatio,
                        Qt.TransformationMode.SmoothTransformation
                )
        );

        QVBoxLayout layout = new QVBoxLayout(this);
        layout.addWidget(imageLabel);
    }
}

