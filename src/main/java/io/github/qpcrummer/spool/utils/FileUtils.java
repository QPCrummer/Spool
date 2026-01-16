package io.github.qpcrummer.spool.utils;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FileUtils {
    /**
     * Gets the file extension of a file's name
     * @param filename Filename to get extension from
     * @return Extension without period
     */
    public static String getFileExt(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }

    /**
     * Extracts a file into a new folder named the same as the zip
     * @param zipFile File to extract
     * @param zipPath File's path
     * @return The path to the extracted folder
     */
    public static Path unzip(File zipFile, Path zipPath) {
        String dir = zipPath.getFileName().toString().replace(".zip", "");
        Path output = zipPath.getParent().resolve(dir);

        try (ZipFile convertedZip = new ZipFile(zipFile)) {
            convertedZip.extractAll(output.toString());
        } catch (IOException e) {
            LoggerUtils.LOGGER.error("Failed to extract file", e);
        }
        return output;
    }

    /**
     * Gets the file's name from a String path
     * @param path File's path
     * @return File's name with extension
     */
    public static String getFileName(String path) {
        int index = path.lastIndexOf(File.separator);
        return path.substring(index + 1);
    }
}
