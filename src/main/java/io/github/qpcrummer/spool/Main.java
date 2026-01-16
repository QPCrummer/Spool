package io.github.qpcrummer.spool;

import io.github.qpcrummer.spool.database.Database;
import io.github.qpcrummer.spool.gui.MainGUI;

import java.io.IOException;
import java.nio.file.Files;

public class Main {

    /**
     * Launches the Spool program
     * @param args There are no args
     */
    public static void main(String[] args) {
        // Set up DB
        Database.init();

        // Create files directory
        createFilesDirectory();

        // Main window setup
        MainGUI.init(args);
    }

    private static void createFilesDirectory() {
        if (!Files.isDirectory(Constants.FILES)) {
            try {
                Files.createDirectory(Constants.FILES);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}