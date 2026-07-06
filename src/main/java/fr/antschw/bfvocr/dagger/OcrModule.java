package fr.antschw.bfvocr.dagger;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import fr.antschw.bfvocr.api.BFVOcrService;
import fr.antschw.bfvocr.config.OcrConfig;
import fr.antschw.bfvocr.config.OcrConfigLoader;
import fr.antschw.bfvocr.impl.DefaultBFVOcrService;
import fr.antschw.bfvocr.ocr.ClasspathTessdataProvider;
import fr.antschw.bfvocr.ocr.OcrService;
import fr.antschw.bfvocr.ocr.Tess4JOcrService;
import fr.antschw.bfvocr.ocr.TessdataProvider;
import fr.antschw.bfvocr.preprocessing.ImagePreprocessor;
import fr.antschw.bfvocr.preprocessing.OpenCvPreprocessor;

import javax.inject.Singleton;

/**
 * Dagger module that defines dependency injection bindings for the OCR components.
 * <p>
 * This module configures the dependency injection mappings for:
 * <ul>
 *   <li>OCR service implementation (using {@code Tess4JOcrService})</li>
 *   <li>Image preprocessor implementation (using {@code OpenCvPreprocessor})</li>
 *   <li>OCR configuration loaded from properties</li>
 *   <li>Tessdata provider implementation (using {@code ClasspathTessdataProvider})</li>
 *   <li>BFV OCR Service implementation (using {@code DefaultBFVOcrService})</li>
 * </ul>
 * <p>
 * It uses {@link Binds} for interface-to-implementation mapping where constructor injection is used,
 * and {@link Provides} for classes requiring manual instantiation or custom loading.
 *
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
@Module
public abstract class OcrModule {

    /**
     * Binds the OCR service interface to its Tess4J implementation.
     *
     * @param impl the concrete Tess4JOcrService implementation
     * @return the bound OcrService instance
     */
    @Binds
    @Singleton
    abstract OcrService bindOcrService(Tess4JOcrService impl);

    /**
     * Binds the BFV OCR service interface to its default implementation.
     *
     * @param impl the concrete DefaultBFVOcrService implementation
     * @return the bound BFVOcrService instance
     */
    @Binds
    @Singleton
    abstract BFVOcrService bindBFVOcrService(DefaultBFVOcrService impl);

    /**
     * Provides the OpenCV-based image preprocessor.
     *
     * @return the ImagePreprocessor instance
     */
    @Provides
    @Singleton
    static ImagePreprocessor provideImagePreprocessor() {
        return new OpenCvPreprocessor();
    }

    /**
     * Provides the classpath-based Tessdata provider.
     *
     * @return the TessdataProvider instance
     */
    @Provides
    @Singleton
    static TessdataProvider provideTessdataProvider() {
        return new ClasspathTessdataProvider();
    }

    /**
     * Provides the application OCR configuration loaded from configuration files.
     *
     * @return the OcrConfig instance
     */
    @Provides
    @Singleton
    static OcrConfig provideOcrConfig() {
        return OcrConfigLoader.load();
    }
}
