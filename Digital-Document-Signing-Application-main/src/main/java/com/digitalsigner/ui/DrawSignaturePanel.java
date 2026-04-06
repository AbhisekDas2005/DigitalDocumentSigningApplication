package com.digitalsigner.ui;

import com.digitalsigner.model.SignatureData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

/**
 * Panel to capture a user's handwritten signature using the mouse.
 */
public class DrawSignaturePanel extends JPanel implements SignatureSource {

    private final Path2D path;
    private Point lastPoint;

    public DrawSignaturePanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(new Color(245, 245, 248)); // MacOS background
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        path = new Path2D.Float();

        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                
                g2.setColor(new Color(210, 210, 215));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));

                g2.setColor(new Color(30, 30, 30));
                g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(path);
                
                g2.setColor(new Color(200, 200, 200));
                g2.setFont(new Font(".AppleSystemUIFont", Font.ITALIC, 14));
                g2.drawString("Draw your signature here", 20, getHeight() - 20);
            }
        };
        canvas.setBackground(new Color(245, 245, 248));
        canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
                path.moveTo(lastPoint.x, lastPoint.y);
                canvas.repaint();
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    Point p = e.getPoint();
                    path.lineTo(p.x, p.y);
                    lastPoint = p;
                    canvas.repaint();
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                lastPoint = null;
            }
        };

        canvas.addMouseListener(ma);
        canvas.addMouseMotionListener(ma);

        JButton btnClear = new JButton("🗑 Clear Canvas");
        btnClear.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 13));
        btnClear.setForeground(new Color(200, 40, 40));
        btnClear.setFocusPainted(false);
        btnClear.addActionListener(e -> {
            path.reset();
            canvas.repaint();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        bottom.add(btnClear);

        add(canvas, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    @Override
    public SignatureData getSignature() {
        if (path.getCurrentPoint() == null) {
            return null; // Canvas is empty
        }

        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw transparent background
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setComposite(AlphaComposite.Src);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(path);
        g2.dispose();

        return new SignatureData(img, "DRAW");
    }
}
