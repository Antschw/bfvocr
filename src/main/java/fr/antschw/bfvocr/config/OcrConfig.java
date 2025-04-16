package fr.antschw.bfvocr.config;

import java.util.Properties;

/**
 * Configuration record for Tesseract OCR engine settings.
 * <p>
 * This record encapsulates all configuration parameters needed for the
 * Tesseract OCR engine, such as the path to tessdata, language, OCR engine mode,
 * page segmentation mode, and character whitelist.
 *
 * @param dataPath      Path to the tessdata directory containing language data files
 * @param language      OCR language to use (e.g., "eng")
 * @param oem           Tesseract OCR Engine Mode (OEM)
 * @param psm           Tesseract Page Segmentation Mode (PSM)
 * @param charWhitelist Characters to whitelist (restrict recognition to these characters)
 * @author antschw
 * @version 1.0
 * @since 1.0
 */
public record OcrConfig(String dataPath, String language, int oem, int psm, String charWhitelist, int dpi) {
    /**
     * Creates a new OcrConfig instance from a Properties object.
     * <p>
     * Extracts and parses the necessary properties to create a configuration object.
     *
     * @param props Properties object containing OCR configuration
     * @throws NumberFormatException if the numeric properties cannot be parsed
     * @throws NullPointerException  if any of the required properties are missing
     */
    public OcrConfig(Properties props) {
        this(
                props.getProperty("tesseract.dataPath"),
                props.getProperty("tesseract.language"),
                Integer.parseInt(props.getProperty("tesseract.oem")),
                Integer.parseInt(props.getProperty("tesseract.psm")),
                props.getProperty("tesseract.charWhitelist"),
                Integer.parseInt(props.getProperty("tesseract.dpi"))
        );
    }
}
