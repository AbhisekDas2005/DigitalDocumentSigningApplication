package com.digitalsigner.service;

import com.digitalsigner.model.DocumentModel;
import com.digitalsigner.model.SignatureData;
import com.digitalsigner.model.SignedDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

/**
 * Orchestrates the full signing workflow:
 * composites the signature image onto the document (PDF or PNG fallback),
 * then cryptographically signs it.
 */
public class SignatureProcessor {

    private SignatureProcessor() { }

    /**
     * Stamps the signatureData onto the specific page of the DocumentModel.
     * If the document is a PDF, it natively injects the image into the PDF bytes.
     * If TXT, it falls back to compositing onto a PNG.
     *
     * @param doc          The entire document model (with raw bytes)
     * @param pageIndex    Which page was being viewed/signed
     * @param renderedPage The screen-rendered page image (150 DPI)
     * @param sigImage     The signature image (scaled as shown on screen)
     * @param placement    Screen coordinates where the user clicked
     * @param privateKey   RSA private key used to sign
     * @return             Fully populated SignedDocument
     */
    public static SignedDocument process(DocumentModel doc,
                                         int pageIndex,
                                         BufferedImage renderedPage,
                                         BufferedImage sigImage,
                                         Point placement,
                                         PrivateKey privateKey)
            throws IOException, GeneralSecurityException {

        byte[] finalBytes;

        if ("PDF".equalsIgnoreCase(doc.getFileType())) {
            // Native PDF Stamping
            try (PDDocument pdf = Loader.loadPDF(doc.getRawBytes())) {
                PDPage page = pdf.getPage(pageIndex);
                PDRectangle cropBox = page.getCropBox();

                // Convert 150 DPI screen coordinates to 72 DPI PDF points
                float scale = 72f / 150f;
                float pdfX = placement.x * scale;
                float pdfSigHeight = sigImage.getHeight() * scale;
                float pdfY = cropBox.getHeight() - (placement.y * scale) - pdfSigHeight;

                PDImageXObject pdImage = LosslessFactory.createFromImage(pdf, sigImage);

                try (PDPageContentStream contentStream = new PDPageContentStream(
                        pdf, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    contentStream.drawImage(pdImage, pdfX, pdfY, sigImage.getWidth() * scale, pdfSigHeight);
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                pdf.save(baos);
                finalBytes = baos.toByteArray();
            }
        } else {
            // PNG Fallback for TXT forms
            BufferedImage composed = compositeImages(renderedPage, sigImage, placement);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(composed, "PNG", baos);
            finalBytes = baos.toByteArray();
        }

        // Sign the bytes
        byte[] signatureBytes = SignatureEngine.sign(finalBytes, privateKey);

        // Build result
        SignedDocument signedDoc = new SignedDocument();
        signedDoc.setDocumentBytes(finalBytes);
        signedDoc.setDigitalSignatureBytes(signatureBytes);
        signedDoc.setOriginalFileName(doc.getSourceFile().getName());
        signedDoc.setSignerInfo(System.getProperty("user.name"));

        return signedDoc;
    }

    /**
     * Fallback: Composites the signature image onto a copy of the document image.
     */
    public static BufferedImage compositeImages(BufferedImage base,
                                                BufferedImage signature,
                                                Point placement) {
        BufferedImage result = new BufferedImage(base.getWidth(), base.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.drawImage(base, 0, 0, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2.drawImage(signature, placement.x, placement.y, null);
        g2.dispose();

        return result;
    }
}
