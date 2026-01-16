package io.github.qpcrummer.spool.utils;

import io.github.qpcrummer.spool.Constants;
import io.github.qpcrummer.spool.database.DBUtils;
import io.github.qpcrummer.spool.database.Database;
import io.github.qpcrummer.spool.file.FileRecord;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class FileConverter {

    /**
     * Processes a list of File names and generates their thumbnails concurrently
     * @param filesNames The names of the files to be processed
     */
    public static void processImageConversions(List<String> filesNames) {
        filesNames.parallelStream().forEach(FileConverter::generateThumbnail);
        // TODO Update thumbnails
    }

    /**
     * Generates a PNG thumbnail from an embroidery file.
     * @param embroideryFileName The source file path after being moved
     */
    private static void generateThumbnail(String embroideryFileName) {
        String pngFileName = embroideryFileName.replaceAll("\\.[^.]+$", "") + ".png";

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

    private static void generateThumbnailPDF(Path pdfPath, Path pngPath) {
        try (PDDocument document = Loader.loadPDF(
                new RandomAccessReadBufferedFile(pdfPath))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            // Render the first page (page index 0) at 300 DPI for good quality
            BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300);

            // Save the BufferedImage as a PNG file
            ImageIO.write(image, "PNG", pngPath.toFile());

            LoggerUtils.LOGGER.info("Converted PDF to PNG");
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
    }
}
