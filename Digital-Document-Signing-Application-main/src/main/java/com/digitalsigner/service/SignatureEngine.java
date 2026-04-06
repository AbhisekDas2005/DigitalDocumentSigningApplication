package com.digitalsigner.service;

import java.security.*;

/**
 * Core cryptographic operations: hashing, signing, and verifying.
 * All methods are static — no state held.
 */
public class SignatureEngine {

    private SignatureEngine() { }

    /**
     * Returns SHA-256 hash of the provided bytes.
     */
    public static byte[] hashDocument(byte[] documentBytes) throws GeneralSecurityException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(documentBytes);
    }

    /**
     * Signs the document bytes using SHA256withRSA and the given private key.
     *
     * @return RSA digital signature bytes
     */
    public static byte[] sign(byte[] documentBytes, PrivateKey privateKey)
            throws GeneralSecurityException {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(documentBytes);
        return sig.sign();
    }

    /**
     * Verifies an RSA digital signature against document bytes and a public key.
     *
     * @return true if the signature is valid
     */
    public static boolean verify(byte[] documentBytes, byte[] signatureBytes,
                                 PublicKey publicKey) throws GeneralSecurityException {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(documentBytes);
        return sig.verify(signatureBytes);
    }
}
