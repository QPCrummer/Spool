package io.github.qpcrummer.spool.database;

import java.util.List;

public record FileQuery(String seller, String fileNameLike, String fileType, List<String> requiredTags) {}
