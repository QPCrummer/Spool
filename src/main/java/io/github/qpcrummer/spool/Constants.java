package io.github.qpcrummer.spool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public final class Constants {
    /**
     * Java working directory
     */
    public static final Path WORKING_DIR = Paths.get("");
    /**
     * Files directory path
     */
    public static final Path FILES = WORKING_DIR.resolve("files/");
    /**
     * Tags json path
     */
    public static final Path TAGS_FILE = WORKING_DIR.resolve("tags.json");
    /**
     * Global GSON instance
     */
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    /**
     * Supported file extensions that can be imported
     */
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
    /**
     * Supported file extensions that can be exported (converted)
     */
    public static final String[] SUPPORTED_WRITING_FILE_TYPES = {
            "dst", "exp", "gcode", "jef", "pec", "pes", "tbf",
            "u01", "vp3", "xxx", "col", "edr", "inf", "pmv",
            "plt", "qcc", "hqf"
    };
}
