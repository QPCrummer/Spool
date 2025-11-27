package io.github.qpcrummer.spool.utils;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class USBUtils {

    /**
     * Attempts to find a removable USB drive on Windows or Linux.
     * Returns the root directory of the drive, or null if none found.
     */
    public static File detectUSBDrive() {

        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return detectUSBWindows();
        } else {
            return detectUSBLinux();
        }
    }

    // --------------------- WINDOWS -----------------------
    private static File detectUSBWindows() {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File[] roots = File.listRoots();

        for (File root : roots) {
            String desc = fsv.getSystemTypeDescription(root);
            if (desc == null) continue;

            desc = desc.toLowerCase();

            if (desc.contains("removable")) {
                return root; // This is likely the USB drive
            }
        }

        return null;
    }

    // --------------------- LINUX -------------------------
    private static File detectUSBLinux() {
        List<File> possibleRoots = new ArrayList<>();

        String user = System.getProperty("user.name");

        possibleRoots.add(new File("/media/" + user));
        possibleRoots.add(new File("/run/media/" + user));

        for (File root : possibleRoots) {
            if (root.exists() && root.isDirectory()) {
                File[] mounts = root.listFiles();
                if (mounts == null) continue;

                for (File mount : mounts) {
                    if (mount.isDirectory() && mount.canWrite()) {
                        return mount;
                    }
                }
            }
        }

        return null;
    }

    public static boolean copyFileToUSB(Path sourceFile) {
        File usb = detectUSBDrive();

        if (usb == null) {
            LoggerUtils.LOGGER.info("No USB Device Found");
            return false;
        }

        Path dest = usb.toPath().resolve(sourceFile.getFileName());
        try {
            Files.copy(sourceFile, dest, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException _) {
            return false;
        }
    }
}

