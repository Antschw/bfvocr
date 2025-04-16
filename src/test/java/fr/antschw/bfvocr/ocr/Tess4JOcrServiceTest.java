package fr.antschw.bfvocr.ocr;

import fr.antschw.bfvocr.config.OcrConfig;
import fr.antschw.bfvocr.config.OcrConfigLoader;
import fr.antschw.bfvocr.exceptions.BFVOcrException;
import fr.antschw.bfvocr.preprocessing.ImagePreprocessor;
import fr.antschw.bfvocr.preprocessing.OpenCvPreprocessor;
import fr.antschw.bfvocr.util.TempDirectoryHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test suite for {@link Tess4JOcrService} covering core functionality
 * and edge cases.
 */
class Tess4JOcrServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tess4JOcrServiceTest.class);

    private OcrConfig config;
    private TessdataProvider tessdataProvider;
    private Tess4JOcrService serviceToCleanup;

    @BeforeEach
    void init() {
        config = OcrConfigLoader.load();
        tessdataProvider = new ClasspathTessdataProvider();
        serviceToCleanup = null;
    }

    @AfterEach
    void cleanup() {
        if (serviceToCleanup != null) {
            serviceToCleanup.close();
            serviceToCleanup = null;
        }
    }

    @AfterAll
    static void cleanupAll() {
        // Ensure all temporary files created by TempDirectoryHandler are cleaned up
        TempDirectoryHandler.cleanup();
    }

    private OcrService createService(ImagePreprocessor preprocessor) {
        Tess4JOcrService service = new Tess4JOcrService(preprocessor, config, tessdataProvider);
        serviceToCleanup = service;
        return service;
    }

    private Path getResourceAsPath(String name) {
        URL resource = getClass().getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalArgumentException("Test resource not found: " + name);
        }

        try {
            return Paths.get(resource.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid resource path: " + name, e);
        }
    }

    private BufferedImage getResourceAsBufferedImage(String name) throws IOException {
        URL resource = getClass().getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalArgumentException("Test resource not found: " + name);
        }
        return ImageIO.read(resource);
    }

    @Test
    void shouldExtractServerNumberFromImage() {
        OcrService service = createService(new OpenCvPreprocessor());
        Path imagePath = getResourceAsPath("teamdeathmatch77665.png");
        String result = service.extractServerNumber(imagePath);
        assertEquals("77665", result);
    }

    @Test
    void shouldExtractServerNumberFromBufferedImage() throws IOException {
        OcrService service = createService(new OpenCvPreprocessor());
        BufferedImage image = getResourceAsBufferedImage("teamdeathmatch77665.png");
        String result = service.extractServerNumber(image);
        assertEquals("77665", result);
    }

    /**
     * Tests OCR extraction on multiple screenshots.
     * The test fails if the server number cannot be extracted
     * or if it doesn't match the number in the filename.
     *
     * @param fileName the name of the screenshot to test
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "breakthrough59257.png",
            "conquest9637.png",
            "grandoperation701.png",
            "outpost40207.png",
            "teamdeathmatch26142.png",
            "teamdeathmatch77665.png"
    })
    void shouldExtractCorrectServerNumberFromScreenshots(String fileName) {
        OcrService service = createService(new OpenCvPreprocessor());
        Path imagePath = getResourceAsPath(fileName);

        // Extract the expected server number from the filename
        String expectedNumber = extractNumberFromFilename(fileName);
        assertNotNull(expectedNumber, "Failed to extract expected number from filename: " + fileName);

        try {
            String result = service.extractServerNumber(imagePath);
            LOGGER.info("Successfully extracted server number '{}' from screenshot '{}' (expected: {})",
                    result, fileName, expectedNumber);

            assertEquals(expectedNumber, result,
                    "Extracted server number should match the number in the filename");
        } catch (Exception e) {
            LOGGER.error("Error extracting server number from '{}': {}", fileName, e.getMessage());
            fail("Failed to extract server number from " + fileName + ": " + e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "breakthrough59257.png",
            "conquest9637.png",
            "grandoperation701.png",
            "outpost40207.png",
            "teamdeathmatch26142.png",
            "teamdeathmatch77665.png"
    })
    void shouldExtractCorrectServerNumberFromBufferedImages(String fileName) throws IOException {
        OcrService service = createService(new OpenCvPreprocessor());
        BufferedImage image = getResourceAsBufferedImage(fileName);

        // Extract the expected server number from the filename
        String expectedNumber = extractNumberFromFilename(fileName);
        assertNotNull(expectedNumber, "Failed to extract expected number from filename: " + fileName);

        try {
            String result = service.extractServerNumber(image);
            LOGGER.info("Successfully extracted server number '{}' from buffered image '{}' (expected: {})",
                    result,
                    fileName,
                    expectedNumber
            );

            assertEquals(expectedNumber, result,
                    "Extracted server number should match the number in the filename");
        } catch (Exception e) {
            LOGGER.error("Error extracting server number from BufferedImage '{}': {}", fileName, e.getMessage());
            fail("Failed to extract server number from BufferedImage " + fileName + ": " + e.getMessage());
        }
    }

    @Test
    void shouldThrowWhenNoServerNumberDetectedInPath() {
        OcrService service = createService(new OpenCvPreprocessor());
        Path imagePath = getResourceAsPath("black.png");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.extractServerNumber(imagePath));
        assertTrue(ex.getMessage().contains("No valid server number"));
    }

    @Test
    void shouldThrowWhenNoServerNumberDetectedInBufferedImage() throws IOException {
        OcrService service = createService(new OpenCvPreprocessor());
        BufferedImage image = getResourceAsBufferedImage("black.png");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.extractServerNumber(image));
        assertTrue(ex.getMessage().contains("No valid server number"));
    }

    @Test
    void shouldThrowWhenProcessingImageWithFalsePositives() {
        OcrService service = createService(new OpenCvPreprocessor());
        Path imagePath = getResourceAsPath("wrongOCR.png");

        // The image contains text that might be incorrectly identified as server numbers
        // The enhanced validation should reject these false positives
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.extractServerNumber(imagePath),
                "Should throw exception when no valid server number is found in image with potential false positives");

        assertTrue(ex.getMessage().contains("No valid server number"),
                "Exception should indicate that no valid server number was found");

        LOGGER.info("Successfully rejected false positives in wrongOCR.png");
    }

    @Test
    void shouldWrapTesseractExceptionForPath() throws IOException {
        Path tempFile = TempDirectoryHandler.createTempFile("test-", ".png");
        Files.writeString(tempFile, "Not a valid image");

        ImagePreprocessor preprocessor = mock(ImagePreprocessor.class);
        doReturn(tempFile.toFile()).when(preprocessor).preprocess(any(Path.class));

        OcrService service = createService(preprocessor);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.extractServerNumber(Path.of("dummy.png")));

        assertNotNull(ex.getMessage(), "Exception message should not be null");
    }

    @Test
    void shouldWrapTesseractExceptionForBufferedImage() throws IOException {
        Path nonexistentFilePath = TempDirectoryHandler.createTempFile("nonexistent-", ".png");
        Files.deleteIfExists(nonexistentFilePath);

        File nonexistentFile = nonexistentFilePath.toFile();

        ImagePreprocessor dummy = mock(ImagePreprocessor.class);
        doReturn(nonexistentFile).when(dummy).preprocess(any(BufferedImage.class));

        OcrService service = createService(dummy);
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.extractServerNumber(image));
        assertTrue(ex.getMessage().contains("OCR processing error"));
        assertNotNull(ex.getCause());
    }

    @Test
    void shouldThrowIfTessdataIsMissing() {
        // Create a provider that throws an exception when tessdata is requested
        TessdataProvider brokenProvider = () -> {
            throw new IOException("Missing tessdata file");
        };

        UncheckedIOException ex = assertThrows(UncheckedIOException.class, () -> new Tess4JOcrService(
                new OpenCvPreprocessor(),
                config,
                brokenProvider)
        );

        assertTrue(ex.getMessage().contains("OCR configuration error"),
                "Exception should mention OCR configuration error");
    }

    @Test
    void shouldThrowIfTempDirCannotBeCreated() {
        // Create a subclass that simulates failure in temp directory creation
        class BrokenTess4JOcrService extends Tess4JOcrService {
            public BrokenTess4JOcrService() {
                super(new OpenCvPreprocessor(), config, tessdataProvider);
            }

            @Override
            protected void setupTesseract() {
                throw new UncheckedIOException("OCR configuration error",
                        new IOException("Could not create temporary directory"));
            }
        }

        UncheckedIOException ex = assertThrows(UncheckedIOException.class,
                BrokenTess4JOcrService::new);

        assertTrue(ex.getMessage().contains("OCR configuration error"),
                "Exception should mention OCR configuration error");
    }

    @Test
    void shouldCleanupResourcesWhenClosed() throws NoSuchFieldException {
        // Create service instance
        Tess4JOcrService service = new Tess4JOcrService(new OpenCvPreprocessor(), config, tessdataProvider);

        // Access private tempDirRef field using reflection
        Field tempDirRefField = Tess4JOcrService.class.getDeclaredField("tempDirRef");
        tempDirRefField.setAccessible(true);

        // Save temporary directory path
        Path tempDir = service.getTempDir();
        assertNotNull(tempDir, "Temporary directory should not be null");
        assertTrue(Files.exists(tempDir), "Temporary directory should exist");

        // Call close method
        service.close();

        assertNull(service.getTempDir(), "Directory reference should be null after close()");

        TempDirectoryHandler.cleanup();

        assertFalse(Files.exists(tempDir), "Temporary directory should be deleted after cleanup");
    }

    @Test
    void testUtilsExtractServerNumber() {
        // Input with no server number
        assertThrows(IllegalArgumentException.class,
                () -> Tess4JOcrUtils.extractServerNumber("No server number here"));

        // Null input
        assertThrows(IllegalArgumentException.class,
                () -> Tess4JOcrUtils.extractServerNumber(null));
    }

    @Test
    void testUtilsFindServerNumber() {
        // Short text with server number - should pass
        String shortTextInput = "Server #12345";
        Optional<String> shortResult = Tess4JOcrUtils.findServerNumber(shortTextInput);
        assertTrue(shortResult.isPresent());
        assertEquals("#12345", shortResult.get());

        // Input with no server number
        Optional<String> emptyResult = Tess4JOcrUtils.findServerNumber("No server number here");
        assertFalse(emptyResult.isPresent());

        // Null input
        Optional<String> nullResult = Tess4JOcrUtils.findServerNumber(null);
        assertFalse(nullResult.isPresent());
    }

    @Test
    void testUtilsRemoveHashSymbol() {
        assertEquals("12345", Tess4JOcrUtils.removeHashSymbol("#12345"));
        assertEquals("12345", Tess4JOcrUtils.removeHashSymbol("12345"));
        assertNull(Tess4JOcrUtils.removeHashSymbol(null));
    }

    /**
     * Extract the numeric part from the filename, assuming the filename follows the pattern:
     * name + number + .extension
     */
    private String extractNumberFromFilename(String fileName) {
        // Extract digits before the file extension
        String withoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
        StringBuilder numbers = new StringBuilder();

        // Extract last sequence of digits
        for (int i = withoutExtension.length() - 1; i >= 0; i--) {
            char c = withoutExtension.charAt(i);
            if (Character.isDigit(c)) {
                numbers.insert(0, c);
            } else if (!numbers.isEmpty()) {
                break; // Stop at the first non-digit after finding digits
            }
        }

        return !numbers.isEmpty() ? numbers.toString() : null;
    }
}