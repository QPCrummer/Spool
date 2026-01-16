package io.github.qpcrummer.spool.gui_2;

import io.github.qpcrummer.spool.Data;
import io.github.qpcrummer.spool.database.DBUtils;
import io.github.qpcrummer.spool.file.FileRecord;
import io.github.qpcrummer.spool.gui_2.file_list.FileItemDelegate;
import io.github.qpcrummer.spool.gui_2.file_list.FileListModel;
import io.qt.core.QItemSelection;
import io.qt.core.QMetaObject;
import io.qt.core.QModelIndex;
import io.qt.core.Qt;
import io.qt.widgets.*;

import java.util.List;

public class FilePanel {
    private static QMenu filterMenu;
    private static FileListModel model;
    private static final QListView listView = new QListView();

    public static QWidget createFilesWindow() {
        QWidget panel = new QWidget();
        panel.setMinimumWidth(400);
        panel.setStyleSheet("background-color: #1e1e1e");

        QVBoxLayout layout = new QVBoxLayout(panel);
        layout.setContentsMargins(6, 6, 6, 6);
        layout.setSpacing(6);

        layout.addWidget(createSearchBar(), 0);
        layout.addWidget(createRightContent(), 1);

        return panel;
    }

    private static QWidget createSearchBar() {
        QWidget bar = new QWidget();
        QHBoxLayout layout = new QHBoxLayout(bar);
        layout.setContentsMargins(0, 0, 0, 0);
        layout.setSpacing(6);

        // Search field
        QLineEdit searchField = new QLineEdit();
        searchField.setPlaceholderText("Search...");
        searchField.returnPressed.connect(() -> performSearch(searchField.text()));

        // Search button
        QPushButton searchButton = new QPushButton("Search");
        searchButton.clicked.connect(() -> performSearch(searchField.text()));

        // Filter dropdown
        QToolButton filterButton = new QToolButton();
        filterButton.setText("Filters");
        filterButton.setPopupMode(QToolButton.ToolButtonPopupMode.InstantPopup);

        filterMenu = new QMenu(filterButton);

        rebuildFilterMenu();

        filterButton.setMenu(filterMenu);

        layout.addWidget(searchField, 1);
        layout.addWidget(searchButton);
        layout.addWidget(filterButton);

        return bar;
    }

    private static QWidget createRightContent() {
        QWidget content = new QWidget();
        QVBoxLayout layout = new QVBoxLayout(content);
        layout.addWidget(createFileListView());
        return content;
    }

    public static void rebuildFilterMenu() {
        filterMenu.clear();

        for (String tag : Data.FILE_TAGS.tags()) {
            // Create a checkbox for the filter
            QCheckBox checkBox = new QCheckBox(tag);

            // Restore state if active
            checkBox.setChecked(Data.ACTIVE_FILTERS.contains(tag));

            // Connect toggled signal
            checkBox.toggled.connect(checked -> applyFilter(tag, checked));

            // Create a QWidgetAction to hold the checkbox
            QWidgetAction widgetAction = new QWidgetAction(filterMenu);
            widgetAction.setDefaultWidget(checkBox);

            // Add the action (with embedded checkbox) to the menu
            filterMenu.addAction(widgetAction);
        }
    }

    private static void performSearch(String query) {
        DBUtils.incrementalSearch(null, query, null, null);
    }

    private static void applyFilter(String filter, boolean toggle) {
        if (toggle) {
            Data.ACTIVE_FILTERS.add(filter);
        } else {
            Data.ACTIVE_FILTERS.remove(filter);
        }
        try {
            DBUtils.incrementalSearch(null, null, null, Data.ACTIVE_FILTERS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // List
    public static QListView createFileListView() {
        listView.setMouseTracking(true); // enables hover

        model = new FileListModel();
        listView.setModel(model);

        FileItemDelegate delegate = new FileItemDelegate();
        listView.setItemDelegate(delegate);

        listView.setSelectionMode(QAbstractItemView.SelectionMode.SingleSelection);

        listView.setStyleSheet("""
        QListView {
            background-color: #1e1e1e;
            border: none;
        }
    """);

        listView.selectionModel().selectionChanged.connect((QItemSelection selected, QItemSelection _) -> {
            if (!selected.indexes().isEmpty()) {
                QModelIndex index = selected.indexes().getFirst();
                Data.SELECTED_INDEX = index;
                FileRecord record = model.getFile(index.row());
                InfoPanel.setSelectedFile(record);
            } else {
                InfoPanel.clear();
            }
        });

        return listView;
    }

    /**
     * Gets the model to delete or add items
     * @return FileListModel instance
     */
    public static FileListModel getModel() {
        return model;
    }

    /**
     * Update using this method when a filter is applied
     * @param filteredFiles
     */
    public static void updateSearchList(List<FileRecord> filteredFiles) {
        QMetaObject.invokeMethod(
                model,
                () -> model.setFiles(filteredFiles),
                Qt.ConnectionType.QueuedConnection
        );
    }
}
