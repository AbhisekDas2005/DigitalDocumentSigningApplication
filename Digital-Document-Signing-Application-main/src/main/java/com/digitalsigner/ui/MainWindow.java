package com.digitalsigner.ui;

import com.digitalsigner.model.DocumentModel;
import com.digitalsigner.model.SignatureData;
import com.digitalsigner.model.SignedDocument;
import com.digitalsigner.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * The main application window.
 * User loads a file, signs it, and saves it directly, overwriting the original.
 */
public class MainWindow extends JFrame {

    private final DocumentPanel documentPanel;
    private final JLabel        statusLabel;
    private final JLabel        pageLabel;
    private final JButton       btnOpen;
    private final JButton       btnSign;
    private final JButton       btnSave;
    private final JButton       btnPrev;
    private final JButton       btnNext;

    private DocumentModel currentDocument;
    private SignedDocument signedDocument;
    private KeyManager    keyManager;

    public MainWindow() {
        super("Digital Document Signing Application");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 245, 248));

        initKeyManager();

        // ── Components ──────────────────────────────
        documentPanel = new DocumentPanel();
        statusLabel   = new JLabel(" Ready — Open a document to begin");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        
        pageLabel     = new JLabel(" Page: — ");
        pageLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

        btnOpen = new JButton("📂 Open Document");
        btnSign = new JButton("✍ Sign Options");
        btnSave = new JButton("💾 Save (Overwrite)");
        btnPrev = new JButton("◀ Prev");
        btnNext = new JButton("Next ▶");

        styleButton(btnOpen, true);
        styleButton(btnSign, false);
        styleButton(btnSave, false);
        styleButton(btnPrev, false);
        styleButton(btnNext, false);

        btnOpen.addActionListener(e -> openDocument());
        btnSign.addActionListener(e -> signDocument());
        btnSave.addActionListener(e -> saveDocument());
        btnPrev.addActionListener(e -> { documentPanel.prevPage(); updatePageLabel(); });
        btnNext.addActionListener(e -> { documentPanel.nextPage(); updatePageLabel(); });

        setLayout(new BorderLayout());
        add(buildToolbar(),   BorderLayout.NORTH);
        add(buildCenter(),    BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private void styleButton(JButton btn, boolean enabled) {
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setEnabled(enabled);
        btn.setMargin(new Insets(6, 12, 6, 12));
    }

    private void initKeyManager() {
        try {
            keyManager = new KeyManager();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Key Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────── Actions ─────────────────────────────────────

    private void openDocument() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Open Document");
        fc.setFileFilter(new FileNameExtensionFilter("Documents (PDF, TXT)", "pdf", "txt"));
        fc.setAcceptAllFileFilterUsed(false);

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<DocumentModel, Void> worker = new SwingWorker<>() {
            @Override protected DocumentModel doInBackground() throws Exception {
                return DocumentLoader.loadFile(file);
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    currentDocument = get();
                    signedDocument  = null;
                    documentPanel.loadDocument(currentDocument);
                    btnSign.setEnabled(true);
                    btnSave.setEnabled(false);
                    updatePageLabel();
                    updateStatus("Loaded: " + file.getName());
                    setTitle("Digital Signer — " + file.getName());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainWindow.this, "Could not load document:\n" + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void signDocument() {
        if (currentDocument == null) return;
        SignatureDialog sigDialog = new SignatureDialog(this);
        sigDialog.setVisible(true);

        SignatureData sigData = sigDialog.getSignatureData();
        if (sigData == null) return;

        showPlacementDialog(sigData);
    }

    private void showPlacementDialog(SignatureData sigData) {
        PlacementOverlayPanel overlay = new PlacementOverlayPanel(
                documentPanel.getRenderedImage(), sigData.getSignatureImage());

        JButton btnApply  = new JButton("Apply Signature");
        btnApply.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btnApply.setEnabled(false);
        overlay.setConfirmButton(btnApply);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(overlay);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.setPreferredSize(new Dimension(850, 600));

        JPanel hint = new JPanel(new FlowLayout(FlowLayout.CENTER));
        hint.add(new JLabel("👆 Click anywhere on the document to place your signature, then click Apply."));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnBar.add(btnCancel);
        btnBar.add(btnApply);

        JDialog placementDialog = new JDialog(this, "Place Signature", true);
        placementDialog.setLayout(new BorderLayout(0, 4));
        placementDialog.add(hint,       BorderLayout.NORTH);
        placementDialog.add(scrollPane, BorderLayout.CENTER);
        placementDialog.add(btnBar,     BorderLayout.SOUTH);
        placementDialog.pack();
        placementDialog.setLocationRelativeTo(this);

        btnCancel.addActionListener(e -> placementDialog.dispose());
        btnApply.addActionListener(e -> {
            placementDialog.dispose();
            applyAndSign(overlay.getPlacementPoint(), sigData);
        });

        placementDialog.setVisible(true);
    }

    private void applyAndSign(Point placementPoint, SignatureData sigData) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<SignedDocument, Void> worker = new SwingWorker<>() {
            @Override protected SignedDocument doInBackground() throws Exception {
                return SignatureProcessor.process(
                        currentDocument,
                        documentPanel.getCurrentPage(),
                        documentPanel.getRenderedImage(),
                        sigData.getSignatureImage(),
                        placementPoint,
                        keyManager.getPrivateKey());
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    signedDocument = get();
                    btnSave.setEnabled(true);
                    updateStatus("✔ Document signed ready to save");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainWindow.this, "Signing failed:\n" + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void saveDocument() {
        if (signedDocument == null) return;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                ExportManager.overwriteOriginal(signedDocument, currentDocument.getSourceFile());
                return null;
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    get();
                    updateStatus("Saved to original file: " + currentDocument.getSourceFile().getName());
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "Document successfully saved!\nOverwrite complete: " 
                            + currentDocument.getSourceFile().getAbsolutePath(),
                            "Save Complete", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainWindow.this, "Save failed:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // ──────────────────────────── UI Builders ─────────────────────────────────

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 205)));

        bar.add(btnOpen);
        bar.add(btnSign);
        bar.add(btnSave);
        bar.add(Box.createHorizontalStrut(20));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        navPanel.add(btnPrev);
        navPanel.add(pageLabel);
        navPanel.add(btnNext);
        bar.add(navPanel);

        return bar;
    }

    private JScrollPane buildCenter() {
        JScrollPane scroll = new JScrollPane(documentPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(110, 110, 120)); 
        return scroll;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(new EmptyBorder(6, 12, 6, 12));
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    private void updateStatus(String msg) {
        statusLabel.setText(" " + msg);
    }

    private void updatePageLabel() {
        if (currentDocument == null) {
            pageLabel.setText(" Page: — ");
            btnPrev.setEnabled(false);
            btnNext.setEnabled(false);
            return;
        }
        int cur   = documentPanel.getCurrentPage() + 1;
        int total = currentDocument.getTotalPages();
        pageLabel.setText(" Page " + cur + " of " + total + " ");
        btnPrev.setEnabled(cur > 1);
        btnNext.setEnabled(cur < total);
    }
}
