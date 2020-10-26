package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.Document;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;

public class DocumentImpl implements Document {
        int textHashCode;
        URI uri;
        String text;
        byte[] pdfData;
    public DocumentImpl(URI uri, String text, int textHashCode){
        if(text == null){
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.text = text;
        this.textHashCode = textHashCode;
        this.pdfData = null;
    }
    public DocumentImpl(URI uri, String text, int textHashCode, byte[] pdfData){
        if(text == null){
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.text = text;
        this.textHashCode = textHashCode;
        this.pdfData = pdfData;
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
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || this.getClass() != o.getClass()){
            return false;
        }
        DocumentImpl other = (DocumentImpl) o;
        return this.hashCode() == other.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uri, this.text);
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