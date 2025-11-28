package io.github.qpcrummer.spool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

public final class Constants {
    public static final Path WORKING_DIR = Paths.get("");
    public static final Path FILES = WORKING_DIR.resolve("files/");
    public static final Path TAGS_FILE = WORKING_DIR.resolve("tags.json");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Set<String> SUPPORTED_READING_FILE_TYPES = Set.of(
            "100", "10o", "bro", "col", "dat", "dsb", "dst", "dsz",
            "emd", "exp", "exy", "fxy", "gcode", "gt", "hqf",
            "hus", "inb", "jef", "jpx", "ksm", "max", "mit",
            "new", "pcd", "pcm", "pcq", "pcs", "pec", "pes",
            "phb", "phc", "sew", "shv", "spx", "stc", "stx",
            "tap", "tbf", "u01", "vp3", "xxx", "zhs", "zxy",
            "edr", "inf", "pmv", "iqp", "plt", "qcc", "hqv",
            "pdf"
    );
    public static final String[] SUPPORTED_WRITING_FILE_TYPES = {
            "dst", "exp", "gcode", "jef", "pec", "pes", "tbf",
            "u01", "vp3", "xxx", "col", "edr", "inf", "pmv",
            "plt", "qcc", "hqf"
    };
}
