package com.digitalsigner.ui;

import com.digitalsigner.model.DocumentModel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Renders the loaded document as a scaled image in a scrollable panel.
 */
public class DocumentPanel extends JPanel {

    private BufferedImage renderedPage;
    private int           currentPage = 0;
    private DocumentModel model;

    public DocumentPanel() {
        setBackground(new Color(60, 60, 70));
        setPreferredSize(new Dimension(800, 600));
    }

    /** Load a DocumentModel and display page 0. */
    public void loadDocument(DocumentModel doc) {
        this.model = doc;
        this.currentPage = 0;
        if (doc != null && !doc.getPages().isEmpty()) {
            renderedPage = doc.getPages().get(0);
        } else {
            renderedPage = null;
        }
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (renderedPage == null) {
            drawPlaceholder(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        // Center the page with drop shadow
        int panelW = getWidth();
        int panelH = getHeight();

        double scaleX = (double)(panelW - 40) / renderedPage.getWidth();
        double scaleY = (double)(panelH - 40) / renderedPage.getHeight();
        double scale  = Math.min(scaleX, scaleY);

        int drawW = (int)(renderedPage.getWidth()  * scale);
        int drawH = (int)(renderedPage.getHeight() * scale);
        int x = (panelW - drawW) / 2;
        int y = (panelH - drawH) / 2;

        // Drop shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRect(x + 4, y + 4, drawW, drawH);

        // Page
        g2.drawImage(renderedPage, x, y, drawW, drawH, null);

        // Border
        g2.setColor(new Color(200, 200, 210));
        g2.drawRect(x, y, drawW, drawH);
    }

    private void drawPlaceholder(Graphics g) {
        g.setColor(new Color(100, 100, 115));
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        String msg = "Open a PDF or TXT file to begin";
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth()  - fm.stringWidth(msg)) / 2;
        int y = (getHeight() + fm.getAscent()) / 2;
        g.drawString(msg, x, y);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    public void nextPage() {
        if (model == null) return;
        if (currentPage < model.getTotalPages() - 1) {
            currentPage++;
            renderedPage = model.getPages().get(currentPage);
            repaint();
        }
    }

    public void prevPage() {
        if (model == null) return;
        if (currentPage > 0) {
            currentPage--;
            renderedPage = model.getPages().get(currentPage);
            repaint();
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public BufferedImage getRenderedImage() { return renderedPage; }
    public int           getCurrentPage()   { return currentPage;  }
    public DocumentModel getModel()         { return model;        }
    public boolean       hasDocument()      { return model != null && !model.getPages().isEmpty(); }
}
