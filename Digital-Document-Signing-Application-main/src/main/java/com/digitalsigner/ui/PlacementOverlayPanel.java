package com.digitalsigner.ui;

import com.digitalsigner.util.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

/**
 * An interactive panel displaying the document.
 * Follows the mouse with a "ghost" of the signature.
 * When clicked, drops the signature there and enables the confirm button.
 */
public class PlacementOverlayPanel extends JPanel {

    private final BufferedImage documentImage;
    private final BufferedImage signatureImage;

    private Point hoverPoint    = null;
    private Point dropPoint     = null;
    private JButton btnConfirm  = null;

    public PlacementOverlayPanel(BufferedImage docImage, BufferedImage rawSignature) {
        this.documentImage = docImage;
        // Scale signature to a reasonable size on screen (e.g., 200x80)
        this.signatureImage = ImageUtils.scaleToFit(rawSignature, 200, 80);

        Dimension size = new Dimension(docImage.getWidth(), docImage.getHeight());
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        // Track mouse to draw ghost
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverPoint = e.getPoint();
                repaint();
            }
        });

        // Track clicks to drop the signature
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoverPoint = null;
                repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                dropPoint = e.getPoint();
                if (btnConfirm != null) {
                    btnConfirm.setEnabled(true);
                }
                repaint();
            }
        });
    }

    public void setConfirmButton(JButton btn) {
        this.btnConfirm = btn;
    }

    /**
     * @return The top-left corner where the user dropped the signature.
     */
    public Point getPlacementPoint() {
        return dropPoint;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Draw shadow backing for document (MacOS glass style)
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillRect(4, 4, documentImage.getWidth(), documentImage.getHeight());

        // Draw the document
        g2.drawImage(documentImage, 0, 0, null);

        // Draw the dropped signature (opaque)
        if (dropPoint != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.drawImage(signatureImage, dropPoint.x, dropPoint.y, null);
            
            // Draw a subtle blue border to show it's selected
            g2.setColor(new Color(0, 122, 255));
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5f}, 0.0f));
            g2.drawRect(dropPoint.x, dropPoint.y, signatureImage.getWidth(), signatureImage.getHeight());
        }

        // Draw the hover ghost (translucent 50%)
        if (hoverPoint != null && (dropPoint == null || !dropPoint.equals(hoverPoint))) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2.drawImage(signatureImage, hoverPoint.x, hoverPoint.y, null);
        }
    }
}
