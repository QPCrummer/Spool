package io.github.qpcrummer.spool;

import io.github.qpcrummer.spool.database.Tags;
import io.github.qpcrummer.spool.file.FileRecord;
import io.qt.core.QModelIndex;

import java.util.ArrayList;
import java.util.List;

public class Data {
    public static Tags FILE_TAGS = Tags.deserialize();
    public static final List<String> ACTIVE_FILTERS = new ArrayList<>();
    public static List<FileRecord> ACTIVE_FILES = new ArrayList<>();
    public static FileRecord SELECTED_FILE;
    public static final List<String> SELECTED_FILE_TAGS = new ArrayList<>();
    public static QModelIndex SELECTED_INDEX;
}
