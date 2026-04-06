package com.digitalsigner.service;

import com.digitalsigner.model.SignedDocument;
import com.digitalsigner.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Handles saving the signed document artifacts directly back to the source file.
 */
public class ExportManager {

    private ExportManager() { }

    /**
     * Overwrites the original document file with the newly signed document bytes.
     * Removes the complex export dialog flow entirely per user request.
     *
     * @param signed       The cryptographically signed / stamped document
     * @param originalFile The exact file the user opened initially
     */
    public static void overwriteOriginal(SignedDocument signed, File originalFile) throws IOException {
        // 1. Overwrite original document (PDF or PNG)
        FileUtils.writeBytes(originalFile, signed.getDocumentBytes());
    }
}
