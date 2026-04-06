package com.digitalsigner.ui;

import com.digitalsigner.model.SignatureData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modal dialog for inputting a signature.
 * Offers two tabs: Draw (using mouse) or Upload (select image file).
 */
public class SignatureDialog extends JDialog {

    private final DrawSignaturePanel drawPanel;
    private final UploadSignaturePanel uploadPanel;

    private SignatureData resultData = null;

    public SignatureDialog(Frame owner) {
        super(owner, "Create Your Signature", true);
        setSize(580, 480);
        setResizable(false);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(new Color(245, 245, 248));
        setLayout(new BorderLayout(0, 0));

        // ── Tabs Header (MacOS unified style, no tab borders) ─────────────────
        JLabel title = new JLabel("Signature Options", SwingConstants.CENTER);
        title.setFont(new Font(".AppleSystemUIFont", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(16, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        // ── Inner Panels ──────────────────────────────────────────────────────
        drawPanel   = new DrawSignaturePanel();
        uploadPanel = new UploadSignaturePanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 14));
        tabbedPane.addTab("🖌 Draw Signature", drawPanel);
        tabbedPane.addTab("🖼 Upload Image", uploadPanel);
        tabbedPane.setBorder(new EmptyBorder(0, 16, 0, 16));
        add(tabbedPane, BorderLayout.CENTER);

        // ── Bottom Button Bar (Mac Style) ─────────────────────────────────────
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 14));
        btnCancel.setFocusPainted(false);
        
        JButton btnSave = new JButton("Save & Continue") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 122, 255));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSave.setFont(new Font(".AppleSystemUIFont", Font.BOLD, 14));
        btnSave.setForeground(Color.WHITE);
        btnSave.setContentAreaFilled(false);
        btnSave.setBorderPainted(false);
        btnSave.setPreferredSize(new Dimension(150, 36));
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnBar.setOpaque(false);
        btnBar.add(btnCancel);
        btnBar.add(btnSave);

        add(btnBar, BorderLayout.SOUTH);

        // Actions
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> {
            Component selected = tabbedPane.getSelectedComponent();
            if (selected instanceof SignatureSource) {
                resultData = ((SignatureSource) selected).getSignature();
                if (resultData != null) {
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No signature provided. Please draw or select an image.",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }

    public SignatureData getSignatureData() {
        return resultData;
    }
}
