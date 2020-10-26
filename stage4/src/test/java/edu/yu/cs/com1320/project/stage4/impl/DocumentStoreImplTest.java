package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.stage4.DocumentStore;
import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Before;
import org.junit.Test;


import java.io.*;
import java.net.URI;

import static org.junit.Assert.*;

public class DocumentStoreImplTest {
	DocumentStoreImpl store;
	DocumentStoreImpl store2;
	InputStream text2InputStream;

	@Before
	public void setUp() throws Exception {
		this.store = new DocumentStoreImpl();
		String textFilePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "TestText.txt";
		InputStream textInputStream = new FileInputStream(textFilePath);
		this.store.putDocument(textInputStream, new URI("textURI"), DocumentStore.DocumentFormat.TXT);
		String pdfFilePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "TestPDF.pdf";
		InputStream pdfInputStream = new FileInputStream(pdfFilePath);
		this.store.putDocument(pdfInputStream, new URI("pdfURI"), DocumentStore.DocumentFormat.PDF);
		String text2FilePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "TestText2.txt";
		this.text2InputStream = new FileInputStream(text2FilePath);

		this.store2 = new DocumentStoreImpl();
		InputStream textInputStream2 = new FileInputStream(textFilePath);
		InputStream text2InputStream2 = new FileInputStream(text2FilePath);
		InputStream pdfInputStream2 = new FileInputStream(pdfFilePath);
		store2.putDocument(textInputStream2, new URI("textURI"), DocumentStore.DocumentFormat.TXT);
		store2.putDocument(text2InputStream2, new URI("text2URI"), DocumentStore.DocumentFormat.TXT);
		store2.putDocument(pdfInputStream2, new URI("pdfURI"), DocumentStore.DocumentFormat.PDF);
	}

	@Test
	public void putDocumentOverride() throws Exception{
		int previousStringHashCode = this.store.getDocumentAsTxt(new URI("textURI")).hashCode();
		assertEquals(this.store.putDocument(this.text2InputStream, new URI("textURI"), DocumentStore.DocumentFormat.TXT), previousStringHashCode);
		String text2ExpectedString = "This is another text file.";
		assertEquals(text2ExpectedString, this.store.getDocumentAsTxt(new URI("textURI")));
	}
	@Test
	public void putDocumentNullInput() throws Exception{
		String textExpectedString = "This is a text. This is another line of the text. This is the last line of the text.";
		int textExpectedHashCode = textExpectedString.hashCode();
		assertEquals(textExpectedHashCode, this.store.putDocument(null, new URI("textURI"), DocumentStore.DocumentFormat.TXT));
	}

	@Test
	public void getDocumentAsPdf() throws Exception{
		String textExpectedString = "This is a text. This is another line of the text. This is the last line of the text.";
		String pdfExpectedString = "This is a PDF. This is another line of the PDF. This is the last line of the PDF.";
		byte[] textData = this.store.getDocumentAsPdf(new URI("textURI"));
		byte[] pdfData = this.store.getDocumentAsPdf(new URI("pdfURI"));
		String textText = null;
		try{
			PDFTextStripper textStripper = new PDFTextStripper();
			textText = textStripper.getText(PDDocument.load(textData)).trim();
		}catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(textExpectedString, textText);
		String pdfText = null;
		try{
			PDFTextStripper textStripper2 = new PDFTextStripper();
			pdfText = textStripper2.getText(PDDocument.load(pdfData)).trim();
		}catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(pdfExpectedString, pdfText);
	}

	@Test
	public void getDocumentAsTxt() throws Exception{
		String textExpectedString = "This is a text. This is another line of the text. This is the last line of the text.";
		String pdfExpectedString = "This is a PDF. This is another line of the PDF. This is the last line of the PDF.";
		assertEquals(textExpectedString, this.store.getDocumentAsTxt(new URI("textURI")));
		assertEquals(pdfExpectedString, this.store.getDocumentAsTxt(new URI ("pdfURI")));
	}

