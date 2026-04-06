package com.digitalsigner.ui;

import com.digitalsigner.model.SignatureData;
import com.digitalsigner.util.ImageUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;

/**
 * Panel to upload a scanned/photo handwriting signature.
 * Automatically removes white background for a seamless effect.
 */
public class UploadSignaturePanel extends JPanel implements SignatureSource {

    private BufferedImage processedSignature;
    private final JLabel  previewLabel;
    private final JButton btnChoose;

    public UploadSignaturePanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(new Color(245, 245, 248)); // Mac light theme bg
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel info = new JLabel("<html><center>Upload a photo of your handwritten signature.<br/>"
                + "The white background will be removed automatically.</center></html>", SwingConstants.CENTER);
        info.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 14));
        info.setForeground(new Color(60, 60, 60));

        previewLabel = new JLabel("No Image Selected", SwingConstants.CENTER);
        previewLabel.setFont(new Font(".AppleSystemUIFont", Font.ITALIC, 14));
        previewLabel.setForeground(new Color(150, 150, 150));
        previewLabel.setBackground(Color.WHITE);
        previewLabel.setOpaque(true);
        previewLabel.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 215), 1));
        previewLabel.setPreferredSize(new Dimension(300, 140));

        btnChoose = new JButton("Select Image...") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(new Color(200, 200, 205));
                else g2.setColor(new Color(225, 225, 230)); // light gray standard mac button
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnChoose.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 14));
        btnChoose.setFocusPainted(false);
        btnChoose.setContentAreaFilled(false);
        btnChoose.setBorderPainted(false);
        btnChoose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnChoose.addActionListener(e -> chooseImage());

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; gbc.insets = new Insets(10, 0, 10, 0);
        centerPanel.add(info, gbc);
        gbc.gridy = 1;
        centerPanel.add(btnChoose, gbc);
        gbc.gridy = 2;
        centerPanel.add(previewLabel, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void chooseImage() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Images (PNG, JPG)", "png", "jpg", "jpeg"));
        fc.setAcceptAllFileFilterUsed(false);

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                BufferedImage raw = ImageIO.read(f);
                if (raw != null) {
                    // Pre-process: make ARGB, remove white, crop to signature drawing
                    BufferedImage argb    = ImageUtils.toARGB(raw);
                    BufferedImage noBg    = ImageUtils.removeWhiteBackground(argb);
                    processedSignature    = ImageUtils.trimWhitespace(noBg);

                    // Show scaled preview
                    BufferedImage preview = ImageUtils.scaleToFit(processedSignature, 280, 120);
                    previewLabel.setText(null);
                    previewLabel.setIcon(new ImageIcon(preview));
                } else {
                    JOptionPane.showMessageDialog(this, "The selected file is not a valid image.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error reading image:\n" + ex.getMessage());
            }
        }
    }

    @Override
    public SignatureData getSignature() {
        if (processedSignature == null) return null;
        return new SignatureData(processedSignature, "UPLOAD");
    }
}
