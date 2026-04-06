package com.digitalsigner.model;

import java.time.LocalDateTime;

/**
 * Encapsulates an exported signed document package.
 */
public class SignedDocument {

    private byte[] documentBytes;
    private byte[] digitalSignatureBytes;
    private String signerInfo;
    private LocalDateTime signedAt;
    private String hashAlgorithm = "SHA-256";
    private String signatureAlgorithm = "RSA";
    private String originalFileName;

    public SignedDocument() {
        this.signedAt = LocalDateTime.now();
    }

    public byte[] getDocumentBytes() { return documentBytes; }
    public void setDocumentBytes(byte[] documentBytes) { this.documentBytes = documentBytes; }

    public byte[] getDigitalSignatureBytes() { return digitalSignatureBytes; }
    public void setDigitalSignatureBytes(byte[] digitalSignatureBytes) { this.digitalSignatureBytes = digitalSignatureBytes; }

    public String getSignerInfo() { return signerInfo; }
    public void setSignerInfo(String signerInfo) { this.signerInfo = signerInfo; }

    public LocalDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }

    public String getHashAlgorithm() { return hashAlgorithm; }
    public void setHashAlgorithm(String hashAlgorithm) { this.hashAlgorithm = hashAlgorithm; }

    public String getSignatureAlgorithm() { return signatureAlgorithm; }
    public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = signatureAlgorithm; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
}
