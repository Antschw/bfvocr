package fr.antschw.bfvocr.constants;

/**
 * Constants used for image preprocessing operations.
 * These values are optimized for extracting server numbers from Battlefield V screenshots.
 *
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
public final class ImageProcessingConstants {

    /**
     * Scale factor for image resizing.
     * A value of 2.0 means the image will be doubled in size.
     */
    public static final double SCALE_FACTOR = 2.0;
    /**
     * Block size for adaptive thresholding.
     * Must be an odd number. Larger values produce more stable thresholding.
     */
    public static final int ADAPTIVE_THRESHOLD_BLOCK_SIZE = 11;
    /**
     * Constant subtracted from the mean for adaptive thresholding.
     * Lower values produce more aggressive thresholding.
     */
    public static final int ADAPTIVE_THRESHOLD_CONSTANT = 2;
    /**
     * Factor to divide image height by when extracting region of interest.
     * A value of 3 means the top third of the image will be processed.
     */
    public static final int ROI_HEIGHT_FACTOR = 3;
    /**
     * Factor to divide image width by when extracting region of interest.
     * A value of 2 means the left half of the image will be processed.
     */
    public static final int ROI_WIDTH_FACTOR = 2;
    /**
     * Default file extension for processed images.
     */
    public static final String PROCESSED_IMAGE_EXTENSION = "_processed.png";

    private ImageProcessingConstants() {
        // Utility class, no instantiation
    }
} 