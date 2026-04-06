package com.digitalsigner.util;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Static utility methods for image processing operations.
 */
public class ImageUtils {

    private ImageUtils() { }

    /**
     * Removes near-white pixels by setting them transparent (ARGB).
     */
    public static BufferedImage removeWhiteBackground(BufferedImage input) {
        BufferedImage result = toARGB(input);
        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                int argb = result.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                if (r > 230 && g > 230 && b > 230) {
                    result.setRGB(x, y, 0x00FFFFFF); // fully transparent
                }
            }
        }
        return result;
    }

    /**
     * Trims the white border from around the actual signature strokes.
     */
    public static BufferedImage trimWhitespace(BufferedImage input) {
        int minX = input.getWidth(), maxX = 0;
        int minY = input.getHeight(), maxY = 0;

        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                int argb = input.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                boolean isWhiteOrTransparent = (r > 240 && g > 240 && b > 240)
                        || ((argb >> 24) & 0xFF) == 0;
                if (!isWhiteOrTransparent) {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX <= minX || maxY <= minY) {
            return input; // nothing to trim
        }

        int pad = 10;
        minX = Math.max(0, minX - pad);
        minY = Math.max(0, minY - pad);
        maxX = Math.min(input.getWidth() - 1, maxX + pad);
        maxY = Math.min(input.getHeight() - 1, maxY + pad);

        return input.getSubimage(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Scales an image to fit within maxW x maxH, preserving aspect ratio.
     */
    public static BufferedImage scaleToFit(BufferedImage input, int maxW, int maxH) {
        double scaleX = (double) maxW / input.getWidth();
        double scaleY = (double) maxH / input.getHeight();
        double scale = Math.min(scaleX, scaleY);

        int newW = (int) (input.getWidth() * scale);
        int newH = (int) (input.getHeight() * scale);

        BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(input, 0, 0, newW, newH, null);
        g2.dispose();
        return scaled;
    }

    /**
     * Converts any BufferedImage to ARGB type.
     */
    public static BufferedImage toARGB(BufferedImage input) {
        if (input.getType() == BufferedImage.TYPE_INT_ARGB) {
            return input;
        }
        BufferedImage result = new BufferedImage(
                input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(input, 0, 0, null);
        g2.dispose();
        return result;
    }
}
