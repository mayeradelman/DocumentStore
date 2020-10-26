package edu.yu.cs.com1320.project.stage2.impl;

import edu.yu.cs.com1320.project.Command;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage2.Document;
import edu.yu.cs.com1320.project.stage2.DocumentStore;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.net.URI;
import java.util.function.Function;

public class DocumentStoreImpl implements DocumentStore {

	/**
	 * the two document formats supported by this document store.
	 * Note that TXT means plain text, i.e. a String.
	 */
	HashTableImpl<URI,DocumentImpl> hashTable = new HashTableImpl<>();
	StackImpl<Command> commandStack = new StackImpl<>();

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
		DocumentImpl previousDocument = this.hashTable.get(uri);
		int hashCodeToReturn = 0;
		if(previousDocument != null){
			hashCodeToReturn = previousDocument.getDocumentTextHashCode();
		}
		if(input == null){
			this.deleteDocument(uri);
			return hashCodeToReturn;
		}
		DocumentImpl newDocument = this.constructDocument(input, uri, format);
		if(previousDocument != null && previousDocument.getDocumentTextHashCode() == newDocument.getDocumentTextHashCode()){
			this.commandStack.push(new Command (uri, (URI commandURI) -> true));
			return hashCodeToReturn;
		}
		this.hashTable.put(uri, newDocument);
		Function<URI, Boolean> undo = (URI commandUri) -> {
			this.hashTable.put(commandUri, previousDocument);
			return true;
		};
		this.commandStack.push(new Command(uri, undo));
		return hashCodeToReturn;
	}

	/**
	 * @param uri the unique identifier of the document to get
	 * @return the given document as a PDF, or null if no document exists with that URI
	 */

	protected Document getDocument(URI uri){
		return this.hashTable.get(uri);
	}
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
		Function<URI, Boolean> undo = (URI commandUri) -> {
			this.hashTable.put(commandUri, previousDocument);
			return true;
		};
		this.commandStack.push(new Command(uri, undo));
		return previousDocument != null;
	}
	/**
	 * undo the last put or delete command
	 * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
	 */
	public void undo() throws IllegalStateException{
		if(this.commandStack.peek() == null){
			throw new IllegalStateException();
		}
		this.commandStack.pop().undo();
	}

	/**
	 * undo the last put or delete that was done with the given URI as its key
	 * @param uri the uri of the most previous action which is to be undone
	 * @throws IllegalStateException if there are no actions on the command stack for the given URI
	 */
	public void undo(URI uri) throws IllegalStateException{
		StackImpl<Command> tempStack = new StackImpl<>();
		while(this.commandStack.peek() != null && !this.commandStack.peek().getUri().equals(uri)){
			Command command = this.commandStack.pop();
			tempStack.push(command);
		}
		if(this.commandStack.peek() == null){
			while(tempStack.peek() != null){
				Command command = tempStack.pop();
				this.commandStack.push(command);
			}
			throw new IllegalStateException();
		}
		this.commandStack.pop().undo();
		while(tempStack.peek() != null){
			Command command = tempStack.pop();
			this.commandStack.push(command);
		}
	}
	private DocumentImpl constructDocument(InputStream input, URI uri, DocumentStore.DocumentFormat format){
		byte[] data = this.convertInputStreamToByteArray(input);
		DocumentImpl document = null;
		String text;
		switch(format){
			case TXT:
				text = new String(data).trim();
				document = new DocumentImpl(uri, text, text.hashCode());
				break;
			case PDF:
				text = this.convertPdfDataToString(data).trim();
				document = new DocumentImpl(uri, text, text.hashCode() , data);
		}
		return document;
	}
	private byte[] convertInputStreamToByteArray(InputStream input) {
		ByteArrayOutputStream outputStream;
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
		PDDocument document;
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