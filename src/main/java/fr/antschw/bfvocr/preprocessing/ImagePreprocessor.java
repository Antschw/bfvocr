package fr.antschw.bfvocr.preprocessing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

/**
 * Interface for preprocessing images before OCR processing.
 * <p>
 * Implementations of this interface provide methods to enhance images
 * before they are passed to the OCR engine, improving text recognition results.
 *
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
public interface ImagePreprocessor {
    /**
     * Preprocesses an image file to optimize it for OCR.
     * <p>
     * This method applies various image processing techniques to increase
     * the accuracy of OCR text extraction, such as scaling, thresholding,
     * noise reduction, and contrast enhancement.
     *
     * @param imagePath path to the source image file
     * @return a File object pointing to the preprocessed image
     * @throws IllegalArgumentException if the image path is invalid
     * @throws RuntimeException         if preprocessing fails
     */
    File preprocess(Path imagePath);

    /**
     * Preprocesses an in-memory image to optimize it for OCR.
     * <p>
     * This method applies various image processing techniques to increase
     * the accuracy of OCR text extraction, such as scaling, thresholding,
     * noise reduction, and contrast enhancement.
     *
     * @param image the BufferedImage to process
     * @return a File object pointing to the preprocessed image
     * @throws IllegalArgumentException if the image is null
     * @throws RuntimeException         if preprocessing fails
     */
    File preprocess(BufferedImage image);
}