package fr.antschw.bfvocr.dagger;

import dagger.Component;
import fr.antschw.bfvocr.api.BFVOcrService;

import javax.inject.Singleton;

/**
 * Dagger component that acts as the entry point for retrieving dependency-injected OCR services.
 * <p>
 * This component coordinates the creation of dependency graphs defined by the {@link OcrModule}
 * and guarantees singleton scope across the component instances.
 *
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
@Singleton
@Component(modules = OcrModule.class)
public interface OcrComponent {
  /**
   * Retrieves the fully configured and dependency-injected BFVOcrService instance.
   *
   * @return the BFVOcrService singleton instance
   */
  BFVOcrService bfvOcrService();
}
