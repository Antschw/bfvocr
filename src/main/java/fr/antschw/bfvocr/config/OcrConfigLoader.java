package fr.antschw.bfvocr.config;

import fr.antschw.bfvocr.constants.OcrConstants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Loads OCR configuration properties from the specified configuration file.
 * <p>
 * This utility class is responsible for loading the OCR configuration from
 * the properties file defined in {@link fr.antschw.bfvocr.constants.OcrConstants#CONFIG_PATH}.
 *
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
public class OcrConfigLoader {
    /**
     * Loads the OCR configuration from the properties file.
     * <p>
     * This method reads the properties file from the classpath and creates
     * an {@link OcrConfig} object with the loaded properties.
     *
     * @return an {@link OcrConfig} object containing all configuration parameters
     * @throws RuntimeException if the configuration file cannot be found or loaded
     */
    public static OcrConfig load() {
        Properties props = new Properties();
        try (InputStream is = OcrConfigLoader.class.getClassLoader().getResourceAsStream(OcrConstants.CONFIG_PATH)) {
            if (is == null) {
                throw new FileNotFoundException("Configuration file not found: " + OcrConstants.CONFIG_PATH);
            }
            props.load(new InputStreamReader(is));
            return new OcrConfig(props);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
}
