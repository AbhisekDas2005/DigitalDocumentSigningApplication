package com.digitalsigner.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Manages RSA key pair generation, saving, and loading.
 * On first launch, auto-generates keys at ~/.digitalsigner/
 */
public class KeyManager {

    private static final String KEY_DIR = System.getProperty("user.home") + "/.digitalsigner";
    private static final String PRIVATE_KEY_FILE = KEY_DIR + "/private.key";
    private static final String PUBLIC_KEY_FILE  = KEY_DIR + "/public.pub";

    private PrivateKey privateKey;
    private PublicKey  publicKey;

    /**
     * Initialises the KeyManager — loads existing keys or generates a new pair.
     */
    public KeyManager() throws GeneralSecurityException, IOException {
        File privFile = new File(PRIVATE_KEY_FILE);
        File pubFile  = new File(PUBLIC_KEY_FILE);

        if (privFile.exists() && pubFile.exists()) {
            privateKey = loadPrivateKey(privFile);
            publicKey  = loadPublicKey(pubFile);
        } else {
            KeyPair pair = generateKeyPair();
            privateKey = pair.getPrivate();
            publicKey  = pair.getPublic();
            File dir = new File(KEY_DIR);
            dir.mkdirs();
            savePrivateKey(privateKey, privFile);
            savePublicKey(publicKey, pubFile);
        }
    }

    public PrivateKey getPrivateKey() { return privateKey; }
    public PublicKey  getPublicKey()  { return publicKey;  }

    public String getPublicKeyPath()  { return PUBLIC_KEY_FILE; }
    public String getPrivateKeyPath() { return PRIVATE_KEY_FILE; }

    // ──────────────────────────────────────────────────────────────────────────

    public KeyPair generateKeyPair() throws GeneralSecurityException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        return gen.generateKeyPair();
    }

    public void savePrivateKey(PrivateKey key, File file) throws IOException {
        Files.write(file.toPath(), key.getEncoded());
    }

    public void savePublicKey(PublicKey key, File file) throws IOException {
        Files.write(file.toPath(), key.getEncoded());
    }

    public PrivateKey loadPrivateKey(File file) throws GeneralSecurityException, IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    public PublicKey loadPublicKey(File file) throws GeneralSecurityException, IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
