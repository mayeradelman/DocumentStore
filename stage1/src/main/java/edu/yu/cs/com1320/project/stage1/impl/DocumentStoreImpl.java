package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage1.DocumentStore;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.net.URI;

public class DocumentStoreImpl implements DocumentStore {

    /**
     * the two document formats supported by this document store.
     * Note that TXT means plain text, i.e. a String.
     */
    HashTableImpl<URI,DocumentImpl> hashTable = new HashTableImpl<>();
    /**
     * @param input the document being put
     * @param uri unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return the hashcode of the String version of the document
     */

    @Override
    public int putDocument(InputStream input, URI uri, DocumentStore.DocumentFormat format){
        if(uri == null || format == null){
            throw new IllegalArgumentException();
        }
        String previousString = this.getDocumentAsTxt(uri);
        int hashCodeToReturn = 0;
        if(previousString != null){
            hashCodeToReturn = previousString.hashCode();
        }
        if(input == null){
            this.deleteDocument(uri);
            return hashCodeToReturn;
        }
        byte[] data = this.convertInputStreamToByteArray(input);
        DocumentImpl document = null;
        String text = null;
        switch(format){
            case TXT:
                text = new String(data).trim();
                document = new DocumentImpl(uri, text, text.hashCode());
                break;
            case PDF:
                text = this.convertPdfDataToString(data).trim();
                document = new DocumentImpl(uri, text, text.hashCode() , data);
        }
        if(!this.alreadyMapped(uri, document)) {
            this.hashTable.put(uri, document);
        }
        return hashCodeToReturn;
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document as a PDF, or null if no document exists with that URI
     */
    @Override
    public byte[] getDocumentAsPdf(URI uri){
        DocumentImpl document = this.hashTable.get(uri);
        if(document == null) {
            return null;
        }
        return document.getDocumentAsPdf();
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document as TXT, i.e. a String, or null if no document exists with that URI
     */
    @Override
    public String getDocumentAsTxt(URI uri){
        DocumentImpl document = this.hashTable.get(uri);
        if(document == null) {
            return null;
        }
        return document.getDocumentAsTxt();
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    @Override
    public boolean deleteDocument(URI uri){
        DocumentImpl previousDocument = this.hashTable.put(uri,null);
        return previousDocument != null;
    }
    private boolean alreadyMapped(URI uri, DocumentImpl document){
        DocumentImpl previousDocument = this.hashTable.get(uri);
        if(previousDocument != null && previousDocument.getDocumentTextHashCode() == document.getDocumentTextHashCode()){
            return true;
        }
        return false;
    }
    private byte[] convertInputStreamToByteArray(InputStream input) {
        ByteArrayOutputStream outputStream = null;
        byte[] byteArray = null;
        try {
            outputStream = new ByteArrayOutputStream();
            int data = input.read();
            while (data != -1) {
                outputStream.write(data);
                data = input.read();
            }
            byteArray = outputStream.toByteArray();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }
    private String convertPdfDataToString(byte[] pdfData) {
        PDDocument document = null;
        String string = null;
        try {
            document = PDDocument.load(pdfData);
            PDFTextStripper textStripper = new PDFTextStripper();
            string = textStripper.getText(document);
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return string;
    }
}
