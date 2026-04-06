package com.digitalsigner.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a loaded document with its rendered pages.
 */
public class DocumentModel {

    private File sourceFile;
    private String fileType;   // "PDF" or "TXT"
    private byte[] rawBytes;
    private List<BufferedImage> pages;
    private int totalPages;

    public DocumentModel() {
        this.pages = new ArrayList<>();
    }

    public File getSourceFile() { return sourceFile; }
    public void setSourceFile(File sourceFile) { this.sourceFile = sourceFile; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public byte[] getRawBytes() { return rawBytes; }
    public void setRawBytes(byte[] rawBytes) { this.rawBytes = rawBytes; }

    public List<BufferedImage> getPages() { return pages; }
    public void setPages(List<BufferedImage> pages) { this.pages = pages; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
