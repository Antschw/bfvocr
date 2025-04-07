package fr.antschw.bfvocr.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fr.antschw.bfvocr.constants.OcrConstants.FLEXIBLE_SERVER_REGEX;
import static fr.antschw.bfvocr.constants.OcrConstants.SERVER_NUMBER_REGEX;

/**
 * Utility class for extracting server numbers from raw OCR result text.
 * <p>
 * This class provides methods to find, extract, and clean server numbers
 * from the text produced by the OCR engine.
 *
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
public final class Tess4JOcrUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tess4JOcrUtils.class);

    private static final Pattern SERVER_PATTERN = Pattern.compile(SERVER_NUMBER_REGEX);

    private static final Pattern FLEXIBLE_SERVER_PATTERN = Pattern.compile(FLEXIBLE_SERVER_REGEX);

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Tess4JOcrUtils() {
        // Utility class
    }

    /**
     * Extracts the Battlefield V server number from raw OCR text.
     * <p>
     * This method uses a regular expression to find a string matching the
     * expected format of a BFV server number.
     *
     * @param ocrText the OCR result text
     * @return extracted server number in the format "#XXXXX"
     * @throws IllegalArgumentException if no valid server number is found in the text
     */
    public static String extractServerNumber(String ocrText) {
        return findServerNumber(ocrText)
                .orElseThrow(() -> new IllegalArgumentException("No valid server number found"));
    }

    /**
     * Attempts to find a server number in the OCR text.
     * <p>
     * This method try first to use a regular expression to find a string matching the
     * expected format of a BFV server number and then try with a more flexible expression
     * if no matching number is found.
     *
     * @param ocrText the OCR result
     * @return an Optional containing the server number if found, empty otherwise
     */
    public static Optional<String> findServerNumber(String ocrText) {
        if (ocrText == null || ocrText.isBlank()) {
            return Optional.empty();
        }

        // First try with the standard pattern
        Matcher matcher = SERVER_PATTERN.matcher(ocrText);
        if (matcher.find()) {
            String result = matcher.group();
            LOGGER.debug("Number found with standard pattern: {}", result);
            return Optional.of(result);
        }

        // If nothing found, use flexible pattern
        matcher = FLEXIBLE_SERVER_PATTERN.matcher(ocrText);
        if (matcher.find()) {
            String result = matcher.group(1); // Group 1 contain just #XXXXX part
            LOGGER.debug("Number found with flexible pattern: {}", result);
            return Optional.of(result);
        }

        return Optional.empty();
    }

    /**
     * Removes the '#' character from a server number if present.
     * <p>
     * This method cleans the extracted server number by removing the '#' prefix,
     * returning only the numeric part. The method first checks if the '#' character
     * is present before performing the replacement for better performance.
     *
     * @param serverNumber the server number potentially containing a '#'
     * @return the server number without the '#' character
     */
    public static String removeHashSymbol(String serverNumber) {
        if (serverNumber == null || !serverNumber.contains("#")) {
            return serverNumber;
        }
        return serverNumber.replace("#", "");
    }
}