package com.digitalsigner.service;

import com.digitalsigner.model.DocumentModel;
import com.digitalsigner.util.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads a PDF or TXT file into a DocumentModel.
 * Uses a strategy-based design: delegates to PdfDocumentStrategy or TxtDocumentStrategy.
 */
public class DocumentLoader {

    private DocumentLoader() { }

    public static DocumentModel loadFile(File file) throws IOException {
        String ext = FileUtils.getExtension(file);
        switch (ext) {
            case "pdf": return new PdfDocumentStrategy().load(file);
            case "txt": return new TxtDocumentStrategy().load(file);
            default:
                throw new IOException("Unsupported file type: " + ext
                        + ". Please open a PDF or TXT file.");
        }
    }

    // ── Strategy: PDF ──────────────────────────────────────────────────────────

    private static class PdfDocumentStrategy {
        DocumentModel load(File file) throws IOException {
            DocumentModel model = new DocumentModel();
            model.setSourceFile(file);
            model.setFileType("PDF");
            model.setRawBytes(FileUtils.readBytes(file));

            List<BufferedImage> pages = new ArrayList<>();
            try (PDDocument pdf = Loader.loadPDF(file)) {
                PDFRenderer renderer = new PDFRenderer(pdf);
                for (int i = 0; i < pdf.getNumberOfPages(); i++) {
                    BufferedImage img = renderer.renderImageWithDPI(i, 150, ImageType.RGB);
                    pages.add(img);
                }
                model.setTotalPages(pdf.getNumberOfPages());
            }
            model.setPages(pages);
            return model;
        }
    }

    // ── Strategy: TXT ──────────────────────────────────────────────────────────

    private static class TxtDocumentStrategy {
        private static final int PAGE_WIDTH  = 850;
        private static final int PAGE_HEIGHT = 1100;
        private static final int MARGIN      = 40;
        private static final int LINE_HEIGHT = 18;
        private static final int LINES_PER_PAGE = 50;
        private static final Font TEXT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 13);

        DocumentModel load(File file) throws IOException {
            DocumentModel model = new DocumentModel();
            model.setSourceFile(file);
            model.setFileType("TXT");
            model.setRawBytes(FileUtils.readBytes(file));

            String content = new String(model.getRawBytes());
            String[] allLines = content.split("\n", -1);

            // Wrap long lines
            List<String> wrapped = new ArrayList<>();
            for (String line : allLines) {
                wrapLine(line, wrapped);
            }

            int totalPagesCount = Math.max(1,
                    (int) Math.ceil((double) wrapped.size() / LINES_PER_PAGE));
            model.setTotalPages(totalPagesCount);

            List<BufferedImage> pages = new ArrayList<>();
            for (int p = 0; p < totalPagesCount; p++) {
                int startLine = p * LINES_PER_PAGE;
                int endLine   = Math.min(startLine + LINES_PER_PAGE, wrapped.size());
                pages.add(renderPage(wrapped.subList(startLine, endLine), p + 1, totalPagesCount));
            }
            model.setPages(pages);
            return model;
        }

        private void wrapLine(String line, List<String> out) {
            int maxChars = 90;
            if (line.length() <= maxChars) {
                out.add(line);
                return;
            }
            while (line.length() > maxChars) {
                int split = line.lastIndexOf(' ', maxChars);
                if (split < 0) split = maxChars;
                out.add(line.substring(0, split));
                line = line.substring(split).stripLeading();
            }
            if (!line.isEmpty()) out.add(line);
        }

        private BufferedImage renderPage(List<String> lines, int page, int total) {
            BufferedImage img = new BufferedImage(PAGE_WIDTH, PAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Background
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, PAGE_WIDTH, PAGE_HEIGHT);

            // Header bar
            g2.setColor(new Color(240, 240, 245));
            g2.fillRect(0, 0, PAGE_WIDTH, 36);
            g2.setColor(new Color(180, 180, 195));
            g2.drawLine(0, 36, PAGE_WIDTH, 36);

            // Text
            g2.setFont(TEXT_FONT);
            g2.setColor(new Color(30, 30, 40));
            int y = MARGIN + 50;
            for (String line : lines) {
                g2.drawString(line, MARGIN, y);
                y += LINE_HEIGHT;
            }

            // Footer
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            g2.setColor(Color.GRAY);
            String footer = "Page " + page + " of " + total;
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(footer, PAGE_WIDTH - fm.stringWidth(footer) - MARGIN, PAGE_HEIGHT - 20);

            g2.dispose();
            return img;
        }
    }
}
