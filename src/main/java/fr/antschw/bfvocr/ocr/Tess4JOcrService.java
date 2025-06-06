package fr.antschw.bfvocr.ocr;

import fr.antschw.bfvocr.config.OcrConfig;
import fr.antschw.bfvocr.preprocessing.ImagePreprocessor;
import fr.antschw.bfvocr.util.TempDirectoryHandler;

import com.google.inject.Inject;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static fr.antschw.bfvocr.constants.OcrConstants.FILE_EXTENSION;
import static fr.antschw.bfvocr.constants.OcrConstants.OCR_TEMP_DIR_PREFIX;
import static fr.antschw.bfvocr.constants.OcrConstants.TEMP_TESSDATA_PREFIX;

/**
 * OCR service implementation using Tess4J.
 * Handles preprocessing, Tesseract configuration, and server number extraction.
 *
 * @implNote The trained data file (eng.traineddata) is copied from the classpath (via a TessdataProvider)
 * into a temporary "tessdata" folder, and Tesseract is configured with its parent directory,
 * so that Tesseract looks for the file in "<tempDir>/tessdata/eng.traineddata".
 */
public class Tess4JOcrService implements OcrService {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tess4JOcrService.class);

    private final ImagePreprocessor preprocessor;
    private final OcrConfig config;
    private final TessdataProvider tessdataProvider;
    private final ITesseract tesseract;
    private final AtomicReference<Path> tempDirRef = new AtomicReference<>();

    /**
     * Constructs the OCR service with required components.
     *
     * @param preprocessor     the image preprocessor
     * @param config           the OCR configuration
     * @param tessdataProvider the provider for Tesseract trained data
     * @throws UncheckedIOException if OCR configuration fails
     */
    @Inject
    public Tess4JOcrService(ImagePreprocessor preprocessor,
                            OcrConfig config,
                            TessdataProvider tessdataProvider) {
        this.preprocessor = Objects.requireNonNull(preprocessor, "Preprocessor cannot be null");
        this.config = Objects.requireNonNull(config, "OCR configuration cannot be null");
        this.tessdataProvider = Objects.requireNonNull(tessdataProvider, "Tessdata provider cannot be null");
        this.tesseract = new Tesseract();
        setupTesseract();
    }

    /**
     * Extracts the Battlefield V server number from the given image file.
     *
     * @param imagePath path to the input image
     * @return extracted server number (digits only, without '#')
     * @throws IllegalArgumentException if the image path is invalid or no server number is found
     * @throws RuntimeException         if OCR processing fails
     */
    @Override
    public String extractServerNumber(Path imagePath) {
        if (imagePath == null) {
            throw new IllegalArgumentException("Image path cannot be null");
        }
        if (!java.nio.file.Files.exists(imagePath)) {
            throw new IllegalArgumentException("Image file does not exist: " + imagePath);
        }

        try {
            // Preprocess the image for better OCR results
            File processed = preprocessor.preprocess(imagePath);

            // Perform OCR on preprocessed image
            String ocrResult = tesseract.doOCR(processed).trim();
            LOGGER.info("Raw OCR result: {}", ocrResult);

            // Extract and clean the server number
            String serverNumber = Tess4JOcrUtils.extractServerNumber(ocrResult);
            String cleanNumber = Tess4JOcrUtils.removeHashSymbol(serverNumber);

            LOGGER.info("Extracted server number: {}", cleanNumber);
            return cleanNumber;

        } catch (TesseractException e) {
            LOGGER.error("OCR extraction failed", e);
            throw new RuntimeException("OCR processing error", e);
        } catch (IllegalArgumentException e) {
            // Re-throw IllegalArgumentException (e.g., no server number found)
            LOGGER.error("Server number extraction failed", e);
            throw e;
        } catch (Exception e) {
            // Catch any other exceptions
            LOGGER.error("Unexpected error during server number extraction", e);
            throw new RuntimeException("Failed to extract server number", e);
        }
    }

    /**
     * Extracts the Battlefield V server number from an in-memory image.
     *
     * @param image the BufferedImage to process
     * @return extracted server number (digits only, without '#')
     * @throws IllegalArgumentException if the image is null or no server number is found
     * @throws RuntimeException         if OCR processing fails
     */
    @Override
    public String extractServerNumber(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        try {
            // Preprocess the image for better OCR results
            File processed = preprocessor.preprocess(image);

            // Perform OCR on preprocessed image
            String ocrResult = tesseract.doOCR(processed).trim();
            LOGGER.info("Raw OCR result from in-memory image: {}", ocrResult);

            // Extract and clean the server number
            String serverNumber = Tess4JOcrUtils.extractServerNumber(ocrResult);
            String cleanNumber = Tess4JOcrUtils.removeHashSymbol(serverNumber);

            LOGGER.info("Extracted server number from in-memory image: {}", cleanNumber);
            return cleanNumber;

        } catch (TesseractException e) {
            LOGGER.error("OCR extraction failed on in-memory image", e);
            throw new RuntimeException("OCR processing error", e);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Server number extraction failed on in-memory image", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error during server number extraction from in-memory image", e);
            throw new RuntimeException("Failed to extract server number", e);
        }
    }

    /**
     * Configures Tesseract with trained data from resources, extracted into a temporary folder.
     *
     * @throws UncheckedIOException if an I/O error occurs during configuration
     * @implNote Creates a temporary directory, then a subfolder "tessdata", copies the trained data file there,
     * and sets the datapath to that subfolder.
     */
    protected void setupTesseract() {
        try {
            // Create a temporary directory for OCR data in the app temp directory
            Path tempDir = TempDirectoryHandler.createTempDirectory(OCR_TEMP_DIR_PREFIX);
            tempDirRef.set(tempDir);

            // Create tessdata subdirectory
            File tessdataFolder = new File(tempDir.toFile(), TEMP_TESSDATA_PREFIX);
            if (!tessdataFolder.mkdirs() && !tessdataFolder.exists()) {
                throw new IOException("Could not create tessdata directory: " + tessdataFolder.getAbsolutePath());
            }

            // Create trained data file
            File traineddataFile = new File(tessdataFolder, config.language() + FILE_EXTENSION);

            // Extract trained data from resources
            try (InputStream is = getTessdataStream();
                 OutputStream os = new FileOutputStream(traineddataFile)) {

                if (is == null) {
                    throw new IOException("Tessdata stream could not be obtained");
                }

                LOGGER.debug("Extracting tessdata file to temporary directory: {}", traineddataFile.getAbsolutePath());
                is.transferTo(os);
            }

            // Configure Tesseract instance
            tesseract.setDatapath(tessdataFolder.getAbsolutePath());
            tesseract.setLanguage(config.language());
            tesseract.setOcrEngineMode(config.oem());
            tesseract.setPageSegMode(config.psm());
            tesseract.setVariable("tessedit_char_whitelist", config.charWhitelist());

            // Set default DPI value to prevent the "Invalid resolution 1 dpi" warning
            tesseract.setVariable("user_defined_dpi", String.valueOf(config.dpi()));

            LOGGER.debug("Tesseract configured successfully with datapath: {}", tessdataFolder.getAbsolutePath());

        } catch (IOException e) {
            LOGGER.error("Failed to set up Tesseract", e);
            throw new UncheckedIOException("OCR configuration error", e);
        }
    }

    /**
     * Retrieves the trained data stream using the injected TessdataProvider.
     * Can be overridden in tests.
     *
     * @return InputStream of the trained data file
     * @throws IOException if an error occurs while obtaining the stream
     */
    protected InputStream getTessdataStream() throws IOException {
        return tessdataProvider.getTessdataStream();
    }

    /**
     * Returns the current temporary directory path used by this service.
     *
     * @return the path to the temporary directory, or null if not initialized or already closed
     */
    protected Path getTempDir() {
        return tempDirRef.get();
    }

    /**
     * Closes resources when the service is no longer needed.
     * Instead of manually cleaning up temp files, this now relies on
     * the centralized TempDirectoryHandler.
     */
    public void close() {
        // Reset reference to temp directory
        tempDirRef.set(null);

        // Temp files are managed by TempDirectoryHandler and will be cleaned up
        // when the application shuts down or when explicitly calling TempDirectoryHandler.cleanup()
    }
}