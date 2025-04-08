package fr.antschw.bfvocr.preprocessing;

import fr.antschw.bfvocr.util.TempDirectoryHandler;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static fr.antschw.bfvocr.constants.ImageProcessingConstants.ADAPTIVE_THRESHOLD_BLOCK_SIZE;
import static fr.antschw.bfvocr.constants.ImageProcessingConstants.ADAPTIVE_THRESHOLD_CONSTANT;
import static fr.antschw.bfvocr.constants.ImageProcessingConstants.PROCESSED_IMAGE_EXTENSION;
import static fr.antschw.bfvocr.constants.ImageProcessingConstants.ROI_HEIGHT_FACTOR;
import static fr.antschw.bfvocr.constants.ImageProcessingConstants.ROI_WIDTH_FACTOR;
import static fr.antschw.bfvocr.constants.ImageProcessingConstants.SCALE_FACTOR;

/**
 * Image preprocessor implementation using OpenCV.
 * <p>
 * This class applies various image processing techniques to optimize OCR results,
 * focusing on the regions of the image where the server number is likely to be found.
 * It uses OpenCV for image processing operations such as thresholding, scaling,
 * and region of interest extraction.
 *
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
public class OpenCvPreprocessor implements ImagePreprocessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenCvPreprocessor.class);

    /**
     * Preprocesses an image file to optimize it for OCR using OpenCV.
     * <p>
     * This implementation:
     * <ol>
     *   <li>Extracts a region of interest (ROI) from the top portion of the image</li>
     *   <li>Converts the image to grayscale</li>
     *   <li>Inverts the image for better OCR (black text on white background)</li>
     *   <li>Applies adaptive thresholding to enhance text visibility</li>
     *   <li>Scales up the image to improve OCR accuracy</li>
     * </ol>
     *
     * @param imagePath path to the source image file
     * @return a File object pointing to the preprocessed image
     * @throws IllegalArgumentException if the image path is invalid or the image cannot be loaded
     * @throws RuntimeException         if preprocessing fails
     */
    @Override
    public File preprocess(Path imagePath) {
        if (imagePath == null) {
            throw new IllegalArgumentException("Image path cannot be null");
        }
        if (!Files.exists(imagePath)) {
            throw new IllegalArgumentException("Image file does not exist: " + imagePath);
        }

        Instant start = Instant.now();
        try (Mat src = opencv_imgcodecs.imread(imagePath.toString())) {
            if (src.empty()) {
                throw new IllegalArgumentException("Failed to load image: " + imagePath);
            }

            LOGGER.debug("Processing image: {}", imagePath);

            // Extract and process ROI
            File result = processImage(src, imagePath.getFileName().toString());

            Duration processingTime = Duration.between(start, Instant.now());
            LOGGER.info("Image preprocessing completed in {} ms: {}", processingTime.toMillis(), result.getAbsolutePath());

            return result;
        } catch (Exception e) {
            LOGGER.error("Error during image preprocessing (after {} ms): {}",
                    Duration.between(start, Instant.now()).toMillis(),
                    e.getMessage());
            throw new RuntimeException("Failed to preprocess image", e);
        }
    }

    /**
     * Preprocesses an in-memory image to optimize it for OCR using OpenCV.
     * <p>
     * This implementation applies the same processing steps as for file-based images:
     * <ol>
     *   <li>Extracts a region of interest (ROI) from the top portion of the image</li>
     *   <li>Converts the image to grayscale</li>
     *   <li>Inverts the image for better OCR (black text on white background)</li>
     *   <li>Applies adaptive thresholding to enhance text visibility</li>
     *   <li>Scales up the image to improve OCR accuracy</li>
     * </ol>
     *
     * @param image the BufferedImage to process
     * @return a File object pointing to the preprocessed image
     * @throws IllegalArgumentException if the image is null
     * @throws RuntimeException         if preprocessing fails
     */
    @Override
    public File preprocess(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        Instant start = Instant.now();

        try {
            // Convert BufferedImage to OpenCV Mat
            Mat src = bufferedImageToMat(image);

            LOGGER.debug("Processing in-memory image");

            // Process image using existing method
            File result = processImage(src, "memory-image-" + UUID.randomUUID());

            Duration processingTime = Duration.between(start, Instant.now());
            LOGGER.info("In-memory image preprocessing completed in {} ms: {}",
                    processingTime.toMillis(), result.getAbsolutePath());

            return result;
        } catch (Exception e) {
            LOGGER.error("Error during in-memory image preprocessing (after {} ms): {}",
                    Duration.between(start, Instant.now()).toMillis(),
                    e.getMessage());
            throw new RuntimeException("Failed to preprocess in-memory image", e);
        }
    }

    /**
     * Converts a Java BufferedImage to an OpenCV Mat.
     *
     * @param image the BufferedImage to convert
     * @return an OpenCV Mat containing the image data
     * @throws IOException if conversion fails
     */
    private Mat bufferedImageToMat(BufferedImage image) throws IOException {
        // The simplest but not most efficient approach is to write to a byte array
        // and then read back as a Mat - this handles all image types
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        byte[] bytes = baos.toByteArray();

        // Read bytes into a Mat
        try (ByteArrayInputStream ignored = new ByteArrayInputStream(bytes)) {
            Mat mat = opencv_imgcodecs.imdecode(new org.bytedeco.opencv.opencv_core.Mat(bytes),
                    opencv_imgcodecs.IMREAD_COLOR);
            if (mat.empty()) {
                throw new IOException("Failed to convert BufferedImage to Mat");
            }
            return mat;
        }
    }

    /**
     * Processes the image by applying various OpenCV operations.
     *
     * @param src           the source image
     * @param originalName  the original image name (for temp file creation)
     * @return the processed image file
     * @throws IOException if an error occurs during file operations
     */
    private File processImage(Mat src, String originalName) throws IOException {
        // Define region of interest (ROI)
        Rect region = new Rect(0, 0,
                src.cols() / ROI_WIDTH_FACTOR,
                src.rows() / ROI_HEIGHT_FACTOR);

        try (Mat roi = new Mat(src, region)) {
            // Convert to grayscale
            opencv_imgproc.cvtColor(roi, roi, opencv_imgproc.COLOR_BGR2GRAY);
            LOGGER.debug("Converted image to grayscale");

            // Invert image (black text on white background is better for OCR)
            opencv_core.bitwise_not(roi, roi);
            LOGGER.debug("Inverted image colors");

            // Apply adaptive thresholding
            applyAdaptiveThreshold(roi);

            // Scale up image to improve OCR accuracy
            scaleImage(roi);

            // Save processed image
            return saveProcessedImage(roi, originalName);
        }
    }

    /**
     * Applies adaptive thresholding to the image.
     *
     * @param mat the image to threshold
     */
    private void applyAdaptiveThreshold(Mat mat) {
        opencv_imgproc.adaptiveThreshold(
                mat,
                mat,
                255,
                opencv_imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                opencv_imgproc.THRESH_BINARY,
                ADAPTIVE_THRESHOLD_BLOCK_SIZE,
                ADAPTIVE_THRESHOLD_CONSTANT
        );
        LOGGER.debug("Applied adaptive thresholding");
    }

    /**
     * Scales the image by the defined scale factor.
     *
     * @param mat the image to scale
     */
    private void scaleImage(Mat mat) {
        opencv_imgproc.resize(
                mat,
                mat,
                new Size(),
                SCALE_FACTOR,
                SCALE_FACTOR,
                opencv_imgproc.INTER_CUBIC
        );
        LOGGER.debug("Scaled image by factor of {}", SCALE_FACTOR);
    }

    /**
     * Saves the processed image to a temporary file.
     *
     * @param mat           the processed image
     * @param originalName  the original image name
     * @return the saved file
     * @throws IOException if an error occurs during file operations
     */
    private File saveProcessedImage(Mat mat, String originalName) throws IOException {
        Path outputPath = createTempOutputFile(originalName);
        String outputFilePath = outputPath.toString();

        opencv_imgcodecs.imwrite(outputFilePath, mat);
        LOGGER.debug("Saved processed image to: {}", outputFilePath);

        return outputPath.toFile();
    }

    /**
     * Creates a temporary file for storing the processed image.
     *
     * @param originalName the name of the original image
     * @return the path of the created temporary file
     * @throws IOException if an I/O error occurs
     */
    private Path createTempOutputFile(String originalName) throws IOException {
        String baseName = originalName;

        // Remove file extension if present
        if (baseName.contains(".")) {
            baseName = baseName.substring(0, baseName.lastIndexOf('.'));
        }

        // Use TempDirectoryHandler to create the temp file in the app's temp directory
        return TempDirectoryHandler.createTempFile(
                baseName + "-" + UUID.randomUUID().toString().substring(0, 8),
                PROCESSED_IMAGE_EXTENSION
        );
    }
}