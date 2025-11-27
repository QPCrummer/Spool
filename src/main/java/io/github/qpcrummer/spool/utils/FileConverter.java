package io.github.qpcrummer.spool.utils;

import io.github.qpcrummer.spool.Constants;
import io.github.qpcrummer.spool.gui.VirtualFileList;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileConverter {
    private static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool();

    /**
     * Asynchronously generates a PNG thumbnail from an embroidery file.
     *
     * @param embroideryFileName The source file name after being moved
     */
    public static void generateThumbnailAsync(
            String embroideryFileName
    ) {

        EXECUTOR.submit(() -> {
            String pngFileName = embroideryFileName.replaceAll("\\.[^.]+$", "") + ".png";

            if (embroideryFileName.contains(".pdf")) {
                generateThumbnailPDF(Constants.FILES.resolve(embroideryFileName), Constants.FILES.resolve(pngFileName));
                SwingUtilities.invokeLater(() -> {
                    // Update thumbnail
                    VirtualFileList.updateThumbnail(embroideryFileName);
                });
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

                    // Read output
                    try (BufferedReader br =
                                 new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            System.out.println("[pystitch] " + line);
                        }
                    }

                    int exit = process.waitFor();
                    if (exit != 0) {
                        throw new IOException("Thumbnail generation failed: exit code " + exit);
                    }

                    SwingUtilities.invokeLater(() -> {
                        // Update thumbnail
                        VirtualFileList.updateThumbnail(embroideryFileName);
                    });
                } catch (Exception ex) {
                    LoggerUtils.LOGGER.warn("Failed to generate thumbnail", ex);
                }
            }
        });
    }
    public static File getThumbnail(File embroideryFile) {
        File pngFile = new File(
                embroideryFile.getParent(),
                embroideryFile.getName().replaceAll("\\.[^.]+$", "") + ".png"
        );

        // If thumbnail exists and is newer than source → use cached
        if (pngFile.exists() && pngFile.lastModified() > embroideryFile.lastModified()) {
            return pngFile;
        }

        // Not found
        return null;
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
}
