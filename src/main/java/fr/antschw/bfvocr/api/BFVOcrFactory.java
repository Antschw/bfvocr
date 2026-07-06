package fr.antschw.bfvocr.api;

import fr.antschw.bfvocr.exceptions.BFVOcrException;
import fr.antschw.bfvocr.dagger.OcrComponent;
import fr.antschw.bfvocr.init.NativeLibraryInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Optional;

import static fr.antschw.bfvocr.dagger.DaggerOcrComponent.create;

/**
 * Main factory for creating and using OCR services.
 * This class provides utility methods to extract server numbers
 * from Battlefield V screenshots.
 * <p>
 * The factory uses a singleton pattern to reuse a single instance of the OCR service
 * throughout the application lifecycle, avoiding repeated initialization of Tesseract
 * and its resources.
 */
public final class BFVOcrFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(BFVOcrFactory.class);
    private static volatile OcrComponent component = null;
    private static volatile BFVOcrService singletonService = null;
    private static final Object LOCK = new Object();

    private BFVOcrFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Returns a singleton instance of the OCR service.
     * The service is initialized only once and reused for later calls.
     * This avoids reloading Tesseract and its resources for each OCR operation.
     *
     * @return the singleton OCR service instance
     * @throws BFVOcrException if the service initialization fails
     */
    public static BFVOcrService getService() {
        if (singletonService == null) {
            synchronized (LOCK) {
                if (singletonService == null) {
                    NativeLibraryInitializer.initialize();
                    singletonService = getComponent().bfvOcrService();

                    // Register JVM shutdown hook to automatically close the service
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        if (singletonService != null) {
                            try {
                                singletonService.close();
                                LOGGER.debug("OCR service closed during JVM shutdown");
                            } catch (Exception e) {
                                // Log but ignore during shutdown
                                LOGGER.warn("Error while closing the OCR service during shutdown", e);
                            }
                        }
                    }));
                }
            }
        }
        return singletonService;
    }

    /**
     * Creates and returns a default OCR service instance with standard configuration.
     * This method now returns the singleton instance for better performance.
     *
     * @return a configured instance of BFVOcrService
     * @throws BFVOcrException if the service initialization fails
     */
    public static BFVOcrService createDefaultService() {
        return getService();
    }

    /**
     * Extracts a server number from a Battlefield V screenshot.
     *
     * @param imagePath the image path
     * @return the extracted server number (without the '#' symbol)
     * @throws IllegalArgumentException if the image path is null
     * @throws BFVOcrException          if the OCR process fails
     */
    public static String extractServerNumber(Path imagePath) {
        if (imagePath == null) {
            throw new IllegalArgumentException("Image path cannot be null");
        }
        return getService().extractServerNumber(imagePath);
    }

    /**
     * Extracts a server number from a Battlefield V screenshot.
     *
     * @param image the BufferedImage containing the screenshot
     * @return the extracted server number (without the '#' symbol)
     * @throws IllegalArgumentException if the image is null
     * @throws BFVOcrException          if the OCR process fails
     */
    public static String extractServerNumber(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        return getService().extractServerNumber(image);
    }

    /**
     * Attempts to extract a server number from a Battlefield V screenshot.
     *
     * @param imagePath the image path
     * @return an Optional containing the server number if found, or empty otherwise
     */
    public static Optional<String> tryExtractServerNumber(Path imagePath) {
        if (imagePath == null) {
            return Optional.empty();
        }
        try {
            String result = extractServerNumber(imagePath);
            return Optional.ofNullable(result);
        } catch (Exception e) {
            LOGGER.warn("Failed to extract server number from image: {}", imagePath, e);
            return Optional.empty();
        }
    }

    /**
     * Attempts to extract a server number from a Battlefield V screenshot.
     *
     * @param image the BufferedImage containing the screenshot
     * @return an Optional containing the server number if found, or empty otherwise
     */
    public static Optional<String> tryExtractServerNumber(BufferedImage image) {
        if (image == null) {
            return Optional.empty();
        }
        try {
            String result = extractServerNumber(image);
            return Optional.ofNullable(result);
        } catch (Exception e) {
            LOGGER.warn("Failed to extract server number from BufferedImage", e);
            return Optional.empty();
        }
    }

    private static synchronized OcrComponent getComponent() {
        if (component == null) {
            component = create();
        }
        return component;
    }

    /**
     * Explicitly releases all resources held by the OCR service.
     * Call this method when your application is shutting down or
     * when the OCR service is no longer needed.
     */
    public static void shutdown() {
        synchronized (LOCK) {
            if (singletonService != null) {
                try {
                    singletonService.close();
                    LOGGER.debug("OCR service explicitly shutdown");
                } finally {
                    singletonService = null;
                }
            }
        }
    }

    /**
     * Reset factory state - intended only for testing purposes.
     */
    static void resetForTesting() {
        synchronized (LOCK) {
            if (singletonService != null) {
                try {
                    singletonService.close();
                } catch (Exception e) {
                    LOGGER.warn("Error while closing the OCR service during test reset", e);
                } finally {
                    singletonService = null;
                }
            }
            component = null;
        }
    }
}