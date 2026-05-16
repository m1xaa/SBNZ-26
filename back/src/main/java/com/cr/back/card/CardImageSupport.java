package com.cr.back.card;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public final class CardImageSupport {
    private static final Path SOURCE_IMAGE_DIR = Path.of("src", "main", "resources", "images");
    private static final Path TARGET_IMAGE_DIR = Path.of("target", "classes", "images");

    private CardImageSupport() {
    }

    public static String loadBase64Image(Integer imageAssetId) {
        if (imageAssetId == null) {
            return null;
        }

        byte[] bytes = loadImageBytes(imageAssetId);
        if (bytes == null) {
            return null;
        }
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
    }

    public static void storeBase64Image(Integer imageAssetId, String imageBase64) {
        if (imageAssetId == null || imageBase64 == null || imageBase64.isBlank()) {
            return;
        }

        String payload = imageBase64.contains(",")
                ? imageBase64.substring(imageBase64.indexOf(',') + 1)
                : imageBase64;
        byte[] bytes = Base64.getDecoder().decode(payload);

        writeImage(SOURCE_IMAGE_DIR.resolve(imageAssetId + ".png"), bytes);
        if (Files.exists(TARGET_IMAGE_DIR)) {
            writeImage(TARGET_IMAGE_DIR.resolve(imageAssetId + ".png"), bytes);
        }
    }

    private static byte[] loadImageBytes(Integer imageAssetId) {
        ClassPathResource classpathImage = new ClassPathResource("images/" + imageAssetId + ".png");
        try {
            if (classpathImage.exists()) {
                return classpathImage.getInputStream().readAllBytes();
            }

            Path sourceImage = SOURCE_IMAGE_DIR.resolve(imageAssetId + ".png");
            if (Files.exists(sourceImage)) {
                return Files.readAllBytes(sourceImage);
            }

            Path targetImage = TARGET_IMAGE_DIR.resolve(imageAssetId + ".png");
            if (Files.exists(targetImage)) {
                return Files.readAllBytes(targetImage);
            }

            return null;
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to load card image: " + imageAssetId, exception);
        }
    }

    private static void writeImage(Path imagePath, byte[] bytes) {
        try {
            Files.createDirectories(imagePath.getParent());
            Files.write(imagePath, bytes);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to store card image: " + imagePath.getFileName(), exception);
        }
    }
}
