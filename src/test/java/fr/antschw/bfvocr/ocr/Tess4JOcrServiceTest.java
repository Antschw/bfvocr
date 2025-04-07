package fr.antschw.bfvocr.ocr;

import fr.antschw.bfvocr.config.OcrConfig;
import fr.antschw.bfvocr.config.OcrConfigLoader;
import fr.antschw.bfvocr.preprocessing.ImagePreprocessor;
import fr.antschw.bfvocr.preprocessing.OpenCvPreprocessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
import java.util.concurrent.atomic.AtomicReference;

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

    @TempDir
    Path tempDir; // JUnit will create and clean up this temporary directory

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
        }
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
            LOGGER.info("Successfully extracted server number '{}' from '{}' (expected: {})",
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
            LOGGER.info("Successfully extracted server number '{}' from '{}' (expected: {})",
                    result, fileName, expectedNumber);

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
    void shouldWrapTesseractExceptionForPath() {
        try {
            Path tempFile = Files.createTempFile("test-", ".png");
            Files.writeString(tempFile, "Not a valid image");

            ImagePreprocessor preprocessor = mock(ImagePreprocessor.class);
            doReturn(tempFile.toFile()).when(preprocessor).preprocess(any(Path.class));

            OcrService service = createService(preprocessor);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.extractServerNumber(Path.of("dummy.png")));

            assertNotNull(ex.getMessage(), "Exception message should not be null");

            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }

    @Test
    void shouldWrapTesseractExceptionForBufferedImage() {
        ImagePreprocessor dummy = mock(ImagePreprocessor.class);
        doReturn(new File("nonexistent.png")).when(dummy).preprocess(any(BufferedImage.class));

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
    void shouldCleanupResourcesWhenClosed() throws NoSuchFieldException, IllegalAccessException {
        // Create service instance
        Tess4JOcrService service = new Tess4JOcrService(new OpenCvPreprocessor(), config, tessdataProvider);

        // Access private tempDirRef field using reflection
        Field tempDirRefField = Tess4JOcrService.class.getDeclaredField("tempDirRef");
        tempDirRefField.setAccessible(true);
        @SuppressWarnings("unchecked")
        AtomicReference<Path> tempDirRef = (AtomicReference<Path>) tempDirRefField.get(service);

        // Save temporary directory path
        Path tempDir = tempDirRef.get();
        assertNotNull(tempDir, "Temporary directory should not be null");
        assertTrue(Files.exists(tempDir), "Temporary directory should exist");

        // Call close method
        service.close();

        // Verify that directory was deleted and reference cleared
        assertFalse(Files.exists(tempDir), "Temporary directory should be deleted after close()");
        assertNull(tempDirRef.get(), "Directory reference should be null after close()");
    }

    @Test
    void testUtilsExtractServerNumber() {
        // Valid input
        String validInput = "Found server #12345 in the game";
        assertEquals("#12345", Tess4JOcrUtils.extractServerNumber(validInput));

        // Input with no server number
        assertThrows(IllegalArgumentException.class,
                () -> Tess4JOcrUtils.extractServerNumber("No server number here"));

        // Null input
        assertThrows(IllegalArgumentException.class,
                () -> Tess4JOcrUtils.extractServerNumber(null));
    }

    @Test
    void testUtilsFindServerNumber() {
        // Valid input
        String validInput = "Found server #12345 in the game";
        Optional<String> result = Tess4JOcrUtils.findServerNumber(validInput);
        assertTrue(result.isPresent());
        assertEquals("#12345", result.get());

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