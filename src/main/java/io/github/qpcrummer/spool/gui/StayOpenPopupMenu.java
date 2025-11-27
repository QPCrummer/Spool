package io.github.qpcrummer.spool.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StayOpenPopupMenu extends JPopupMenu {

    private final JPanel contentPanel;
    private final List<JCheckBox> checkBoxes = new ArrayList<>();

    public StayOpenPopupMenu(Set<String> tags, List<String> selected) {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        for (String tag : tags) {
            JCheckBox cb = new JCheckBox(tag);
            cb.addActionListener(_ -> {
                if (cb.isSelected()) {
                    selected.add(tag);
                } else {
                    selected.remove(tag);
                }
            });
            checkBoxes.add(cb);
            contentPanel.add(cb);
        }

        add(contentPanel);

        // Close when clicking outside
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (!(event instanceof MouseEvent me)) return;
            if (me.getID() != MouseEvent.MOUSE_PRESSED) return;

            Component clicked = me.getComponent();
            if (SwingUtilities.getWindowAncestor(clicked) != SwingUtilities.getWindowAncestor(this)) {
                setVisible(false);
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }
}
