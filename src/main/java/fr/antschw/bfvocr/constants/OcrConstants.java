package fr.antschw.bfvocr.constants;

/**
 * Constants related to OCR processing.
 * <p>
 * This class holds reusable constants for the OCR module, including configuration paths,
 * resource locations, temporary file naming conventions, and pattern definitions.
 *
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
public final class OcrConstants {

    /**
     * Path to the OCR configuration properties file.
     */
    public static final String CONFIG_PATH = "ocr-config.properties";

    /**
     * Resource path to the Tesseract English training data file.
     */
    public static final String TESSDATA_RESOURCE = "tessdata/eng.traineddata";

    /**
     * Prefix for temporary tessdata directory.
     */
    public static final String TEMP_TESSDATA_PREFIX = "tessdata";

    /**
     * File extension for Tesseract training data files.
     */
    public static final String FILE_EXTENSION = ".traineddata";

    /**
     * Prefix for temporary OCR directory.
     */
    public static final String OCR_TEMP_DIR_PREFIX = "ocr-tessdata-";

    /**
     * Regex pattern to match a Battlefield V server number.
     * <p>
     * It expects a '#' character followed by 3-5 digits, with space or line boundary before.
     */
    public static final String SERVER_NUMBER_REGEX = "(?<=^|\\s)#\\d{3,5}(?=\\s|$)";

    /**
     * Flexible regex pattern to match a Battlefield V server number with various surroundings.
     * <p>
     * Used as a fallback when the strict pattern doesn't match.
     * Group 1 captures just the server number.
     */
    public static final String FLEXIBLE_SERVER_REGEX = ".*?(#\\d{3,5})(?:\\s|$|[^\\d])";

    /**
     * Regex for validating the format of an extracted server number.
     */
    public static final String VALID_SERVER_NUMBER_FORMAT = "^#\\d{3,5}$";

    /**
     * Maximum number of numeric sequences in valid BFV screenshot OCR result
     */
    public static final int MAX_NUMERIC_SEQUENCES = 20;

    /**
     * Maximum length of OCR text for a valid BFV server screen
     */
    public static final int MAX_OCR_TEXT_LENGTH = 500;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private OcrConstants() {
        // Utility class
    }
}