package fr.antschw.bfvocr.api;

import fr.antschw.bfvocr.guice.OcrModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bytedeco.javacpp.Loader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BFVOcrFactoryTest {

    @Mock
    private BFVOcrService mockService;

    @Mock
    private Injector mockInjector;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().when(mockInjector.getInstance(BFVOcrService.class)).thenReturn(mockService);
        resetFactoryStatics();
    }

    protected void resetFactoryStatics() throws Exception {
        // Reset via public method first
        BFVOcrFactory.resetForTesting();

        // Backup reset via reflection
        Field injectorField = BFVOcrFactory.class.getDeclaredField("injector");
        injectorField.setAccessible(true);
        injectorField.set(null, null);

        Field serviceField = BFVOcrFactory.class.getDeclaredField("singletonService");
        serviceField.setAccessible(true);
        serviceField.set(null, null);

        // Reset native libraries
        fr.antschw.bfvocr.init.NativeLibraryInitializer.reset();
    }

    @Test
    void getService_ShouldReturnServiceFromInjector() {
        try (MockedStatic<Guice> mockedGuice = mockStatic(Guice.class);
             MockedStatic<Loader> mockedLoader = mockStatic(Loader.class)) {
            mockedGuice.when(() -> Guice.createInjector(any(OcrModule.class))).thenReturn(mockInjector);

            BFVOcrService service = BFVOcrFactory.getService();

            assertNotNull(service);
            assertSame(mockService, service);
            mockedLoader.verify(() -> Loader.load(any(Class.class)));
        }
    }

    @Test
    void getService_ShouldReturnSameInstanceOnMultipleCalls() {
        try (MockedStatic<Guice> mockedGuice = mockStatic(Guice.class);
             MockedStatic<Loader> mockedLoader = mockStatic(Loader.class)) {
            mockedGuice.when(() -> Guice.createInjector(any(OcrModule.class))).thenReturn(mockInjector);

            BFVOcrService service1 = BFVOcrFactory.getService();
            BFVOcrService service2 = BFVOcrFactory.getService();

            assertSame(service1, service2);
            mockedGuice.verify(() -> Guice.createInjector(any(OcrModule.class)), times(1));
            mockedLoader.verify(() -> Loader.load(any(Class.class)), times(1));
        }
    }

    @Test
    void extractServerNumber_Path_ShouldUseServiceInstance() {
        try (MockedStatic<BFVOcrFactory> mockedFactory = mockStatic(BFVOcrFactory.class)) {
            mockedFactory.when(BFVOcrFactory::getService).thenReturn(mockService);
            mockedFactory.when(() -> BFVOcrFactory.extractServerNumber(any(Path.class))).thenCallRealMethod();
            lenient().when(mockService.extractServerNumber(any(Path.class))).thenReturn("12345");

            String result = BFVOcrFactory.extractServerNumber(Path.of("test.png"));

            assertEquals("12345", result);
            verify(mockService).extractServerNumber(any(Path.class));
        }
    }

    @Test
    void extractServerNumber_Path_ShouldThrowWhenPathIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> BFVOcrFactory.extractServerNumber((Path) null));
    }

    @Test
    void extractServerNumber_BufferedImage_ShouldUseServiceInstance() {
        try (MockedStatic<BFVOcrFactory> mockedFactory = mockStatic(BFVOcrFactory.class)) {
            mockedFactory.when(BFVOcrFactory::getService).thenReturn(mockService);
            mockedFactory.when(() -> BFVOcrFactory.extractServerNumber(any(BufferedImage.class))).thenCallRealMethod();
            lenient().when(mockService.extractServerNumber(any(BufferedImage.class))).thenReturn("12345");

            BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            String result = BFVOcrFactory.extractServerNumber(image);

            assertEquals("12345", result);
            verify(mockService).extractServerNumber(any(BufferedImage.class));
        }
    }

    @Test
    void tryExtractServerNumber_Path_ShouldReturnOptionalWhenSuccess() {
        try (MockedStatic<BFVOcrFactory> mockedFactory = mockStatic(BFVOcrFactory.class)) {
            mockedFactory.when(() -> BFVOcrFactory.extractServerNumber(any(Path.class))).thenReturn("12345");
            mockedFactory.when(() -> BFVOcrFactory.tryExtractServerNumber(any(Path.class))).thenCallRealMethod();

            Optional<String> result = BFVOcrFactory.tryExtractServerNumber(Path.of("test.png"));

            assertTrue(result.isPresent());
            assertEquals("12345", result.get());
        }
    }

    @Test
    void shutdown_ShouldCloseAndClearService() {
        BFVOcrService spyService = mock(BFVOcrService.class);

        try {
            Field serviceField = BFVOcrFactory.class.getDeclaredField("singletonService");
            serviceField.setAccessible(true);
            serviceField.set(null, spyService);

            BFVOcrFactory.shutdown();

            verify(spyService).close();

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage(), e);
        } finally {
            try {
                Field serviceField = BFVOcrFactory.class.getDeclaredField("singletonService");
                serviceField.setAccessible(true);
                serviceField.set(null, null);
            } catch (Exception e) {
                // Ignored
            }
        }
    }
}