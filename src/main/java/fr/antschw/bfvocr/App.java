package fr.antschw.bfvocr;

import fr.antschw.bfvocr.api.BFVOcrFactory;
import fr.antschw.bfvocr.exceptions.BFVOcrException;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Main application class for testing Battlefield V OCR functionality.
 * <p>
 * This application allows the user to choose between extracting the server number
 * from an image file (by providing its path) or from a live screenshot (using a BufferedImage).
 * The extraction is performed using the BFVOcrFactory.
 * </p>
 * <p>
 * The code is organized into modular methods to adhere to SOLID principles.
 * </p>
 *
 * @author antschw
 * @version 1.1
 * @since 1.0
 */
public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    /**
     * Application entry point.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        // Log the OpenCV version
        LOGGER.info("OpenCV version: {}", Core.VERSION);

        App app = new App();
        app.run();
    }

    /**
     * Runs the application by displaying the menu and processing user input.
     */
    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            displayMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    processFileExtraction(scanner);
                    break;
                case "2":
                    processScreenshotExtraction();
                    break;
                default:
                    LOGGER.warn("Invalid choice: {}. Exiting application.", choice);
                    System.out.println("Invalid option. Please restart the application and choose 1 or 2.");
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error in application execution: {}", e.getMessage(), e);
            System.err.println("An unexpected error occurred: " + e.getMessage());
        } finally {
            // Explicitly shutdown the OCR service
            BFVOcrFactory.shutdown();
        }
    }

    /**
     * Displays the application menu to the user.
     */
    private void displayMenu() {
        System.out.println("=== Battlefield V OCR Test Application ===");
        System.out.println("Select an option:");
        System.out.println("1: Extract server number from an image file (using file path).");
        System.out.println("2: Extract server number from a screenshot (using BufferedImage).");
        System.out.print("Enter your choice (1 or 2): ");
    }

    /**
     * Processes the extraction of the server number from an image file.
     *
     * @param scanner the Scanner object for reading user input
     */
    private void processFileExtraction(Scanner scanner) {
        try {
            System.out.print("Enter the full path to the image file: ");
            String filePath = scanner.nextLine().trim();
            Path imagePath = Paths.get(filePath);

            LOGGER.info("Starting OCR extraction from file: {}", imagePath);
            String serverNumber = BFVOcrFactory.extractServerNumber(imagePath);
            LOGGER.info("OCR extraction successful. Extracted server number: {}", serverNumber);
            System.out.println("Extracted server number: " + serverNumber);
        } catch (BFVOcrException e) {
            LOGGER.error("OCR extraction error from file: {}", e.getMessage(), e);
            System.err.println("OCR extraction error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error during file extraction: {}", e.getMessage(), e);
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Processes the extraction of the server number from a screenshot.
     */
    private void processScreenshotExtraction() {
        try {
            LOGGER.info("Capturing screenshot of the entire screen...");
            BufferedImage screenshot = captureScreenshot();
            LOGGER.info("Screenshot captured successfully.");

            String serverNumber = BFVOcrFactory.extractServerNumber(screenshot);
            LOGGER.info("OCR extraction successful. Extracted server number: {}", serverNumber);
            System.out.println("Extracted server number from screenshot: " + serverNumber);
        } catch (AWTException e) {
            LOGGER.error("Error during screenshot capture: {}", e.getMessage(), e);
            System.err.println("Screenshot capture error: " + e.getMessage());
        } catch (BFVOcrException e) {
            LOGGER.error("OCR extraction error from screenshot: {}", e.getMessage(), e);
            System.err.println("OCR extraction error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error during screenshot extraction: {}", e.getMessage(), e);
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Captures a screenshot of the entire screen.
     *
     * @return a BufferedImage representing the screenshot
     * @throws AWTException if the screenshot capture fails
     */
    private BufferedImage captureScreenshot() throws AWTException {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        return robot.createScreenCapture(screenRect);
    }
}
