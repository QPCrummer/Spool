package io.github.qpcrummer.spool.migrator;

import io.github.qpcrummer.spool.utils.OrderedTags;

import java.util.Set;

public record LegacyTags(Set<String> tags) {
    /**
     * Migrates from the legacy 2.0.1 tag storage system
     * @return New OrderTags implementation
     */
    public OrderedTags migrate() {
        return OrderedTags.fromList(tags.stream().toList());
    }
}
