package com.digitalsigner.ui;

import com.digitalsigner.model.SignatureData;

/**
 * Interface for any panel that provides a signature image.
 * Implemented by DrawSignaturePanel and UploadSignaturePanel.
 */
public interface SignatureSource {
    /**
     * Returns the signature as a SignatureData object containing the image and metadata.
     */
    SignatureData getSignature();
}