	@Test
	public void deleteDocument() throws Exception{
		assertTrue(this.store.deleteDocument(new URI("textURI")));
		assertTrue(this.store.deleteDocument(new URI("pdfURI")));
		assertFalse(this.store.deleteDocument(new URI("fakeURI")));
	}
	@Test
	public void undo() throws Exception{
		String pdfExpectedString = "This is a PDF. This is another line of the PDF. This is the last line of the PDF.";
		assertEquals(pdfExpectedString, store2.getDocumentAsTxt(new URI("pdfURI")));
		byte[] pdfData = this.store2.getDocumentAsPdf(new URI("pdfURI"));
		String pdfText = null;
		try{
			PDFTextStripper textStripper2 = new PDFTextStripper();
			pdfText = textStripper2.getText(PDDocument.load(pdfData)).trim();
		}catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(pdfExpectedString, pdfText);
		this.store2.undo();
		assertNull(this.store2.getDocumentAsPdf(new URI("pdfURI")));
		assertNull(this.store2.getDocumentAsTxt(new URI("pdfURI")));
		assertNull(this.store2.getDocument(new URI("pdfURI")));

		String text2ExpectedString = "This is another text file.";
		assertEquals(text2ExpectedString, store2.getDocumentAsTxt(new URI("text2URI")));
		this.store2.undo();
		assertNull(store2.getDocumentAsTxt(new URI("text2URI")));

		String textExpectedString = "This is a text. This is another line of the text. This is the last line of the text.";
		assertEquals(textExpectedString, this.store.getDocumentAsTxt(new URI("textURI")));
		this.store2.deleteDocument(new URI("textURI"));
		assertNull(store2.getDocumentAsTxt(new URI("textURI")));
		this.store2.undo();
		assertEquals(textExpectedString, this.store.getDocumentAsTxt(new URI("textURI")));
	}
	@Test
	public void undoWithURI() throws Exception{
		this.store2.putDocument(this.text2InputStream, new URI("textURI"), DocumentStore.DocumentFormat.TXT);
		String textExpectedString = "This is a text. This is another line of the text. This is the last line of the text.";
		String text2ExpectedString = "This is another text file.";
		assertEquals(text2ExpectedString, store2.getDocumentAsTxt(new URI("textURI")));
		this.store2.undo(new URI("textURI"));
		assertEquals(textExpectedString, store2.getDocumentAsTxt(new URI("textURI")));
		this.store2.undo(new URI("textURI"));
		assertNull(store2.getDocumentAsTxt(new URI("textURI")));
	}
	@Test
	public void setMaxDocumentCount() throws Exception{
		assertNotNull(this.store.getDocumentAsTxt(new URI("textURI")));
		assertNotNull(this.store.getDocumentAsTxt(new URI("pdfURI")));
		this.store.setMaxDocumentCount(2);
		this.store.putDocument(this.text2InputStream, new URI("text2URI"), DocumentStore.DocumentFormat.TXT);
		assertNull(this.store.getDocumentAsTxt(new URI("txtURI")));
		assertNotNull(this.store.getDocumentAsTxt(new URI("pdfURI")));
		assertNotNull(this.store.getDocumentAsTxt(new URI("text2URI")));
		this.store.setMaxDocumentCount(1);
		assertNull(this.store.getDocumentAsTxt(new URI("pdfURI")));
		assertNotNull(this.store.getDocumentAsTxt(new URI("text2URI")));
	}
	@Test
	public void setMaxDocumentCount2() throws Exception{
		this.store2.setMaxDocumentCount(2);
		assertNull(this.store2.getDocumentAsTxt(new URI("textURI")));
		assertNotNull(this.store2.getDocumentAsTxt(new URI("text2URI")));
		assertNotNull(this.store2.getDocumentAsTxt(new URI("pdfURI")));
		this.store2.setMaxDocumentCount(1);
		assertNull(this.store2.getDocumentAsTxt(new URI("text2URI")));
		assertNotNull(this.store2.getDocumentAsTxt(new URI("pdfURI")));
		this.store2.setMaxDocumentCount(0);
		assertNull(this.store2.getDocumentAsTxt(new URI("pdfURI")));
	}
	@Test
	public void setMaxDocumentBytes2() throws Exception{
		this.store2.setMaxDocumentBytes(193262);
		assertNotNull(this.store2.getDocumentAsTxt(new URI("textURI")));
		assertNotNull(this.store2.getDocumentAsTxt(new URI("text2URI")));
		assertNotNull(this.store2.getDocumentAsTxt(new URI("pdfURI")));
		this.store2.setMaxDocumentBytes(193261);
		assertNull(this.store2.getDocumentAsTxt(new URI("textURI")));
	}
}