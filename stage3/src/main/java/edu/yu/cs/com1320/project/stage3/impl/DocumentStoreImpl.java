package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.DocumentStore;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.function.Function;

public class DocumentStoreImpl implements DocumentStore {

	/**
	 * the two document formats supported by this document store.
	 * Note that TXT means plain text, i.e. a String.
	 */
	HashTableImpl<URI,DocumentImpl> hashTable = new HashTableImpl<>();
	StackImpl<Undoable> commandStack = new StackImpl<>();
	TrieImpl<Document> trie = new TrieImpl<>();


	/**
	 * @param input the document being put
	 * @param uri unique identifier for the document
	 * @param format indicates which type of document format is being passed
	 * @return the hashcode of the String version of the document
	 */

	@Override
	public int putDocument(InputStream input, URI uri, DocumentStore.DocumentFormat format) {
		if(uri == null || format == null) {
			throw new IllegalArgumentException();
		}
		DocumentImpl previousDocument = this.hashTable.get(uri);
		int hashCodeToReturn = 0;
		if (previousDocument != null) {
			hashCodeToReturn = previousDocument.getDocumentTextHashCode();
		}
		if(input == null) {
			this.deleteDocument(uri);
			return hashCodeToReturn;
		}
		DocumentImpl newDocument = this.constructDocument(input, uri, format);
		Function<URI, Boolean> undo = (URI commandUri) -> true;
		if(previousDocument == null){
			this.implementPut(newDocument);
			undo = (URI commandUri) -> {
				this.implementDelete(newDocument);
				return true;
			};
		}else if(previousDocument.getDocumentTextHashCode() != newDocument.getDocumentTextHashCode()){
			this.implementOverride(newDocument, previousDocument);
			undo = (URI commandUri) -> {
				this.implementOverride(previousDocument, newDocument);
				return true;
			};
		}
		this.commandStack.push(new GenericCommand(uri, undo));
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
		DocumentImpl documentToDelete = this.hashTable.get(uri);
		Function<URI, Boolean> undo = (URI commandUri) -> true;
		if(documentToDelete != null){
			this.implementDelete(documentToDelete);
			undo = (URI commandUri) -> {
				this.implementPut(documentToDelete);
				return true;
			};
		}
		this.commandStack.push(new GenericCommand(uri, undo));
		return documentToDelete != null;
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
		StackImpl<Undoable> tempStack = new StackImpl<>();
		GenericCommand<URI> genericCommand = null;
		CommandSet<URI> commandSet = null;
		while(this.commandStack.peek() != null){
			Undoable command = this.commandStack.pop();
			if(command instanceof  GenericCommand){
				if(((GenericCommand<URI>) command).getTarget().equals(uri)){
					genericCommand = (GenericCommand<URI>) command;
					break;
				}
			}
			if(command instanceof CommandSet){
				if(((CommandSet<URI>) command).containsTarget(uri)){
					commandSet = (CommandSet<URI>) command;
					break;
				}
			}
			tempStack.push(command);
		}
		if(genericCommand != null){
			genericCommand.undo();
		}else if(commandSet != null){
			commandSet.undo(uri);
			if(commandSet.size() != 0) {
				this.commandStack.push(commandSet);
			}
		}else{
			while(tempStack.peek() != null){
				Undoable command = tempStack.pop();
				this.commandStack.push(command);
			}
			throw new IllegalStateException();
		}
		while(tempStack.peek() != null){
			Undoable command = tempStack.pop();
			this.commandStack.push(command);
		}
	}
	/**
	 * Retrieve all documents whose text contains the given keyword.
	 * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
	 * Search is CASE INSENSITIVE.
	 * @param keyword
	 * @return a List of the matches. If there are no matches, return an empty list.
	 */
	@Override
	public List<String> search(String keyword) {
		List<Document> documents = this.trie.getAllSorted(keyword, this.generateKeywordComparator(keyword));
		List<String> documentsAsTexts = new LinkedList<>();
		for(Document document : documents) {
			documentsAsTexts.add(document.getDocumentAsTxt());
		}
		return documentsAsTexts;
	}

