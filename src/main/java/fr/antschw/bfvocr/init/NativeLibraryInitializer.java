package fr.antschw.bfvocr.init;

import fr.antschw.bfvocr.exceptions.BFVOcrException;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for initializing native libraries required for OCR.
 */
public class NativeLibraryInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeLibraryInitializer.class);
    private static final Object INIT_LOCK = new Object();
    private static volatile boolean initialized = false;

    private NativeLibraryInitializer() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Initializes the native libraries required for OCR processing.
     * This method loads the OpenCV libraries using JavaCPP's Loader.
     * It ensures that the libraries are loaded only once even if called multiple times.
     *
     * @throws BFVOcrException if initialization fails
     */
    public static void initialize() {
        if (!initialized) {
            synchronized (INIT_LOCK) {
                if (!initialized) {
                    try {
                        LOGGER.debug("Initializing native libraries...");

                        // Load OpenCV with JavaCPP
                        Loader.load(opencv_java.class);

                        initialized = true;
                        LOGGER.info("Native libraries initialized successfully");
                    } catch (Exception e) {
                        LOGGER.error("Failed to initialize native libraries", e);
                        throw new BFVOcrException("Failed to initialize native libraries", e);
                    }
                }
            }
        }
    }

    /**
     * Resets the initialization state (mainly used for testing).
     */
    public static void reset() {
        synchronized (INIT_LOCK) {
            initialized = false;
        }
    }
}
