package io.github.qpcrummer.spool.gui;

import javax.swing.*;

public class FileManagerPopup extends JDialog {

    public FileManagerPopup(JFrame parent) {
        super(parent, "File Manager", false);

        setContentPane(new FileManagerPanel(this::dispose));
        setSize(600, 500);
        setLocationRelativeTo(parent);
    }
}

