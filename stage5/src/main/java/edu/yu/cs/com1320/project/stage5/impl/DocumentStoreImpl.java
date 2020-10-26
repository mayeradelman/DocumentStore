package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;

import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;

public class DocumentStoreImpl implements DocumentStore {

	/**
	 * the two document formats supported by this document store.
	 * Note that TXT means plain text, i.e. a String.
	 */
	File baseDir;
	BTreeImpl<URI, Document> bTree;
	PersistenceManager dpm;
	StackImpl<Undoable> commandStack = new StackImpl<>();
	TrieImpl<URI> trie = new TrieImpl<>();
	MinHeapImpl<DocumentGetter> minHeap = new MinHeapImpl<>();
	int maxDocumentCount;
	int maxDocumentBytes;
	int documentCount;
	int byteCount;

	public DocumentStoreImpl(){
		this.maxDocumentCount = Integer.MAX_VALUE;
		this.maxDocumentBytes = Integer.MAX_VALUE;
		this.documentCount = 0;
		this.byteCount = 0;
		this.bTree = new BTreeImpl<>();
		try{
			URI sentinel = new URI("");
			this.bTree.put(sentinel, null);
		}catch (URISyntaxException e) {
			e.printStackTrace();
		}
		this.baseDir = new File(System.getProperty("user.dir"));
		this.dpm = new DocumentPersistenceManager(this.baseDir);
		this.bTree.setPersistenceManager(this.dpm);
	}
	public DocumentStoreImpl(File baseDir){
		this.maxDocumentCount = Integer.MAX_VALUE;
		this.maxDocumentBytes = Integer.MAX_VALUE;
		this.documentCount = 0;
		this.byteCount = 0;
		this.bTree = new BTreeImpl<>();
		try{
			URI sentinel = new URI("");
			this.bTree.put(sentinel, null);
		}catch (URISyntaxException e) {
			e.printStackTrace();
		}
		this.baseDir = baseDir;
		this.dpm = new DocumentPersistenceManager(baseDir);
		this.bTree.setPersistenceManager(this.dpm);
	}

