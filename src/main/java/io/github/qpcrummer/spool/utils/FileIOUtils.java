package io.github.qpcrummer.spool.utils;

import com.google.gson.reflect.TypeToken;
import io.github.qpcrummer.spool.Constants;
import io.github.qpcrummer.spool.Data;
import io.github.qpcrummer.spool.database.DBUtils;
import io.github.qpcrummer.spool.database.Database;
import io.github.qpcrummer.spool.file.FileRecord;
import io.github.qpcrummer.spool.file.UploadRecord;
import io.github.qpcrummer.spool.gui.FilePanel;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileIOUtils {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final int MAX_THUMB_SIZE = 1024;

    /**
     * Processes a list of File names and generates their thumbnails concurrently
     * @param trimmed The UploadRecords that are being processed
     */
    public static void handleFileSUpload(List<UploadRecord> trimmed) {
        EXECUTOR.execute(() -> {
            for (UploadRecord file : trimmed) {
                try {
                    copyFile(file.getPath());
                    DBUtils.addFile(file.getFileRecord().path(), file.getFileRecord().fileType(), file.getFileRecord().seller(), file.getTags());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            List<String> names = trimmed.stream().map(uploadRecord -> uploadRecord.getFileRecord().path()).toList();
            FileIOUtils.processImageConversions(names);
        });
    }

    private static void processImageConversions(List<String> filesNames) {
        filesNames.parallelStream().forEach(FileIOUtils::generateThumbnail);
        FilePanel.refreshVisibleThumbnails();
    }

    /**
     * Generates a PNG thumbnail from an embroidery file.
     * @param embroideryFileName The source file path after being moved
     */
    private static void generateThumbnail(String embroideryFileName) {
        String pngFileName = embroideryFileName + ".png";

        if (embroideryFileName.contains(".pdf")) {
            generateThumbnailPDF(Constants.FILES.resolve(embroideryFileName), Constants.FILES.resolve(pngFileName));
        } else {
            // Python one-liner script
            String script =
                    "import pystitch\n" +
                            "p=pystitch.read(r'" + embroideryFileName + "')\n" +
                            "pystitch.write(p, r'" + pngFileName + "')\n";

            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "python", "-c", script
                );

                pb.redirectErrorStream(true);
                pb.directory(Constants.FILES.toFile());
                Process process = pb.start();

                int exit = process.waitFor();
                if (exit != 0) {
                    throw new IOException("Thumbnail generation failed: exit code " + exit);
                }
            } catch (Exception ex) {
                LoggerUtils.LOGGER.warn("Failed to generate thumbnail", ex);
            }
        }
    }

    private static void copyFile(Path file) {
        try {
            Files.copy(file, Constants.FILES.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void generateThumbnailPDF(Path pdfPath, Path pngPath) {
        try (PDDocument document = Loader.loadPDF(
                new RandomAccessReadBufferedFile(pdfPath))) {

            PDFRenderer renderer = new PDFRenderer(document);
            PDPage page = document.getPage(0);

            float widthPt = page.getMediaBox().getWidth();
            float heightPt = page.getMediaBox().getHeight();

            float widthIn = widthPt / 72f;
            float heightIn = heightPt / 72f;

            float dpiX = MAX_THUMB_SIZE / widthIn;
            float dpiY = MAX_THUMB_SIZE / heightIn;
            float dpi = Math.min(Math.min(dpiX, dpiY), 300f);

            BufferedImage image = renderer.renderImageWithDPI(
                    0,
                    dpi,
                    ImageType.RGB
            );

            ImageIO.write(image, "PNG", pngPath.toFile());
            LoggerUtils.LOGGER.info("Converted PDF to PNG at {} DPI", dpi);

        } catch (IOException e) {
            LoggerUtils.LOGGER.warn("Failed to convert PDF to PNG", e);
        }
    }

    /**
     * Converts between two file types. The original file is not changed.
     * @param file Input file
     * @param toFileType Type of file to convert to
     */
    public static void convert(FileRecord file, String toFileType) {
        EXECUTOR.execute(() -> {
            String convertedFile = file.path().replaceAll("\\.[^.]+$", "") + "." + toFileType.toLowerCase(Locale.ROOT);

            // Python one-liner script
            String script =
                    "import pystitch\n" +
                            "p=pystitch.read(r'" + file.path() + "')\n" +
                            "pystitch.write(p, r'" + convertedFile + "')\n";

            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "python", "-c", script
                );

                pb.redirectErrorStream(true);
                pb.directory(Constants.FILES.toFile());
                Process process = pb.start();

                int exit = process.waitFor();
                if (exit != 0) {
                    throw new IOException("Conversion failed: exit code " + exit);
                }

                DBUtils.addFile(convertedFile, toFileType.toUpperCase(Locale.ROOT), file.seller(), Database.getTagsForFile(file.id()));
            } catch (Exception ex) {
                LoggerUtils.LOGGER.warn("Failed to convert file", ex);
            }
        });
    }

    /**
     * Write the tags to file
     */
    public static void serializeTags() {
        try (FileWriter writer = new FileWriter(Constants.TAGS_FILE.toFile())) {
            String data = Constants.GSON.toJson(Data.FILE_TAGS.toList());
            writer.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads the tags from {@link Constants#TAGS_FILE}
     * @return Tags instance with tags data
     */
    public static OrderedTags deserializeTags() {
        if (Files.notExists(Constants.TAGS_FILE)) {
            return new OrderedTags();
        }

        try (Reader reader = Files.newBufferedReader(Constants.TAGS_FILE)) {
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> tags = Constants.GSON.fromJson(reader, type);
            return OrderedTags.fromList(tags);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
