package fr.antschw.bfvocr.guice;

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

import com.google.inject.AbstractModule;

/**
 * Google Guice module for OCR-related dependencies.
 * <p>
 * This module configures the dependency injection bindings for the OCR components,
 * allowing the application to be constructed with proper dependency management.
 *
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
public class OcrModule extends AbstractModule {
    /**
     * Configures the bindings for OCR-related dependencies.
     * <p>
     * This method sets up the dependency injection mappings for:
     * <ul>
     *   <li>OCR service implementation</li>
     *   <li>Image preprocessor implementation</li>
     *   <li>OCR configuration</li>
     *   <li>Tessdata provider implementation</li>
     *   <li>BFV OCR Service implementation</li>
     * </ul>
     */
    @Override
    protected void configure() {
        bind(OcrService.class).to(Tess4JOcrService.class);
        bind(ImagePreprocessor.class).to(OpenCvPreprocessor.class);
        bind(OcrConfig.class).toInstance(OcrConfigLoader.load());
        bind(TessdataProvider.class).to(ClasspathTessdataProvider.class);
        bind(BFVOcrService.class).to(DefaultBFVOcrService.class);
    }
}