	/**
	 * @param input the document being put
	 * @param uri unique identifier for the document
	 * @param format indicates which type of document format is being passed
	 * @return the hashcode of the String version of the document
	 */
	@Override
	public int putDocument(InputStream input, URI uri, DocumentStore.DocumentFormat format) {
		if (uri == null || format == null) {
			throw new IllegalArgumentException();
		}
		DocumentImpl previousDocument = (DocumentImpl) this.bTree.get(uri);
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
			undo = (URI commandUri) -> 	{
				this.implementDelete(newDocument);
				return true;
			};
		}else if(previousDocument.getDocumentTextHashCode() != newDocument.getDocumentTextHashCode()){
			this.implementOverride(newDocument, previousDocument);
			undo = (URI commandUri) -> {
				this.implementOverride(previousDocument, newDocument);
				return true;
			};
		}else{
			newDocument.setLastUseTime(System.nanoTime());
		}
		this.commandStack.push(new GenericCommand(uri, undo));
		return hashCodeToReturn;
	}

	/**
	 * @param uri the unique identifier of the document to get
	 * @return the given document as a PDF, or null if no document exists with that URI
	 */

	protected Document getDocument(URI uri){
		String potentialJsonFilePath = this.baseDir + uri.toString().replace("/", File.separator).replaceFirst(uri.getScheme() + ":" + File.separator + File.separator, "") + ".json";
		File potentialJsonFile = new File(potentialJsonFilePath);
		Boolean docIsInMemory = !potentialJsonFile.exists();
		if(!docIsInMemory){
			return null;
		}
		return this.bTree.get(uri);
	}

	@Override
	public byte[] getDocumentAsPdf(URI uri){
		String potentialJsonFilePath = this.baseDir + uri.toString().replace("/", File.separator).replaceFirst(uri.getScheme() + ":" + File.separator + File.separator, "") + ".json";
		File potentialJsonFile = new File(potentialJsonFilePath);
		Boolean docWasInMemory = !potentialJsonFile.exists();
		Document document = this.bTree.get(uri);
		if(document == null) {
			return null;
		}
		document.setLastUseTime(System.nanoTime());
		if(docWasInMemory){
			this.minHeap.reHeapify(new DocumentGetter(uri));
		}else{
			this.minHeap.insert(new DocumentGetter(uri));
			this.documentCount++;
			this.byteCount = this.byteCount + ((DocumentImpl) document).byteUsage();
			try {
				this.manageSpace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return document.getDocumentAsPdf();
	}

	/**
	 * @param uri the unique identifier of the document to get
	 * @return the given document as TXT, i.e. a String, or null if no document exists with that URI
	 */
	@Override
	public String getDocumentAsTxt(URI uri){
		String potentialJsonFilePath = this.baseDir + uri.toString().replace("/", File.separator).replaceFirst(uri.getScheme() + ":" + File.separator + File.separator, "") + ".json";
		File potentialJsonFile = new File(potentialJsonFilePath);
		Boolean docWasInMemory = !potentialJsonFile.exists();
		Document document = this.bTree.get(uri);
		if(document == null) {
			return null;
		}
		document.setLastUseTime(System.nanoTime());
		if(docWasInMemory){
			DocumentGetter docGetter = new DocumentGetter(uri);
			this.minHeap.reHeapify(new DocumentGetter(uri));
		}else{
			this.minHeap.insert(new DocumentGetter(uri));
			this.documentCount++;
			this.byteCount = this.byteCount + ((DocumentImpl) document).byteUsage();
			try {
				this.manageSpace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return document.getDocumentAsTxt();
	}

	/**
	 * @param uri the unique identifier of the document to delete
	 * @return true if the document is deleted, false if no document exists with that URI
	 */
	@Override
	public boolean deleteDocument(URI uri){
		DocumentImpl documentToDelete = (DocumentImpl) this.bTree.get(uri);
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
		HashSet<URI> commandUris = new HashSet<>();
		if(this.commandStack.peek() instanceof CommandSet){
			CommandSet commandSet = (CommandSet) this.commandStack.peek();
			Iterator<GenericCommand<URI>> commandSetIterator = commandSet.iterator();
			while(commandSetIterator.hasNext()){
				commandUris.add(commandSetIterator.next().getTarget());
			}
		}
		this.commandStack.pop().undo();
		long lastUsedTime = System.nanoTime();
		for(URI uri : commandUris){
			this.getDocument(uri).setLastUseTime(lastUsedTime);
		}
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
		List<URI> documentsURIs = this.trie.getAllSorted(keyword, this.generateKeywordComparator(keyword));
		List<String> documentsAsTexts = new LinkedList<>();
		for(URI documentUri : documentsURIs){
			String potentialJsonFilePath = this.baseDir + documentUri.toString().replace("/", File.separator).replaceFirst(documentUri.getScheme() + ":" + File.separator + File.separator, "") + ".json";
			File potentialJsonFile = new File(potentialJsonFilePath);
			Boolean docWasInMemory = !potentialJsonFile.exists();
			Document document = this.bTree.get(documentUri);
			documentsAsTexts.add(document.getDocumentAsTxt());
			document.setLastUseTime(System.nanoTime());
			if(docWasInMemory){
				this.minHeap.reHeapify(new DocumentGetter(documentUri));
			}else{
				this.minHeap.insert(new DocumentGetter(documentUri));
				this.documentCount++;
				this.byteCount = this.byteCount + ((DocumentImpl) document).byteUsage();
				try {
					this.manageSpace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return documentsAsTexts;
	}

	/**
	 * same logic as search, but returns the docs as PDFs instead of as Strings
	 */
	@Override
	public List<byte[]> searchPDFs(String keyword) {
		List<URI> documentsURIs = this.trie.getAllSorted(keyword, this.generateKeywordComparator(keyword));
		List<byte[]> documentsAsPdfs = new LinkedList<>();
		for(URI documentUri : documentsURIs){
			String potentialJsonFilePath = this.baseDir + documentUri.toString().replace("/", File.separator).replaceFirst(documentUri.getScheme() + ":" + File.separator + File.separator, "") + ".json";
			File potentialJsonFile = new File(potentialJsonFilePath);
			Boolean docWasInMemory = !potentialJsonFile.exists();
			Document document = this.bTree.get(documentUri);
			documentsAsPdfs.add(document.getDocumentAsPdf());
			document.setLastUseTime(System.nanoTime());
			if(docWasInMemory){
				this.minHeap.reHeapify(new DocumentGetter(documentUri));
			}else{
				this.minHeap.insert(new DocumentGetter(documentUri));
				this.documentCount++;
				this.byteCount = this.byteCount + ((DocumentImpl) document).byteUsage();
				try {
					this.manageSpace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
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
		List<URI> documentsURIs = this.trie.getAllWithPrefixSorted(prefix, this.generateKeywordPrefixComparator(prefix));
		List<String> documentsAsTexts = new LinkedList<>();
		for(URI documentUri : documentsURIs){
			String potentialJsonFilePath = this.baseDir + documentUri.toString().replace("/", File.separator).replaceFirst(documentUri.getScheme() + ":" + File.separator + File.separator, "") + ".json";
			File potentialJsonFile = new File(potentialJsonFilePath);
			Boolean docWasInMemory = !potentialJsonFile.exists();
			Document document = this.bTree.get(documentUri);
			documentsAsTexts.add(document.getDocumentAsTxt());
			document.setLastUseTime(System.nanoTime());
			if(docWasInMemory){
				this.minHeap.reHeapify(new DocumentGetter(documentUri));
			}else{
				this.minHeap.insert(new DocumentGetter(documentUri));
				this.documentCount++;
				this.byteCount = this.byteCount + ((DocumentImpl) document).byteUsage();
				try {
					this.manageSpace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return documentsAsTexts;
	}
	/**
	 * same logic as searchByPrefix, but returns the docs as PDFs instead of as Strings
	 */
	@Override
	public List<byte[]> searchPDFsByPrefix(String prefix){
		List<URI> documentsURIs = this.trie.getAllWithPrefixSorted(prefix, this.generateKeywordPrefixComparator(prefix));
		List<byte[]> documentsAsPdfs = new LinkedList<>();
		for(URI documentUri : documentsURIs){
			String potentialJsonFilePath = this.baseDir + documentUri.toString().replace("/", File.separator).replaceFirst(documentUri.getScheme() + ":" + File.separator + File.separator, "") + ".json";
			File potentialJsonFile = new File(potentialJsonFilePath);
			Boolean docWasInMemory = !potentialJsonFile.exists();
			Document document = this.bTree.get(documentUri);
			documentsAsPdfs.add(document.getDocumentAsPdf());
			document.setLastUseTime(System.nanoTime());
			if(docWasInMemory){
				this.minHeap.reHeapify(new DocumentGetter(documentUri));
			}else{
				this.minHeap.insert(new DocumentGetter(documentUri));
				this.documentCount++;
				this.byteCount = this.byteCount + ((DocumentImpl) document).byteUsage();
				try {
					this.manageSpace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
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
		Set<URI> documentsToDeleteUris = new HashSet<>(this.trie.getAllSorted(keyword, this.generateKeywordComparator(keyword)));
		CommandSet<URI> commandSet = new CommandSet<>();
		this.trie.deleteAll(keyword);
		for(URI documentToDeleteUri : documentsToDeleteUris){
			Document documentToDelete = this.bTree.get(documentToDeleteUri);
			this.implementDelete((DocumentImpl) documentToDelete);
			Function<URI, Boolean> undo = (URI commandUri) -> {
				this.implementPut((DocumentImpl) documentToDelete);
				return true;
			};
			commandSet.addCommand(new GenericCommand<>(documentToDelete.getKey(), undo));
		}
		this.commandStack.push(commandSet);
		return documentsToDeleteUris;
	}
	/**
	 * Completely remove any trace of any document which contains a word that has the given prefix
	 * Search is CASE INSENSITIVE.
	 * @param keywordPrefix
	 * @return a Set of URIs of the documents that were deleted.
	 */
	@Override
	public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
		Set<URI> documentsToDeleteUris = new HashSet<>(this.trie.getAllWithPrefixSorted(keywordPrefix, this.generateKeywordPrefixComparator(keywordPrefix)));
		CommandSet<URI> commandSet = new CommandSet<>();
		this.trie.deleteAll(keywordPrefix);
		for(URI documentToDeleteUri : documentsToDeleteUris){
			Document documentToDelete = this.bTree.get(documentToDeleteUri);
			this.implementDelete((DocumentImpl) documentToDelete);
			Function<URI, Boolean> undo = (URI commandUri) -> {
				this.implementPut((DocumentImpl) documentToDelete);
				return true;
			};
			commandSet.addCommand(new GenericCommand<>(documentToDelete.getKey(), undo));
		}
		this.commandStack.push(commandSet);
		return documentsToDeleteUris;
	}

	@Override
	public void setMaxDocumentCount(int limit) {
		this.maxDocumentCount = limit;
		try {
			this.manageSpace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setMaxDocumentBytes(int limit) {
		this.maxDocumentBytes = limit;
		try {
			this.manageSpace();
		} catch (Exception e) {
			e.printStackTrace();
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
	private void implementPut(DocumentImpl newDocument){
		URI documentUri = newDocument.getKey();
		this.bTree.put(documentUri, newDocument);
		for(String key : newDocument.getWords()) {
			this.trie.put(key, documentUri);
		}
		newDocument.setLastUseTime(System.nanoTime());
		DocumentGetter newDocumentGetter = new DocumentGetter(documentUri);
		this.minHeap.insert(newDocumentGetter);
		this.documentCount++;
		this.byteCount = this.byteCount + newDocument.byteUsage();
		try {
			this.manageSpace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void implementOverride(DocumentImpl newDocument, DocumentImpl previousDocument){
		URI documentsUri = newDocument.getKey();
		String potentialJsonFilePath = this.baseDir + documentsUri.toString().replace("/", File.separator).replaceFirst(documentsUri.getScheme() + ":" + File.separator + File.separator, "") + ".json";
		File potentialJsonFile = new File(potentialJsonFilePath);
		Boolean docIsInMemory = !potentialJsonFile.exists();
		if(docIsInMemory){
			previousDocument.setLastUseTime(Long.MIN_VALUE);
			this.minHeap.reHeapify(new DocumentGetter(documentsUri));
			this.minHeap.removeMin();
		}
		this.bTree.put(documentsUri, newDocument);
		for(String key : previousDocument.getWords()) {
			this.trie.delete(key, documentsUri);
		}

		for(String key : newDocument.getWords()) {
			this.trie.put(key, documentsUri);
		}
		newDocument.setLastUseTime(System.nanoTime());
		DocumentGetter newDocumentGetter = new DocumentGetter(documentsUri);
		this.minHeap.insert(newDocumentGetter);

		this.byteCount = (this.byteCount + newDocument.byteUsage()) - previousDocument.byteUsage();
		try {
			this.manageSpace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void implementDelete(DocumentImpl documentToDelete){
		URI documentUri = documentToDelete.getKey();
		String potentialJsonFilePath = this.baseDir + documentUri.toString().replace("/", File.separator).replaceFirst(documentUri.getScheme() + ":" + File.separator + File.separator, "") + ".json";
		File potentialJsonFile = new File(potentialJsonFilePath);
		Boolean docIsInMemory = !potentialJsonFile.exists();
		if(docIsInMemory){
			documentToDelete.setLastUseTime(Long.MIN_VALUE);
			this.minHeap.reHeapify(new DocumentGetter(documentUri));
			this.minHeap.removeMin();
		}
		this.bTree.put(documentUri, null);
		for(String key : documentToDelete.getWords()) {
			this.trie.delete(key, documentUri);
		}
		this.documentCount--;
		this.byteCount = this.byteCount - documentToDelete.byteUsage();
	}
	private Comparator<URI> generateKeywordComparator(String keyword){
		return (v1, v2) -> (this.bTree.get(v2).wordCount(keyword) - this.bTree.get(v1).wordCount(keyword));
	}
	private Comparator<URI> generateKeywordPrefixComparator(String keywordPrefix){
		return (v1, v2) -> {
			int v1PrefixCount = 0;
			for (String word : ((DocumentImpl) this.bTree.get(v1)).getWords()) {
				if (word.startsWith(keywordPrefix)) {
					v1PrefixCount++;
				}
			}
			int v2PrefixCount = 0;
			for (String word : ((DocumentImpl) this.bTree.get(v2)).getWords()) {
				if (word.startsWith(keywordPrefix)) {
					v2PrefixCount++;
				}
			}
			return v2PrefixCount - v1PrefixCount;
		};
	}
	private void manageSpace() throws Exception {
		while(this.documentCount > maxDocumentCount || this.byteCount > this.maxDocumentBytes){
			URI uriToWriteToDisk = this.minHeap.removeMin().getUri();
			DocumentImpl documentToWriteToDisk = (DocumentImpl) this.bTree.get(uriToWriteToDisk);
			this.bTree.moveToDisk(uriToWriteToDisk);
			this.documentCount--;
			this.byteCount = this.byteCount - documentToWriteToDisk.byteUsage();
		}
	}
	private class DocumentGetter implements Comparable<DocumentGetter>{
		URI uri;
		DocumentGetter(URI uri){
			this.uri = uri;
		}
		URI getUri(){
			return this.uri;
		}
		Document getDocument(){
			return bTree.get(this.uri);
		}

		@Override
		public int compareTo(DocumentGetter that) {
			return this.getDocument().compareTo(that.getDocument());
		}

		@Override
		public boolean equals(Object o){
			if(o == this){
				return true;
			}
			if(!(o instanceof  DocumentGetter)){
				return false;

			}
			DocumentGetter dg = (DocumentGetter) o;
			return this.getUri().equals(dg.getUri());
		}
		@Override
		public int hashCode(){
			return this.uri.hashCode();
		}
	}
}