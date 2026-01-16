package io.github.qpcrummer.spool.gui.file_list;

import io.github.qpcrummer.spool.Data;
import io.github.qpcrummer.spool.file.FileRecord;
import io.qt.core.*;

import java.util.List;

public class FileListModel extends QAbstractListModel {
    private final List<FileRecord> files = Data.ACTIVE_FILES;

    @Override
    public int rowCount(QModelIndex parent) {
        return files.size();
    }

    @Override
    public QVariant data(QModelIndex index, int role) {
        if (!index.isValid() || index.row() < 0 || index.row() >= files.size()) {
            return new QVariant();
        }

        FileRecord file = files.get(index.row());

        if (role == Qt.ItemDataRole.UserRole) {
            return new QVariant(file);
        }

        return new QVariant();
    }

    public void addFile(FileRecord file) {
        int row = files.size();
        beginInsertRows(new QModelIndex(), row, row);
        files.add(file);
        endInsertRows();
    }

    public void removeFileAt(int index) {
        if (index < 0 || index >= files.size()) {
            return;
        }

        beginRemoveRows(new QModelIndex(), index, index);
        files.remove(index);
        endRemoveRows();
    }

    public void removeFileById(int id) {
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).id() == id) {
                removeFileAt(i);
                return;
            }
        }
    }

    public void updateFile(QModelIndex index, FileRecord updated) {
        if (!index.isValid()) {
            return;
        }

        int row = index.row();
        if (row < 0 || row >= files.size()) {
            return;
        }

        files.set(row, updated);
        dataChanged.emit(index, index);
    }

    public void setFiles(List<FileRecord> newFiles) {
        beginResetModel();
        files.clear();
        files.addAll(newFiles);
        endResetModel();
    }

    public FileRecord getFile(int row) {
        return files.get(row);
    }
}
