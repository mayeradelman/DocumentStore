package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.DocumentStore;
import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Before;
import org.junit.Test;


import java.io.*;
import java.net.URI;

import static org.junit.Assert.*;

public class DocumentStoreImplTest {
	DocumentStoreImpl store;
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
}