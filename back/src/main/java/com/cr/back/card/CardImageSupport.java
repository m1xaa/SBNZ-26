package com.cr.back.card;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;

public final class CardImageSupport {
    private CardImageSupport() {
    }

    public static String loadBase64Image(Integer imageAssetId) {
        if (imageAssetId == null) {
            return null;
        }
        ClassPathResource image = new ClassPathResource("images/" + imageAssetId + ".png");
        if (!image.exists()) {
            return null;
        }
        try {
            byte[] bytes = image.getInputStream().readAllBytes();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to load card image: " + imageAssetId, exception);
        }
    }
}
