package fr.antschw.bfvocr.exceptions;

/**
 * Exception thrown when errors occur during OCR processing.
 * <p>
 * This exception is used to wrap various lower-level exceptions that may occur
 * during the OCR process, such as I/O errors, Tesseract errors, or OpenCV errors.
 *
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
public class BFVOcrException extends RuntimeException {

    /**
     * Constructs a new BFVOcrException with the specified detail message.
     *
     * @param message the detail message
     */
    public BFVOcrException(String message) {
        super(message);
    }

    /**
     * Constructs a new BFVOcrException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public BFVOcrException(String message, Throwable cause) {
        super(message, cause);
    }

}