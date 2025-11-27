package io.github.qpcrummer.spool.database;

import io.github.qpcrummer.spool.Constants;
import io.github.qpcrummer.spool.Data;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record Tags(Set<String> tags) {
    public void serialize() {
        try (FileWriter writer = new FileWriter(Constants.TAGS_FILE.toFile())) {
            String data = Constants.GSON.toJson(this);
            writer.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Tags deserialize() {
        if (Files.notExists(Constants.TAGS_FILE)) {
            return new Tags(new HashSet<>());
        }
        try (FileReader reader = new FileReader(Constants.TAGS_FILE.toFile())) {
            return Constants.GSON.fromJson(reader, Tags.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createTag(String name) {
        Data.FILE_TAGS.tags.add(name);
        Data.FILE_TAGS.serialize();
    }

    public static void removeTags(List<String> names) {
        names.forEach(Data.FILE_TAGS.tags()::remove);
        Data.FILE_TAGS.serialize();
    }
}
