package io.github.qpcrummer.spool.utils;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FileUtils {
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

    public static Path unzip(File zipFile, Path zipPath) {
        String dir = zipPath.getFileName().toString().replace(".zip", "");
        Path output = zipPath.getParent().resolve(dir);

        try (ZipFile convertedZip = new ZipFile(zipFile)) {
            convertedZip.extractAll(output.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static String getFileName(String path) {
        int index = path.lastIndexOf(File.separator);
        return path.substring(index + 1);
    }
}
