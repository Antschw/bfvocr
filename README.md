---
title: Battlefield V Server Number OCR Library
author: antschw
---

# Battlefield V Server Number OCR Library

A Java library for extracting Battlefield V server numbers from screenshots using Tesseract OCR with OpenCV image preprocessing.

## Project Description

This library processes Battlefield V screenshots to extract server numbers (3–5 digits preceded by a `#` symbol) that typically appear in the top-left corner of the game interface.
It leverages advanced image processing techniques (via OpenCV) to optimize images before applying Tesseract OCR for text recognition.

### Key Features
- Accurate server number extraction via regex and post-processing
- Image preprocessing pipeline using OpenCV (grayscale, thresholding, ROI, scaling)
- Clean and modular architecture following SOLID principles
- Static API access via `BFVOcrFactory` or injectable services via Guice
- SLF4J-based logging with Logback
- Fully tested using JUnit 5 and Mockito

## Project Structure

```text
fr.antschw.bfvocr
├── api                        # Public interfaces and factories
│   ├── BFVOcrFactory.java     # Static access to core services
│   └── BFVOcrService.java     # Main service interface
├── config                     # Configuration classes
│   ├── OcrConfig.java
│   └── OcrConfigLoader.java
├── constants                  # Static constants (e.g. regex, paths)
│   ├── AppConstants.java
│   ├── ImageProcessingConstants.java
│   └── OcrConstants.java
├── exceptions                 # Domain-specific exceptions
│   └── BFVOcrException.java
├── guice                      # Dependency Injection with Guice
│   └── OcrModule.java
├── impl                       # Service implementation
│   └── DefaultBFVOcrService.java
├── init                       # Native loader for OpenCV
│   └── NativeLibraryInitializer.java
├── ocr                        # Core OCR logic
│   ├── Tess4JOcrService.java
│   ├── Tess4JOcrUtils.java
│   ├── OcrService.java
│   ├── TessdataProvider.java
│   └── ClasspathTessdataProvider.java
├── preprocessing              # Image preprocessor interfaces and impl
│   ├── ImagePreprocessor.java
│   └── OpenCvPreprocessor.java
├── util                       # Utility class for cleaning temporary files 
│   └── OpenCvPreprocessor.java
└── App.java                   # CLI test runner for screenshots or files
```

## Build & Test

```bash
git clone https://github.com/Antschw/bfvocr
cd bfvocr
mvn clean package
mvn test
```

## Usage Examples

### Static Factory Access

```java
String serverNumber = BFVOcrFactory.extractServerNumber(Path.of("screenshot.png"));
Optional<String> maybe = BFVOcrFactory.tryExtractServerNumber(bufferedImage);
```

### Using the Service Interface

```java
try (BFVOcrService service = BFVOcrFactory.createDefaultService()) {
    String num = service.extractServerNumber(Path.of("screenshot.png"));
}
```

### App Entry Point
Run `App.java` to:
- Prompt the user to input a file path
- OR capture a live screen (Robot API)
- OCR the image and print the result to console

## Test Coverage

- `Tess4JOcrServiceTest.java` – unit tests and parameterized validation
- `DefaultBFVOcrServiceTest.java` – guards and delegation validation
- `BFVOcrFactoryTest.java` – factory lifecycle and mocking

## Architecture & DI

- Guice module: `OcrModule.java` binds:
   - `BFVOcrService` → `DefaultBFVOcrService`
   - `OcrService` → `Tess4JOcrService`
   - `ImagePreprocessor` → `OpenCvPreprocessor`
   - `TessdataProvider` → `ClasspathTessdataProvider`
- Resources (e.g., `.traineddata`) are embedded via classpath

## OCR Workflow

1. **Preprocessing**
   - Crop, grayscale, invert, threshold, scale
2. **Tesseract OCR**
   - Custom whitelist `#0123456789`
   - Optimized for server ID shapes
3. **Post-processing**
   - Extract and sanitize number using regex
   - Remove hash symbol `#`

## Stack

- Java 21, Maven
- Tess4J 5.11.0 + Tesseract 5.5.0
- OpenCV 4.10.0 (JavaCPP)
- SLF4J + Logback
- Google Guice
- JUnit 5 + Mockito
