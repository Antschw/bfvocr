package fr.antschw.bfvocr.ocr;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

/**
 * OCR Service Interface for extracting Battlefield V server numbers from screenshots.
 * <p>
 * This interface defines the core functionality for extracting BFV server numbers
 * from screenshots by applying optical character recognition (OCR) techniques.
 *
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
public interface OcrService {
    /**
     * Extracts the Battlefield V server number from the given image file.
     * <p>
     * This method applies image preprocessing and OCR to identify and extract
     * the server number in the format "#XXXXX" from a Battlefield V screenshot.
     * The return value will be the numeric part only (without the '#' symbol).
     *
     * @param imagePath path to the input image file
     * @return extracted server number (digits only, without '#')
     * @throws IllegalArgumentException if the image path is invalid or no server number is found
     * @throws RuntimeException         if OCR processing fails
     */
    String extractServerNumber(Path imagePath);

    /**
     * Extracts the Battlefield V server number from an in-memory image.
     * <p>
     * This method applies image preprocessing and OCR to identify and extract
     * the server number in the format "#XXXXX" from a Battlefield V screenshot.
     * The return value will be the numeric part only (without the '#' symbol).
     *
     * @param image the BufferedImage to process
     * @return extracted server number (digits only, without '#')
     * @throws IllegalArgumentException if the image is null or no server number is found
     * @throws RuntimeException         if OCR processing fails
     */
    String extractServerNumber(BufferedImage image);
}