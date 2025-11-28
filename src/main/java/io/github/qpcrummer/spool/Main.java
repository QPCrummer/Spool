package io.github.qpcrummer.spool;

import io.github.qpcrummer.spool.database.DBUtils;
import io.github.qpcrummer.spool.database.Database;
import io.github.qpcrummer.spool.database.Tags;
import io.github.qpcrummer.spool.file.FileRecord;
import io.github.qpcrummer.spool.gui.ConvertMenu;
import io.github.qpcrummer.spool.gui.FileManagerPopup;
import io.github.qpcrummer.spool.gui.TagCheckboxList;
import io.github.qpcrummer.spool.gui.VirtualFileList;
import io.github.qpcrummer.spool.utils.Theme;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {
    private static final JPanel filterCheckList = new JPanel();
    private static FileManagerPopup fileManagerPopup;
    private static JFrame mainFrame;
    private static VirtualFileList fileList;
    private static final JPanel selectedFileDataPanel = new JPanel();

    public static void main(String[] args) {
        // Set up DB
        Database.init();

        // Create files directory
        createFilesDirectory();

        // Main window setup
        mainFrame = new JFrame();
        fileManagerPopup = new FileManagerPopup(mainFrame);
        mainFrame.setSize(800, 600);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setLayout(new GridBagLayout());
        mainFrame.setTitle("Spool");
        ImageIcon icon = new ImageIcon(
                Main.class.getResource("/SpoolIcon.png")
        );
        mainFrame.setIconImage(icon.getImage());
        mainFrame.getContentPane().setBackground(Theme.BACKGROUND);

        GridBagConstraints c = new GridBagConstraints();

        // Menu bar
        JMenuBar fileBar = new JMenuBar();
        c.weighty = 0.02;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.BOTH;
        fileBar.setBackground(Theme.ACCENT);
        mainFrame.add(fileBar, c);

        // File Menu Options
        JMenu addMenu = new JMenu("Add");
        JMenuItem addTag = new JMenuItem("Tag");
        addTag.addActionListener(_ -> onAddTag(mainFrame));
        addMenu.add(addTag);
        JMenuItem addFiles = new JMenuItem("Files");
        addFiles.addActionListener(_ -> onAddFiles());
        addMenu.add(addFiles);
        fileBar.add(addMenu);

        JMenu removeMenu = new JMenu("Remove");
        JMenuItem removeTag = new JMenuItem("Tag");
        removeTag.addActionListener(_ -> onRemoveTag(mainFrame));
        removeMenu.add(removeTag);
        fileBar.add(removeMenu);

        JMenu convertMenu = new JMenu("Convert");
        JMenuItem convertFile = new JMenuItem("Selected File");
        convertFile.addActionListener(_ -> convertFiles());
        convertMenu.add(convertFile);
        fileBar.add(convertMenu);

        // Horizontal Divider
        JSeparator hDiv = new JSeparator();
        c.gridy = 1;
        c.weighty = 0.02;
        c.gridwidth = 3;
        hDiv.setBackground(Theme.BACKGROUND);
        mainFrame.add(hDiv, c);

        // Control Panel
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(Theme.LIGHT_GRAY_BACKGROUND);
        c.gridy = 2;
        c.weighty = 0.9;
        c.weightx = 0.3;
        c.gridwidth = 1;
        renderControlPanel(controlPanel);
        mainFrame.add(controlPanel, c);

        // Vertical Divide
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        c.gridx = 1;
        c.weightx = 0.02;
        separator.setBackground(Theme.BACKGROUND);
        mainFrame.add(separator, c);

        // Display Panel
        Data.ACTIVE_FILES = Database.getAllFiles();
        fileList = new VirtualFileList(Data.ACTIVE_FILES);
        c.gridx = 2;
        c.weightx = 0.7;
        mainFrame.add(fileList.getPanel(), c);

        mainFrame.setVisible(true);
    }

    static void onAddTag(JFrame frame) {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(new JLabel("Create a Tag"));

        JTextField nameField = new JTextField("Name");
        popupMenu.add(nameField);

        JPanel buttonPanel = new JPanel();

        JButton create = new JButton("Create");
        create.addActionListener(_ -> {
            if (!Objects.equals(nameField.getText(), "Name")) {
                Tags.createTag(nameField.getText());
                updateTagsChecklist(nameField.getText());
                updateSelectedFile();
                popupMenu.setVisible(false);
            }
        });
        buttonPanel.add(create);

        JButton exit = new JButton("Exit");
        exit.addActionListener(_ -> popupMenu.setVisible(false));
        buttonPanel.add(exit);

        popupMenu.add(buttonPanel);

        popupMenu.setPopupSize(300, 100);
        popupMenu.show(frame, 200, 200);
    }

    static void onRemoveTag(JFrame frame) {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(new JLabel("Remove a Tag"));
        List<String> markedForRemoval = new ArrayList<>();

        JPanel checkPanel = new JPanel();
        checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS));

        for (String tag : Data.FILE_TAGS.tags()) {
            JCheckBox box = new JCheckBox(tag);
            box.addActionListener(_ -> {
                if (box.isSelected()) {
                    markedForRemoval.add(tag);
                } else {
                    markedForRemoval.remove(tag);
                }
            });
            checkPanel.add(box);
        }

        JScrollPane scrollPane = new JScrollPane(checkPanel);
        scrollPane.setMaximumSize(new Dimension(300, 300));
        popupMenu.add(scrollPane);

        JPanel buttonPanel = new JPanel();

        JButton remove = new JButton("Remove");
        remove.addActionListener(_ -> {
            Tags.removeTags(markedForRemoval);
            redrawChecklist();
            updateSelectedFile();
            popupMenu.setVisible(false);
        });
        buttonPanel.add(remove);

        JButton exit = new JButton("Exit");
        exit.addActionListener(_ -> popupMenu.setVisible(false));
        buttonPanel.add(exit);

        popupMenu.add(buttonPanel);

        popupMenu.setPopupSize(300, 400);
        popupMenu.show(frame, 200, 200);
    }

    static void onAddFiles() {
        fileManagerPopup.setVisible(true);
    }

    static void convertFiles() {
        if (Data.SELECTED_FILE != null) {
            ConvertMenu convertMenu = new ConvertMenu();
            convertMenu.setVisible(true);
        }
    }

    static void renderControlPanel(JPanel panel) {
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;

        JPanel filterLabelPanel = new JPanel();
        filterLabelPanel.setBackground(Theme.ACCENT);
        JLabel filterLabel = new JLabel("Filters");
        filterLabelPanel.add(filterLabel);
        c.weighty = 0.05;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(filterLabelPanel, c);

        renderTagsChecklist();
        c.gridy = 1;
        c.weighty = 0.4;
        c.fill = GridBagConstraints.BOTH;
        filterCheckList.setLayout(new BoxLayout(filterCheckList, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(filterCheckList);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel scrollContainer = new JPanel(new BorderLayout());
        scrollContainer.add(scroll, BorderLayout.CENTER);
        scrollContainer.setMinimumSize(new Dimension(0, 0));
        scrollContainer.setPreferredSize(new Dimension(0, 0));
        scrollContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        panel.add(scrollContainer, c);

        JSeparator separator = new JSeparator();
        c.gridy = 2;
        c.weighty = 0.02;
        panel.add(separator, c);

        c.weighty = 0.53;
        c.gridy = 3;
        selectedFileDataPanel.setLayout(new BoxLayout(selectedFileDataPanel, BoxLayout.Y_AXIS));
        updateSelectedFile();
        panel.add(selectedFileDataPanel, c);
    }

    static void updateTagsChecklist(String newTag) {
        JCheckBox box = new JCheckBox(newTag);
        box.setBackground(Theme.GRAY_MENU);
        filterCheckList.add(box);
        filterCheckList.revalidate();
        filterCheckList.repaint();
    }

    static void redrawChecklist() {
        filterCheckList.removeAll();
        renderTagsChecklist();
        filterCheckList.revalidate();
        filterCheckList.repaint();
    }

    static void renderTagsChecklist() {
        for (String tag : Data.FILE_TAGS.tags()) {
            JCheckBox box = new JCheckBox(tag);
            box.addActionListener(_ -> {
                if (box.isSelected()) {
                    Data.ACTIVE_FILTERS.add(tag);
                } else {
                    Data.ACTIVE_FILTERS.remove(tag);
                }
                try {
                    DBUtils.incrementalSearch(null, null, null, Data.ACTIVE_FILTERS);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            box.setBackground(Theme.GRAY_MENU);
            filterCheckList.add(box);
        }
    }

    public static void updateSearchList(List<FileRecord> files) {
        fileList.updateFiles(files);
    }

    public static void updateSelectedFile() {
        selectedFileDataPanel.removeAll();
        if (Data.SELECTED_FILE == null) {
            JLabel noFileSelected = new JLabel("No File Selected");
            selectedFileDataPanel.add(noFileSelected);
        } else {
            selectedFileDataPanel.add(new JLabel(Data.SELECTED_FILE.path()));
            JPanel sellerPanel = new JPanel();
            TextField seller = new TextField(Data.SELECTED_FILE.seller());
            seller.addActionListener(_ -> {
                try {
                    DBUtils.updateFile(Data.SELECTED_FILE.id(), null, seller.getText(),null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            sellerPanel.add(new JLabel("Seller: "));
            sellerPanel.add(seller);
            selectedFileDataPanel.add(sellerPanel);

            JPanel tagsPanel = new JPanel(new BorderLayout());
            tagsPanel.add(new JLabel("Tags:"), BorderLayout.NORTH);
            tagsPanel.add(new TagCheckboxList(Data.SELECTED_FILE.id()), BorderLayout.CENTER);
            selectedFileDataPanel.add(tagsPanel);

            JButton removeButton = new JButton("Delete");
            removeButton.addActionListener(_ -> {
                try {
                    DBUtils.removeFile(Data.SELECTED_FILE.id());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                Data.SELECTED_FILE = null;
                updateSelectedFile();
            });
            selectedFileDataPanel.add(removeButton);
        }
        selectedFileDataPanel.repaint();
        selectedFileDataPanel.revalidate();
    }

    private static void createFilesDirectory() {
        if (!Files.isDirectory(Constants.FILES)) {
            try {
                Files.createDirectory(Constants.FILES);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}