package fr.antschw.bfvocr.api;

import fr.antschw.bfvocr.exceptions.BFVOcrException;
import fr.antschw.bfvocr.ocr.OcrService;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Main interface for BFV OCR operations providing methods to extract
 * server numbers from Battlefield V screenshots.
 * <p>
 * This interface extends the basic OcrService with additional functionalities
 * and more flexible parameter types.
 *
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
public interface BFVOcrService extends OcrService, AutoCloseable {

    /**
     * Extracts a server number from a Battlefield V screenshot.
     * <p>
     * This method overrides the base method from OcrService to provide
     * better documentation and error handling.
     *
     * @param imagePath path to the input image file
     * @return extracted server number (digits only, without '#')
     * @throws IllegalArgumentException if the image path is invalid or no server number is found
     * @throws BFVOcrException          if OCR processing fails
     */
    @Override
    String extractServerNumber(Path imagePath);

    /**
     * Extracts a server number from a Battlefield V screenshot loaded as a BufferedImage.
     *
     * @param image BufferedImage containing the screenshot
     * @return extracted server number (digits only, without '#')
     * @throws IllegalArgumentException if the image is null or no server number is found
     * @throws BFVOcrException          if OCR processing fails
     */
    @Override
    String extractServerNumber(BufferedImage image);

    /**
     * Attempts to extract a server number from an image, returning an Optional result.
     * <p>
     * This method doesn't throw exceptions for missing server numbers; it returns
     * an empty Optional instead.
     *
     * @param imagePath path to the input image file
     * @return an Optional containing the server number if found, empty otherwise
     * @throws BFVOcrException if OCR processing fails
     */
    Optional<String> tryExtractServerNumber(Path imagePath);

    /**
     * Attempts to extract a server number from a BufferedImage, returning an Optional result.
     * <p>
     * This method doesn't throw exceptions for missing server numbers; it returns
     * an empty Optional instead.
     *
     * @param image BufferedImage containing the screenshot
     * @return an Optional containing the server number if found, empty otherwise
     * @throws BFVOcrException if OCR processing fails
     */
    Optional<String> tryExtractServerNumber(BufferedImage image);

    /**
     * Shuts down the service and releases all resources.
     * <p>
     * This method should be called when the service is no longer needed to ensure
     * proper cleanup of Tesseract and OpenCV resources.
     */
    void shutdown();

    /**
     * Implements AutoCloseable to support try-with-resources.
     * <p>
     * This method simply calls shutdown().
     */
    @Override
    default void close() {
        shutdown();
    }
}