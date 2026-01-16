package io.github.qpcrummer.spool.gui.conversion;

import io.github.qpcrummer.spool.Constants;
import io.github.qpcrummer.spool.Data;
import io.github.qpcrummer.spool.utils.FileIOUtils;
import io.qt.core.Qt;
import io.qt.gui.QFont;
import io.qt.widgets.*;

import io.qt.widgets.*;

public class ConversionDialog extends QDialog {

    private final QComboBox formatCombo;

    public ConversionDialog(QWidget parent) {
        super(parent);
        setWindowTitle("Convert File");
        setModal(true);
        setMinimumWidth(420);

        setStyleSheet("""
            QDialog {
                background-color: #2e2e2e;
                color: white;
            }
            QLabel {
                color: white;
            }
            QComboBox {
                background-color: #3a3a3a;
                padding: 4px;
                border-radius: 4px;
            }
            QPushButton {
                background-color: #0078d7;
                border-radius: 4px;
                padding: 6px 14px;
            }
            QPushButton:disabled {
                background-color: #555555;
            }
        """);

        QVBoxLayout root = new QVBoxLayout(this);
        root.setSpacing(10);

        // ---------- Top Row ----------
        QHBoxLayout topRow = new QHBoxLayout();
        topRow.setSpacing(16);

        // Left: file info
        QLabel fileInfo = new QLabel(
                Data.SELECTED_FILE.path() + "\n(" + Data.SELECTED_FILE.fileType().toUpperCase() + ")"
        );
        fileInfo.setWordWrap(true);
        fileInfo.setAlignment(Qt.AlignmentFlag.AlignLeft);
        fileInfo.setFont(new QFont("Segoe UI", 10));

        // Middle: arrow
        QLabel arrow = new QLabel("→");
        arrow.setAlignment(Qt.AlignmentFlag.AlignCenter);
        arrow.setFont(new QFont("Segoe UI", 22));

        // Right: either dropdown OR warning
        QWidget rightWidget;
        boolean isPdf = Data.SELECTED_FILE.fileType().equalsIgnoreCase("pdf");

        if (isPdf) {
            QLabel warning = new QLabel("PDF files cannot be converted");
            warning.setStyleSheet("color: #ff5555;");
            warning.setWordWrap(true);
            rightWidget = warning;
            formatCombo = null;
        } else {
            formatCombo = new QComboBox();
            formatCombo.addItems(Constants.SUPPORTED_WRITING_FILE_TYPES);
            rightWidget = formatCombo;
        }

        topRow.addWidget(fileInfo, 2);
        topRow.addWidget(arrow, 0);
        topRow.addWidget(rightWidget, 1);

        // ---------- Convert Button ----------
        QPushButton convertBtn = new QPushButton("Convert");
        convertBtn.setEnabled(!isPdf);

        convertBtn.clicked.connect(() -> {
            String targetFormat = formatCombo.currentText();
            accept();
            FileIOUtils.convert(Data.SELECTED_FILE, targetFormat);
        });

        root.addLayout(topRow);
        root.addWidget(convertBtn, 0, Qt.AlignmentFlag.AlignCenter);
    }
}


