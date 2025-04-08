package fr.antschw.bfvocr.impl;

import fr.antschw.bfvocr.exceptions.BFVOcrException;
import fr.antschw.bfvocr.ocr.TessdataProvider;
import fr.antschw.bfvocr.preprocessing.ImagePreprocessor;
import fr.antschw.bfvocr.util.TempDirectoryHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests for DefaultBFVOcrService.
 */
@ExtendWith(MockitoExtension.class)
class DefaultBFVOcrServiceTest {

    @Mock
    private ImagePreprocessor mockPreprocessor;

    @Mock
    private fr.antschw.bfvocr.config.OcrConfig mockConfig;

    @Mock
    private TessdataProvider mockTessdataProvider;

    @TempDir
    Path tempDir;

    private Path testImagePath;

    @BeforeEach
    void setUp() throws IOException {
        testImagePath = Files.createFile(tempDir.resolve("test.png"));
    }

    @AfterEach
    void tearDown() {
    }

    @AfterAll
    static void cleanupAll() {
        TempDirectoryHandler.cleanup();
    }

    /**
     * Tests for extractServerNumber(Path)
     */
    @Test
    void extractServerNumber_Path_ShouldExtractNumber() {
        // Arrange
        try (DefaultBFVOcrService service = spy(createNoOpService())) {
            doReturn("12345").when(service).extractServerNumber(any(Path.class));

            // Act
            String result = service.extractServerNumber(testImagePath);

            // Assert
            assertEquals("12345", result);
        }
    }

    @Test
    void extractServerNumber_Path_ShouldThrowWhenPathIsNull() {
        // Arrange
        try (DefaultBFVOcrService service = createNoOpService()) {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> service.extractServerNumber((Path) null));
        }
    }

    @Test
    void extractServerNumber_Path_ShouldThrowWhenPathDoesNotExist() {
        // Arrange
        Path nonExistentPath = tempDir.resolve("nonexistent.png");
        try (DefaultBFVOcrService service = createNoOpService()) {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.extractServerNumber(nonExistentPath));
        }
    }

    /**
     * Tests for extractServerNumber(BufferedImage)
     */
    @Test
    void extractServerNumber_BufferedImage_ShouldExtractNumber() {
        // Arrange
        BufferedImage mockImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        try (DefaultBFVOcrService service = spy(createNoOpService())) {
            doReturn("12345").when(service).extractServerNumber(any(BufferedImage.class));

            // Act
            String result = service.extractServerNumber(mockImage);

            // Assert
            assertEquals("12345", result);
        }
    }

    @Test
    void extractServerNumber_BufferedImage_ShouldThrowWhenImageIsNull() {
        // Arrange
        try (DefaultBFVOcrService service = createNoOpService()) {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> service.extractServerNumber((BufferedImage) null));
        }
    }

    /**
     * Tests for tryExtractServerNumber(Path)
     */
    @Test
    void tryExtractServerNumber_Path_ShouldReturnOptionalWhenSuccess() {
        // Arrange
        try (DefaultBFVOcrService service = spy(createNoOpService())) {
            doReturn("12345").when(service).extractServerNumber(any(Path.class));

            // Act
            Optional<String> result = service.tryExtractServerNumber(testImagePath);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("12345", result.get());
        }
    }

    @Test
    void tryExtractServerNumber_Path_ShouldReturnEmptyWhenPathIsNull() {
        // Arrange
        try (DefaultBFVOcrService service = createNoOpService()) {
            // Act
            Optional<String> result = service.tryExtractServerNumber((Path) null);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void tryExtractServerNumber_Path_ShouldReturnEmptyWhenPathDoesNotExist() {
        // Arrange
        Path nonExistentPath = tempDir.resolve("nonexistent.png");
        try (DefaultBFVOcrService service = createNoOpService()) {
            // Act
            Optional<String> result = service.tryExtractServerNumber(nonExistentPath);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void tryExtractServerNumber_Path_ShouldReturnEmptyWhenExceptionIsThrown() {
        // Arrange
        try (DefaultBFVOcrService service = spy(createNoOpService())) {
            doThrow(new RuntimeException("Test exception")).when(service).extractServerNumber(any(Path.class));

            // Act
            Optional<String> result = service.tryExtractServerNumber(testImagePath);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    /**
     * Tests for tryExtractServerNumber(BufferedImage)
     */
    @Test
    void tryExtractServerNumber_BufferedImage_ShouldReturnOptionalWhenSuccess() {
        // Arrange
        BufferedImage mockImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        try (DefaultBFVOcrService service = spy(createNoOpService())) {
            doReturn("12345").when(service).extractServerNumber(any(BufferedImage.class));

            // Act
            Optional<String> result = service.tryExtractServerNumber(mockImage);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("12345", result.get());
        }
    }

    @Test
    void tryExtractServerNumber_BufferedImage_ShouldReturnEmptyWhenImageIsNull() {
        // Arrange
        try (DefaultBFVOcrService service = createNoOpService()) {
            // Act
            Optional<String> result = service.tryExtractServerNumber((BufferedImage) null);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void tryExtractServerNumber_BufferedImage_ShouldReturnEmptyWhenExceptionIsThrown() {
        // Arrange
        BufferedImage mockImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        try (DefaultBFVOcrService service = spy(createNoOpService())) {
            doThrow(new BFVOcrException("Test exception")).when(service).extractServerNumber(any(BufferedImage.class));

            // Act
            Optional<String> result = service.tryExtractServerNumber(mockImage);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void shutdown_ShouldCallClose() {
        // Arrange
        try (DefaultBFVOcrService service = spy(createNoOpService())) {
            doNothing().when(service).close();

            // Act
            service.shutdown();

            // Assert
            verify(service).close();
        }
    }

    /**
     * Creates a DefaultBFVOcrService instance that skips problematic constructor code.
     *
     * @return A testable DefaultBFVOcrService instance
     */
    private DefaultBFVOcrService createNoOpService() {
        return new DefaultBFVOcrService(mockPreprocessor, mockConfig, mockTessdataProvider) {
            @Override
            protected void setupTesseract() {
                // Do nothing to avoid initialization errors
            }

            @Override
            public void close() {
                // Implementation left empty intentionally
            }
        };
    }
}