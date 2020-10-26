package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.stage3.Document;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DocumentImpl implements Document {
        int textHashCode;
        URI uri;
        String text;
        byte[] pdfData;
        String[] words;
        Map<String, Integer> wordCount = new HashMap<>();

    public DocumentImpl(URI uri, String text, int textHashCode){
        if(text == null){
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.text = text;
        this.textHashCode = textHashCode;
        this.pdfData = null;
        this.words = text.replaceAll("[^a-zA-Z0-9\\s+]", "").toLowerCase().split("\\s+");
        for(String word: this.words){
            Integer previousWordCount = this.wordCount.get(word);
            this.wordCount.put(word, 1 + (previousWordCount == null ? 0 : previousWordCount));
        }
    }
    public DocumentImpl(URI uri, String text, int textHashCode, byte[] pdfData){
        if(text == null){
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.text = text;
        this.textHashCode = textHashCode;
        this.pdfData = pdfData;
        this.words = text.replaceAll("[^a-zA-Z0-9\\s+]", "").toLowerCase().split("\\s+");
        for(String word: this.words){
            Integer previousCount = this.wordCount.get(word);
            this.wordCount.put(word, 1 + (previousCount != null ? previousCount : 0));
        }
    }
    /**
     * @return the document as a PDF
     */
    @Override
    public byte[] getDocumentAsPdf(){
        if(this.pdfData == null){
            this.pdfData = this.convertTextToPdfData(this.text);
        }
        return this.pdfData;
    }

    /**
     * @return the document as a Plain String
     */
    @Override
    public String getDocumentAsTxt(){
        return this.text.trim();
    }

    /**
     * @return hash code of the plain text version of the document
     */
    @Override
    public int getDocumentTextHashCode(){
        return this.textHashCode;
    }

    /**
     * @return URI which uniquely identifies this document
     */
    @Override
    public URI getKey(){
        return this.uri;
    }

    @Override
    public int wordCount(String word) {
        Integer wordCount = this.wordCount.get(word.toLowerCase());
        return (wordCount != null ? wordCount : 0);
    }

    String[] getWords(){
        return this.words;
    }
    private byte[] convertTextToPdfData(String text){
        byte[] pdfData = null;
        try{
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream contents = new PDPageContentStream(document, page);
            contents.beginText();
            PDFont font = PDType1Font.HELVETICA_BOLD;
            contents.setFont(font, 12);
            contents.newLineAtOffset(100, 700);
            contents.showText(text);
            contents.endText();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            contents.close();
            document.save(outputStream);
            document.close();
            pdfData = outputStream.toByteArray();
        }catch(IOException e){
            e.printStackTrace();
        }
        return pdfData;
    }
}