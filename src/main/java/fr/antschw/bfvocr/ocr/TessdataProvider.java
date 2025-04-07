package fr.antschw.bfvocr.ocr;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for providing Tesseract training data files.
 * <p>
 * Implementations of this interface are responsible for providing access to
 * Tesseract's language data files (*.traineddata) that are required for OCR.
 *
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
public interface TessdataProvider {
    /**
     * Provides an InputStream for the Tesseract training data file.
     * <p>
     * This method should return a stream to the appropriate *.traineddata file
     * based on the configured language.
     *
     * @return an InputStream containing the Tesseract training data
     * @throws IOException if the training data cannot be accessed
     */
    InputStream getTessdataStream() throws IOException;
}
