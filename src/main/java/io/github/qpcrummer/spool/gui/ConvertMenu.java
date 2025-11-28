package io.github.qpcrummer.spool.gui;

import io.github.qpcrummer.spool.Constants;
import io.github.qpcrummer.spool.Data;
import io.github.qpcrummer.spool.utils.FileConverter;

import javax.swing.*;

public class ConvertMenu extends JDialog {
    public ConvertMenu() {
        this.setSize(400, 200);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(Data.SELECTED_FILE.path()));

        JPanel convertToPanel = new JPanel();
        convertToPanel.add(new JLabel("From " + Data.SELECTED_FILE.fileType() + " to "));
        JComboBox<String> dropdown = new JComboBox<>(Constants.SUPPORTED_WRITING_FILE_TYPES);
        convertToPanel.add(dropdown);
        panel.add(convertToPanel);

        JButton button = new JButton("Convert");
        button.addActionListener(_ -> {
            if (dropdown.getSelectedItem() != null) {
                FileConverter.convert(Data.SELECTED_FILE, (String) dropdown.getSelectedItem());
                this.setVisible(false);
                this.dispose();
            }
        });
        panel.add(button);

        JButton exit = new JButton("Exit");
        exit.addActionListener(_ -> {
            this.setVisible(false);
            this.dispose();
        });
        panel.add(exit);
        this.add(panel);
    }

}
