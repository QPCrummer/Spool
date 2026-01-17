package io.github.qpcrummer.spool;

import io.github.qpcrummer.spool.file.FileRecord;
import io.github.qpcrummer.spool.utils.FileIOUtils;
import io.github.qpcrummer.spool.utils.OrderedTags;
import io.qt.core.QModelIndex;

import java.util.ArrayList;
import java.util.List;

public class Data {
    /**
     * All loaded tags
     */
    public static OrderedTags FILE_TAGS = FileIOUtils.deserializeTags();
    /**
     * Filters being applied to the file list
     */
    public static final OrderedTags ACTIVE_FILTERS = new OrderedTags();
    /**
     * Files in the file list
     */
    public static List<FileRecord> ACTIVE_FILES = new ArrayList<>();
    /**
     * The selected file in the file list
     */
    public static FileRecord SELECTED_FILE;
    /**
     * The tags of the selected file {@link Data#SELECTED_FILE}
     */
    public static final List<String> SELECTED_FILE_TAGS = new ArrayList<>();
    /**
     * The index in the file list of the selected file {@link Data#SELECTED_FILE}
     */
    public static QModelIndex SELECTED_INDEX;
}
