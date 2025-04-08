package fr.antschw.bfvocr.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Handles temporary directory and file operations for the BFV OCR library.
 * <p>
 * This utility class manages a dedicated temporary directory for the application,
 * providing methods to create temporary files and directories, as well as cleaning up
 * resources when they are no longer needed.
 *
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
public class TempDirectoryHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TempDirectoryHandler.class);
    private static final String APP_TEMP_DIR_NAME = "bfvocr-temp";
    private static volatile Path appTempDir = null;
    private static final Object LOCK = new Object();

    /**
     * Gets the application's dedicated temporary directory.
     * Creates it if it doesn't exist.
     *
     * @return the application's temporary directory path
     */
    public static Path getAppTempDir() {
        if (appTempDir == null) {
            synchronized (LOCK) {
                if (appTempDir == null) {
                    try {
                        // Get the system temp directory
                        Path systemTempDir = Path.of(System.getProperty("java.io.tmpdir"));
                        appTempDir = systemTempDir.resolve(APP_TEMP_DIR_NAME);

                        // Create the application temp directory if it doesn't exist
                        if (!Files.exists(appTempDir)) {
                            Files.createDirectory(appTempDir);
                            LOGGER.info("Created application temp directory: {}", appTempDir);
                        }

                        // Register a JVM shutdown hook to clean up when the application exits
                        Runtime.getRuntime().addShutdownHook(new Thread(TempDirectoryHandler::cleanup));
                    } catch (IOException e) {
                        LOGGER.error("Failed to create application temp directory", e);
                        // Fall back to system temp directory if creation fails
                        appTempDir = Path.of(System.getProperty("java.io.tmpdir"));
                    }
                }
            }
        }
        return appTempDir;
    }

    /**
     * Creates a temporary file in the application's dedicated temporary directory.
     *
     * @param prefix the prefix string to be used in generating the file name
     * @param suffix the suffix string to be used in generating the file name
     * @return the path to the newly created temporary file
     * @throws IOException if an I/O error occurs
     */
    public static Path createTempFile(String prefix, String suffix) throws IOException {
        return Files.createTempFile(getAppTempDir(), prefix, suffix);
    }

    /**
     * Creates a temporary directory in the application's dedicated temporary directory.
     *
     * @param prefix the prefix string to be used in generating the directory name
     * @return the path to the newly created temporary directory
     * @throws IOException if an I/O error occurs
     */
    public static Path createTempDirectory(String prefix) throws IOException {
        return Files.createTempDirectory(getAppTempDir(), prefix);
    }

    /**
     * Cleans up the application's temporary directory by deleting all its contents.
     * The directory itself is preserved for future use.
     */
    public static void cleanup() {
        if (appTempDir == null || !Files.exists(appTempDir)) {
            return;
        }

        LOGGER.info("Cleaning up application temp directory: {}", appTempDir);

        try (Stream<Path> paths = Files.walk(appTempDir)) {
            // Sort in reverse order to delete files before directories
            paths.sorted(Comparator.reverseOrder())
                    .filter(path -> !path.equals(appTempDir)) // Don't delete the parent directory
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            LOGGER.debug("Deleted: {}", path);
                        } catch (IOException e) {
                            LOGGER.warn("Failed to delete: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            LOGGER.error("Error cleaning up application temp directory", e);
        }
    }
}