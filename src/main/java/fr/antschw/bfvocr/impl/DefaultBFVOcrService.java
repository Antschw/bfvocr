package fr.antschw.bfvocr.impl;

import fr.antschw.bfvocr.api.BFVOcrService;
import fr.antschw.bfvocr.ocr.Tess4JOcrService;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Default implementation of the BFVOcrService interface.
 * <p>
 * This class extends the Tess4JOcrService to provide additional functionality
 * specific to BFV server number extraction, including more flexible input types
 * and better error handling.
 *
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
public class DefaultBFVOcrService extends Tess4JOcrService implements BFVOcrService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBFVOcrService.class);

    /**
     * Constructs a new DefaultBFVOcrService with injected dependencies.
     * <p>
     * This constructor delegates to the parent Tess4JOcrService constructor
     * to initialize OCR capabilities.
     *
     * @param preprocessor     the image preprocessor to use
     * @param config           the OCR configuration
     * @param tessdataProvider the provider for Tesseract trained data
     */
    @Inject
    public DefaultBFVOcrService(
            fr.antschw.bfvocr.preprocessing.ImagePreprocessor preprocessor,
            fr.antschw.bfvocr.config.OcrConfig config,
            fr.antschw.bfvocr.ocr.TessdataProvider tessdataProvider) {
        super(preprocessor, config, tessdataProvider);
        LOGGER.debug("DefaultBFVOcrService initialized");
    }

    @Override
    public String extractServerNumber(Path imagePath) {
        Objects.requireNonNull(imagePath, "Image path cannot be null");
        if (!Files.exists(imagePath)) {
            throw new IllegalArgumentException("Image file does not exist: " + imagePath);
        }

        return super.extractServerNumber(imagePath);
    }

    @Override
    public String extractServerNumber(BufferedImage image) {
        Objects.requireNonNull(image, "Image cannot be null");
        return super.extractServerNumber(image);
    }

    @Override
    public Optional<String> tryExtractServerNumber(Path imagePath) {
        if (imagePath == null || !Files.exists(imagePath)) {
            return Optional.empty();
        }

        try {
            return Optional.of(extractServerNumber(imagePath));
        } catch (Exception e) {
            LOGGER.debug("Failed to extract server number from {}: {}", imagePath, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> tryExtractServerNumber(BufferedImage image) {
        if (image == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(extractServerNumber(image));
        } catch (Exception e) {
            LOGGER.debug("Failed to extract server number from BufferedImage: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void shutdown() {
        this.close();
    }
}