	/**
	 * same logic as search, but returns the docs as PDFs instead of as Strings
	 */
	@Override
	public List<byte[]> searchPDFs(String keyword) {
		List<Document> documents = this.trie.getAllSorted(keyword, this.generateKeywordComparator(keyword));
		List<byte[]> documentsAsPdfs = new LinkedList<>();
		for (Document document : documents) {
			documentsAsPdfs.add(document.getDocumentAsPdf());
		}
		return documentsAsPdfs;
	}
	/**
	 * Retrieve all documents whose text starts with the given prefix
	 * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
	 * Search is CASE INSENSITIVE.
	 * @param prefix
	 * @return a List of the matches. If there are no matches, return an empty list.
	 */
	@Override
	public List<String> searchByPrefix(String prefix){
		List<Document> documents = this.trie.getAllWithPrefixSorted(prefix, this.generatePrefixComparator(prefix));
		List<String> documentsAsTexts = new LinkedList<>();
		for(Document document : documents){
			documentsAsTexts.add(document.getDocumentAsTxt());
		}
		return documentsAsTexts;
	}
	/**
	 * same logic as searchByPrefix, but returns the docs as PDFs instead of as Strings
	 */
	@Override
	public List<byte[]> searchPDFsByPrefix(String prefix){
		List<Document> documents = this.trie.getAllWithPrefixSorted(prefix, this.generatePrefixComparator(prefix));
		List<byte[]> documentsAsPdfs = new LinkedList<>();
		for(Document document : documents){
			documentsAsPdfs.add(document.getDocumentAsPdf());
		}
		return documentsAsPdfs;
	}
	/**
	 * Completely remove any trace of any document which contains the given keyword
	 * @param keyword
	 * @return a Set of URIs of the documents that were deleted.
	 */
	@Override
	public Set<URI> deleteAll(String keyword) {
		Set<Document> documentsToDelete = new HashSet<>(this.trie.getAllSorted(keyword, this.generateKeywordComparator(keyword)));
		Set<URI> deletedUris = new HashSet<>();
		CommandSet<URI> commandSet = new CommandSet<>();
		if(!documentsToDelete.isEmpty()) {
			this.trie.deleteAll(keyword);
			for (Document documentToDelete : documentsToDelete) {
				this.implementDelete((DocumentImpl) documentToDelete);
				deletedUris.add(documentToDelete.getKey());
				Function<URI, Boolean> undo = (URI commandUri) -> {
					this.implementPut((DocumentImpl) documentToDelete);
					return true;
				};
				commandSet.addCommand(new GenericCommand<>(documentToDelete.getKey(), undo));
			}
		}
		this.commandStack.push(commandSet);
		return deletedUris;
	}
	/**
	 * Completely remove any trace of any document which contains a word that has the given prefix
	 * Search is CASE INSENSITIVE.
	 * @param keywordPrefix
	 * @return a Set of URIs of the documents that were deleted.
	 */
	@Override
	public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
		Set<Document> documentsToDelete = new HashSet<>(this.trie.getAllWithPrefixSorted(keywordPrefix, this.generatePrefixComparator(keywordPrefix)));
		Set<URI> deletedUris = new HashSet<>();
		CommandSet<URI> commandSet = new CommandSet<>();
		if(!documentsToDelete.isEmpty()) {
			this.trie.deleteAllWithPrefix(keywordPrefix);
			for (Document documentToDelete : documentsToDelete) {
				this.implementDelete((DocumentImpl) documentToDelete);
				deletedUris.add(documentToDelete.getKey());
				Function<URI, Boolean> undo = (URI commandUri) -> {
					this.implementPut((DocumentImpl) documentToDelete);
					return true;
				};
				commandSet.addCommand(new GenericCommand<>(documentToDelete.getKey(), undo));
			}
		}
		this.commandStack.push(commandSet);
		return deletedUris;
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
	private void implementPut(DocumentImpl newDocument){
		this.hashTable.put(newDocument.getKey(), newDocument);
		for(String key : newDocument.getWords()) {
			this.trie.put(key, newDocument);
		}
	}
	private void implementOverride(DocumentImpl newDocument, DocumentImpl previousDocument){
		this.hashTable.put(newDocument.getKey(), newDocument);
		for(String key : newDocument.getWords()) {
			this.trie.put(key, newDocument);
		}
		for(String key : previousDocument.getWords()) {
			this.trie.delete(key, previousDocument);
		}
	}
	private void implementDelete(DocumentImpl documentToDelete){
		this.hashTable.put(documentToDelete.getKey(), null);
		for(String key : documentToDelete.getWords()) {
			this.trie.delete(key, documentToDelete);
		}
	}
	private Comparator<Document> generateKeywordComparator(String keyword){
		return (v1, v2) -> (v2.wordCount(keyword) - v1.wordCount(keyword));
	}
	private Comparator<Document> generatePrefixComparator(String keywordPrefix){
		return (v1, v2) -> {
			int v1PrefixCount = 0;
			for (String word : ((DocumentImpl) v1).getWords()) {
				if (word.startsWith(keywordPrefix)) {
					v1PrefixCount++;
				}
			}
			int v2PrefixCount = 0;
			for (String word : ((DocumentImpl) v2).getWords()) {
				if (word.startsWith(keywordPrefix)) {
					v2PrefixCount++;
				}
			}
			return v2PrefixCount - v1PrefixCount;
		};
	}
}