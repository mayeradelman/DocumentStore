package edu.yu.cs.com1320.project.stage2;

import java.io.InputStream;
import java.net.URI;

public interface DocumentStore
{
	/**
	 * the two document formats supported by this document store.
	 * Note that TXT means plain text, i.e. a String.
	 */
	static enum DocumentFormat{
		TXT,PDF
	};
	/**
	 * @param input the document being put
	 * @param uri unique identifier for the document
	 * @param format indicates which type of document format is being passed
	 * @return the hashcode of the String version of the document
	 */
	int putDocument(InputStream input, URI uri, DocumentFormat format);

	/**
	 * @param uri the unique identifier of the document to get
	 * @return the given document as a PDF, or null if no document exists with that URI
	 */
	byte[] getDocumentAsPdf(URI uri);

	/**
	 * @param uri the unique identifier of the document to get
	 * @return the given document as TXT, i.e. a String, or null if no document exists with that URI
	 */
	String getDocumentAsTxt(URI uri);

	/**
	 * @param uri the unique identifier of the document to delete
	 * @return true if the document is deleted, false if no document exists with that URI
	 */
	boolean deleteDocument(URI uri);

	/**
	 * undo the last put or delete command
	 * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
	 */
	void undo() throws IllegalStateException;

	/**
	 * undo the last put or delete that was done with the given URI as its key
	 * @param uri the uri of the most previous action which is to be undone
	 * @throws IllegalStateException if there are no actions on the command stack for the given URI
	 */
	void undo(URI uri) throws IllegalStateException;

}