package com.digitalsigner.ui;

import com.digitalsigner.service.KeyManager;
import com.digitalsigner.service.SignatureEngine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

/**
 * Modal dialog for verifying a signed document.
 * User loads the signed file, .sig file, and a .pub public key.
 */
public class VerifyDialog extends JDialog {

    private File      docFile;
    private File      sigFile;
    private File      pubKeyFile;
    private JLabel    lblDocFile;
    private JLabel    lblSigFile;
    private JLabel    lblPubKey;
    private JTextArea resultArea;
    private JButton   btnVerify;

    public VerifyDialog(Frame owner) {
        super(owner, "Verify Signed Document", true);
        setSize(540, 480);
        setLocationRelativeTo(owner);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 248));
        setLayout(new BorderLayout(0, 0));

        // ── Header ────────────────────────────────────────────────────────────
        JLabel title = new JLabel("Document Verification", SwingConstants.CENTER);
        title.setFont(new Font(".AppleSystemUIFont", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(16, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        // ── File pickers ──────────────────────────────────────────────────────
        lblDocFile = createStatusLabel("No document selected");
        lblSigFile = createStatusLabel("No signature selected");
        lblPubKey  = createStatusLabel("No key selected");

        JButton btnDoc = filePickerButton("📄 Select Document…", "Documents (PDF, PNG)", "pdf", "png");
        JButton btnSig = filePickerButton("🔏 Select Signature (.sig)…", "Signature Files", "sig");
        JButton btnKey = filePickerButton("🔑 Select Public Key (.pub)…", "Key Files", "pub");

        btnDoc.addActionListener(e -> docFile = pickFile(lblDocFile, "Documents", "pdf", "png"));
        btnSig.addActionListener(e -> sigFile = pickFile(lblSigFile, "Signature", "sig"));
        btnKey.addActionListener(e -> pubKeyFile = pickFile(lblPubKey, "Keys", "pub"));

        JPanel pickerPanel = new JPanel(new GridBagLayout());
        pickerPanel.setOpaque(false);
        pickerPanel.setBorder(new EmptyBorder(8, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(pickerPanel, gbc, 0, btnDoc, lblDocFile);
        addRow(pickerPanel, gbc, 1, btnSig, lblSigFile);
        addRow(pickerPanel, gbc, 2, btnKey, lblPubKey);

        // ── Result area ───────────────────────────────────────────────────────
        resultArea = new JTextArea(5, 40);
        resultArea.setEditable(false);
        resultArea.setFont(new Font(".AppleSystemUIFont", Font.BOLD, 13));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setOpaque(false);
        resultArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 225), 1));
        resultPanel.add(resultArea, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(0, 24, 8, 24));
        centerPanel.add(pickerPanel, BorderLayout.NORTH);
        centerPanel.add(resultPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // ── Buttons ───────────────────────────────────────────────────────────
        btnVerify = new JButton("Verify Document") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) g2.setColor(new Color(180, 180, 185));
                else if (getModel().isPressed()) g2.setColor(new Color(0, 90, 200));
                else g2.setColor(new Color(0, 122, 255));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnVerify.setFont(new Font(".AppleSystemUIFont", Font.BOLD, 14));
        btnVerify.setForeground(Color.WHITE);
        btnVerify.setFocusPainted(false);
        btnVerify.setContentAreaFilled(false);
        btnVerify.setBorderPainted(false);
        btnVerify.setEnabled(false);
        btnVerify.setPreferredSize(new Dimension(140, 36));
        btnVerify.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVerify.addActionListener(e -> performVerification());

        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 14));
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnBar.setOpaque(false);
        btnBar.add(btnClose);
        btnBar.add(btnVerify);

        add(btnBar, BorderLayout.SOUTH);
    }

    // ──────────────────────────────────────────────────────────────────────────

    private void performVerification() {
        if (docFile == null || sigFile == null || pubKeyFile == null) return;

        try {
            byte[] docBytes = java.nio.file.Files.readAllBytes(docFile.toPath());
            byte[] sigBytes = java.nio.file.Files.readAllBytes(sigFile.toPath());

            KeyManager km = new KeyManager();
            PublicKey pubKey = km.loadPublicKey(pubKeyFile);

            boolean valid = SignatureEngine.verify(docBytes, sigBytes, pubKey);

            if (valid) {
                resultArea.setForeground(new Color(20, 150, 60)); // Apple Green
                resultArea.setText("✅ VALID — The document has not been tampered with.\n\n"
                        + "The digital signature matches the document content exactly. "
                        + "This document is authentic and intact.");
            } else {
                resultArea.setForeground(new Color(220, 40, 40)); // Apple Red
                resultArea.setText("❌ INVALID — Signature does not match the document.\n\n"
                        + "The document may have been modified after signing, "
                        + "or the wrong public key was used.");
            }
        } catch (GeneralSecurityException ex) {
            ex.printStackTrace();
            resultArea.setForeground(new Color(220, 40, 40));
            resultArea.setText("⚠ Security error: " + ex.getMessage()
                    + "\n\nMake sure you are using the correct public key file (.pub).");
        } catch (Exception ex) {
            ex.printStackTrace();
            resultArea.setForeground(new Color(220, 40, 40));
            resultArea.setText("⚠ Error: " + ex.getMessage());
        }
    }

    private File pickFile(JLabel lbl, String desc, String... exts) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter(desc, exts));
        fc.setAcceptAllFileFilterUsed(false);
        
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            lbl.setText(f.getName());
            lbl.setForeground(new Color(40, 40, 45)); // darker when selected
            checkReady();
            return f;
        }
        return null; // Return null if nothing chosen
    }

    private void checkReady() {
        btnVerify.setEnabled(docFile != null && sigFile != null && pubKeyFile != null);
    }

    private JLabel createStatusLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(150, 150, 160));
        lbl.setFont(new Font(".AppleSystemUIFont", Font.ITALIC, 13));
        return lbl;
    }

    private JButton filePickerButton(String text, String tooltip, String... exts) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(190, 32));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, JButton btn, JLabel lbl) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        p.add(btn, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        p.add(lbl, gbc);
    }
}
