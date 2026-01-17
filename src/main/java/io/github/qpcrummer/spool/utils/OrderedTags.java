package io.github.qpcrummer.spool.utils;

import com.google.gson.annotations.Expose;

import java.util.*;

public class OrderedTags {
    @Expose(serialize = false, deserialize = false)
    private final NavigableSet<String> TAGS = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    @Expose()
    private final List<String> TAGS_LIST = new ArrayList<>();

    /**
     * Add a tag in the correct alphabetical position
     * @param tag Tag to add
     */
    public void add(String tag) {
        TAGS.add(tag);
        updateList();
    }

    /**
     * Removes a tag
     * @param tag Tag to remove
     */
    public void remove(String tag) {
        TAGS.remove(tag);
        TAGS_LIST.remove(tag);
    }

    /**
     * Converts the alphabetically ordered list into a List instance
     * @return Converts to Java List instance
     */
    public List<String> toList() {
        return TAGS_LIST;
    }

    /**
     * If the tags is present in the list
     * @param tag Tag to check
     * @return If the tag is present
     */
    public boolean contains(String tag) {
        return TAGS.contains(tag);
    }

    /**
     * Generates a new OrderedTags instance from a List
     * @param tags Unordered List of tags
     */
    public static OrderedTags fromList(List<String> tags) {
        OrderedTags result = new OrderedTags();
        result.TAGS_LIST.clear();
        result.TAGS_LIST.addAll(tags);
        result.updateList();
        return result;
    }

    private void updateList() {
        TAGS_LIST.clear();
        TAGS_LIST.addAll(TAGS);
    }
}
