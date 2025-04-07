package fr.antschw.bfvocr.ocr;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static fr.antschw.bfvocr.constants.OcrConstants.TESSDATA_RESOURCE;

/**
 * Implementation of TessdataProvider that loads training data from the classpath.
 * <p>
 * This provider retrieves Tesseract training data files from the application's
 * classpath resources, allowing the OCR engine to function without requiring
 * external file dependencies.
 *
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
public class ClasspathTessdataProvider implements TessdataProvider {

    /**
     * Provides an InputStream for the Tesseract training data file from the classpath.
     * <p>
     * Retrieves the training data file defined in {@link fr.antschw.bfvocr.constants.OcrConstants#TESSDATA_RESOURCE}
     * from the application's classpath.
     *
     * @return an InputStream containing the Tesseract training data
     * @throws IOException           if the training data cannot be accessed
     * @throws FileNotFoundException if the training data resource is not found in the classpath
     */
    @Override
    public InputStream getTessdataStream() throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(TESSDATA_RESOURCE);
        if (stream == null) {
            throw new FileNotFoundException("Missing OCR resource: " + TESSDATA_RESOURCE);
        }
        return stream;
    }
}
