package io.github.qpcrummer.spool.gui;

import io.github.qpcrummer.spool.Constants;
import io.github.qpcrummer.spool.Data;
import io.github.qpcrummer.spool.Main;
import io.github.qpcrummer.spool.database.DBUtils;
import io.github.qpcrummer.spool.file.FileRecord;
import io.github.qpcrummer.spool.utils.FileConverter;
import io.github.qpcrummer.spool.utils.LoggerUtils;
import io.github.qpcrummer.spool.utils.Theme;
import io.github.qpcrummer.spool.utils.USBUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class VirtualFileList {
    public static JList<FileRecord> fileJList;
    private final DefaultListModel<FileRecord> listModel;
    private final JTextField searchField;
    private final JPanel mainPanel;
    private static final int BUTTON_HEIGHT = 30;
    private static final int BUTTON_WIDTH = 100;

    public VirtualFileList(List<FileRecord> files) {
        mainPanel = new JPanel(new BorderLayout());

        // ---- Top search bar ----
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(30);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(_ -> fuzzySearch(searchField.getText()));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.setBackground(Theme.ACCENT);
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // ---- List model ----
        listModel = new DefaultListModel<>();
        for (FileRecord file : files) {
            listModel.addElement(file);
        }

        // ---- JList setup ----
        fileJList = new JList<>(listModel);
        fileJList.setBackground(Theme.LIGHT_GRAY_BACKGROUND);
        fileJList.setCellRenderer(new FileCellRenderer());
        fileJList.setFixedCellHeight(80); // match previous entry height
        JScrollPane scrollPane = new JScrollPane(fileJList);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        fileJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = fileJList.locationToIndex(e.getPoint());
                if (index < 0) {
                    return;
                }

                FileRecord file = listModel.get(index);

                Rectangle cellBounds = fileJList.getCellBounds(index, index);
                int relativeX = e.getX() - cellBounds.x;
                int relativeY = e.getY() - cellBounds.y;

                fileJList.setSelectedIndex(index);
                Data.SELECTED_FILE = file;
                Main.updateSelectedFile();

                // ---- Image click (left side) ----
                if (relativeX < 60) { // image width
                    JDialog dialog = new JDialog();
                    dialog.setTitle("Preview: " + file.path());
                    dialog.setSize(750, 750);
                    dialog.setLocationRelativeTo(fileJList);
                    JPanel preview = new JPanel();
                    renderThumbnail(file, preview, new Dimension(700, 700));
                    dialog.add(preview);
                    dialog.setVisible(true);
                }

                // Open Location button
                if (relativeX > cellBounds.width - BUTTON_WIDTH &&
                        relativeY < BUTTON_HEIGHT) {
                    LoggerUtils.LOGGER.info("Opening file location");
                    openFileLocation(Constants.FILES.resolve(file.path()).toFile());
                }

                // Export to USB button
                if (relativeX > cellBounds.width - BUTTON_WIDTH &&
                        relativeY >= BUTTON_HEIGHT &&
                        relativeY < 2 * BUTTON_HEIGHT) {
                    exportToUSB(file); // call your method
                }
            }
        });
    }

    private void exportToUSB(FileRecord file) {
        LoggerUtils.LOGGER.info("Export to USB method called for file: {}", file.path());
        if (!USBUtils.copyFileToUSB(Constants.FILES.resolve(file.path()))) {
            JOptionPane.showMessageDialog(null,
                    "No USB drive detected!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public void updateFiles(List<FileRecord> newFiles) {
        listModel.clear();
        newFiles.forEach(listModel::addElement);
    }

    // ---- Custom cell renderer ----
    private static class FileCellRenderer extends JPanel implements ListCellRenderer<FileRecord> {

        private final JLabel nameLabel = new JLabel();
        private final JLabel sellerLabel = new JLabel();
        private final JPanel infoPanel;

        public FileCellRenderer() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

            // Static text area
            infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.add(nameLabel);
            infoPanel.add(sellerLabel);
            add(infoPanel, BorderLayout.CENTER);

            // Right side buttons
            JPanel rightPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(0, 0, getWidth(), BUTTON_HEIGHT);
                    g.setColor(Color.BLACK);
                    g.drawString("Open Location", 10, 20);
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(0, BUTTON_HEIGHT, getWidth(), BUTTON_HEIGHT);
                    g.setColor(Color.BLACK);
                    g.drawString("Export to USB", 10, 20 + BUTTON_HEIGHT);
                }
            };
            rightPanel.setPreferredSize(new Dimension(BUTTON_WIDTH, 2 * BUTTON_HEIGHT));
            add(rightPanel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends FileRecord> list,
                                                      FileRecord file,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {

            removeAll(); // IMPORTANT

            setLayout(new BorderLayout());

            // ==== IMAGE PANEL IS CREATED PER ROW ====
            JPanel imagePanel = new JPanel();
            imagePanel.setPreferredSize(new Dimension(60, 60));

            renderThumbnail(file, imagePanel, new Dimension(60, 60));
            add(imagePanel, BorderLayout.WEST);

            // Name / seller text
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

            nameLabel.setText("Name: " + file.path());
            sellerLabel.setText("Seller: " + file.seller());
            infoPanel.add(nameLabel);
            infoPanel.add(sellerLabel);
            add(infoPanel, BorderLayout.CENTER);

            // Buttons panel
            JPanel rightPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(0, 0, getWidth(), BUTTON_HEIGHT);
                    g.setColor(Color.BLACK);
                    g.drawString("Open Location", 10, 20);
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(0, BUTTON_HEIGHT, getWidth(), BUTTON_HEIGHT);
                    g.setColor(Color.BLACK);
                    g.drawString("Export to USB", 10, 20 + BUTTON_HEIGHT);
                }
            };
            rightPanel.setPreferredSize(new Dimension(BUTTON_WIDTH, 2 * BUTTON_HEIGHT));
            add(rightPanel, BorderLayout.EAST);

            if (isSelected) {
                infoPanel.setBackground(Theme.SELECTED);
                rightPanel.setBackground(Theme.SELECTED);
                setBackground(Theme.SELECTED);
            } else {
                infoPanel.setBackground(Color.WHITE);
                rightPanel.setBackground(Color.WHITE);
                setBackground(Color.WHITE);
            }

            return this;
        }
    }


    private static void renderThumbnail(FileRecord file, JPanel imagePanel, Dimension dimension) {
       renderThumbnail(file.path(), imagePanel, dimension);
    }

    private static void renderThumbnail(String file, JPanel imagePanel, Dimension dim) {
        File embroideryFile = Constants.FILES.resolve(file).toFile();
        File png = null;

        if (png != null && png.exists()) {
            try {
                BufferedImage buffImg = ImageIO.read(png);
                if (buffImg != null) {

                    BufferedImage img = multiStepResize(buffImg, dim.width, dim.height);

                    imagePanel.removeAll();
                    imagePanel.add(new JLabel(new ImageIcon(img)));
                    imagePanel.setBackground(Color.GRAY);

                } else {
                    imagePanel.setBackground(Color.BLUE);
                }
            } catch (IOException ignored) {
                imagePanel.setBackground(Color.RED);
            }
        } else {
            imagePanel.setBackground(Color.RED);
        }
    }

    private static BufferedImage multiStepResize(BufferedImage img, int targetW, int targetH) {
        int originalW = img.getWidth();
        int originalH = img.getHeight();

        // Compute scale to preserve aspect ratio
        double scale = Math.min((double) targetW / originalW, (double) targetH / originalH);
        int newW = (int) (originalW * scale);
        int newH = (int) (originalH * scale);

        BufferedImage current = img;
        int w = originalW;
        int h = originalH;

        // Multi-step downscale (halve repeatedly)
        while (w > newW * 2 || h > newH * 2) {
            w = Math.max(newW, w / 2);
            h = Math.max(newH, h / 2);

            BufferedImage temp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = temp.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(current, 0, 0, w, h, null);
            g.dispose();

            current = temp;
        }

        // Final high-quality bicubic pass
        BufferedImage finalImg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = finalImg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(current, 0, 0, newW, newH, null);
        g.dispose();

        return finalImg;
    }


    public static void updateThumbnail() {
        fileJList.repaint();
        fileJList.revalidate();
    }

    private void fuzzySearch(String query) {
        DBUtils.incrementalSearch(null, query, null, null);
    }

    public static void openFileLocation(File file) {
        try {
            String absolute = file.getAbsolutePath();

            // --- WINDOWS ---
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                Runtime.getRuntime().exec(new String[]{
                        "explorer.exe", "/select,", absolute
                });
                return;
            }

            // --- LINUX / UNIX ---
            String parent = file.getParent();

            // Try common Linux file managers
            String[][] cmds = {
                    {"nautilus", "--select", absolute},
                    {"dolphin", "--select", absolute},
                    {"nemo", absolute},
                    {"thunar", parent}
            };

            for (String[] cmd : cmds) {
                try {
                    new ProcessBuilder(cmd).start();
                    return;
                } catch (Exception ignored) {}
            }

            // Fallback
            Desktop.getDesktop().open(new File(parent));

        } catch (Exception e) {
            LoggerUtils.LOGGER.warn("Failed to open file", e);
        }
    }

}


