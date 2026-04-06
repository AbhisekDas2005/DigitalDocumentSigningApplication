package com.digitalsigner.model;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

/**
 * Holds the signature image and metadata about how it was created.
 */
public class SignatureData {

    private BufferedImage signatureImage;
    private String method;          // "DRAW" or "UPLOAD"
    private LocalDateTime timestamp;

    public SignatureData(BufferedImage signatureImage, String method) {
        this.signatureImage = signatureImage;
        this.method = method;
        this.timestamp = LocalDateTime.now();
    }

    public BufferedImage getSignatureImage() { return signatureImage; }
    public void setSignatureImage(BufferedImage signatureImage) { this.signatureImage = signatureImage; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
