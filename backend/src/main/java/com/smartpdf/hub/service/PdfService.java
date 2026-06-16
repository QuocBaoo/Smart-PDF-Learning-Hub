package com.smartpdf.hub.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfService {

    public static class PdfPageContent {
        private final int pageNumber;
        private final String text;

        public PdfPageContent(int pageNumber, String text) {
            this.pageNumber = pageNumber;
            this.text = text;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public String getText() {
            return text;
        }
    }

    public List<PdfPageContent> extractTextByPage(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            return extractText(document);
        }
    }

    public List<PdfPageContent> extractTextByPageFromUrl(String fileUrl) throws IOException {
        URL url = URI.create(fileUrl).toURL();
        try (InputStream in = url.openStream();
             PDDocument document = Loader.loadPDF(in.readAllBytes())) {
            return extractText(document);
        }
    }

    private List<PdfPageContent> extractText(PDDocument document) throws IOException {
        List<PdfPageContent> pagesContent = new ArrayList<>();
        int totalPages = document.getNumberOfPages();
        PDFTextStripper stripper = new PDFTextStripper();

        for (int i = 1; i <= totalPages; i++) {
            stripper.setStartPage(i);
            stripper.setEndPage(i);
            String pageText = stripper.getText(document);
            pagesContent.add(new PdfPageContent(i, pageText.trim()));
        }
        return pagesContent;
    }
}
