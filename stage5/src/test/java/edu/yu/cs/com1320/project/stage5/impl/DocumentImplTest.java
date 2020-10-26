package edu.yu.cs.com1320.project.stage5.impl;

import org.apache.pdfbox.pdmodel.PDDocument;


import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class DocumentImplTest {
	URI textUri;
	String textString;
	int textHashCode;

	URI pdfUri;
	String pdfString;
	int pdfHashCode;
	byte[] pdfData;

	@Before
	public void setUp() throws Exception{
		this.textUri = new URI("ThisIsAText");
		this.textString = "This is a text. This is another line of the text. This is the last line of the text.";
		this.textHashCode = this.textString.hashCode();

		this.pdfUri = new URI("ThisIsAPdf");
		this.pdfString = "This is a PDF. This is another line of the PDF. This is the last line of the PDF.";
		this.pdfHashCode = this.pdfString.hashCode();
		String pdfFilePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "TestPDF.pdf";
		InputStream pdfInputStream = new FileInputStream(pdfFilePath);
		ByteArrayOutputStream outputStream = null;
		try{
			outputStream = new ByteArrayOutputStream();
			int data = pdfInputStream.read();
			while (data != -1) {
				outputStream.write(data);
				data = pdfInputStream.read();
			}
			this.pdfData = outputStream.toByteArray();
			pdfInputStream.close();
			outputStream.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void textGetDocumentAsTxt() {
		DocumentImpl textDocument = new DocumentImpl(this.textUri, this.textString, this.textHashCode);
		assertEquals(this.textString, textDocument.getDocumentAsTxt());
		assertNotEquals(this.textString, "hi");
	}
	@Test
	public void pdfGetDocumentAsTxt() {
		DocumentImpl pdfDocument = new DocumentImpl(this.pdfUri, this.pdfString, this.pdfHashCode, this.pdfData);
		assertEquals(this.pdfString, pdfDocument.getDocumentAsTxt());
		assertNotEquals(this.pdfString, "hi");
	}
	@Test
	public void textGetDocumentAsPdf() {
		DocumentImpl textDocument = new DocumentImpl(this.textUri, this.textString, this.textHashCode);
		byte[] pdfData = textDocument.getDocumentAsPdf();
		String textAsPdfString = null;
		try{
			PDFTextStripper textStripper = new PDFTextStripper();
			textAsPdfString = textStripper.getText(PDDocument.load(pdfData)).trim();
		}catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(this.textString, textAsPdfString);
		assertNotEquals(this.textString, "hi");
	}
	@Test
	public void pdfGetDocumentAsPdf() {
		DocumentImpl pdfDocument = new DocumentImpl(this.pdfUri, this.pdfString, this.pdfHashCode, this.pdfData);
		byte[] pdfData = pdfDocument.getDocumentAsPdf();
		String pdfAsPdfString = null;
		try{
			PDFTextStripper textStripper = new PDFTextStripper();
			pdfAsPdfString = textStripper.getText(PDDocument.load(pdfData)).trim();
		}catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(this.pdfString, pdfAsPdfString);
		assertNotEquals(this.pdfString, "hi");
	}

	@Test
	public void textGetDocumentTextHashCode() {
		DocumentImpl textDocument = new DocumentImpl(this.textUri, this.textString, this.textHashCode);
		assertEquals(this.textHashCode, textDocument.getDocumentTextHashCode());
		assertNotEquals(this.textHashCode, 25);
	}
	@Test
	public void pdfGetDocumentTextHashCode() {
		DocumentImpl pdfDocument = new DocumentImpl(this.pdfUri, this.pdfString, this.pdfHashCode, this.pdfData);
		assertEquals(this.pdfHashCode, pdfDocument.getDocumentTextHashCode());
		assertNotEquals(this.pdfHashCode, 25);
	}

	@Test
	public void textGetKey() {
		DocumentImpl textDocument = new DocumentImpl(this.textUri, this.textString, this.textHashCode);
		assertEquals(this.textUri, textDocument.getKey());
		URI fakeUri = null;
		try{
			fakeUri = new URI("thisIsNotTheURI");
		}catch(URISyntaxException e){
			e.printStackTrace();
		}
		assertNotEquals(this.textUri, fakeUri);
	}
	@Test
	public void pdfGetKey() {
		DocumentImpl pdfDocument = new DocumentImpl(this.pdfUri, this.pdfString, this.pdfHashCode, this.pdfData);
		assertEquals(this.pdfUri, pdfDocument.getKey());
		URI fakeUri = null;
		try{
			fakeUri = new URI("thisIsNotTheURI");
		}catch(URISyntaxException e){
			e.printStackTrace();
		}
		assertNotEquals(this.pdfUri, fakeUri);
	}
	@Test
	public void wordCount() throws URISyntaxException {
		DocumentImpl textDocument = new DocumentImpl(this.textUri, this.textString, this.textHashCode);
		assertEquals(textDocument.wordCount("thIs"), 3);
		assertEquals(textDocument.wordCount("line"), 2);
		assertEquals(textDocument.wordCount("joshy"), 0);
		DocumentImpl pdfDocument = new DocumentImpl(this.pdfUri, this.pdfString, this.pdfHashCode, this.pdfData);
		assertEquals(pdfDocument.wordCount("thIs"), 3);
		assertEquals(pdfDocument.wordCount("pdF"), 3);
		assertEquals(pdfDocument.wordCount("a"), 1);
		DocumentImpl numberDocument = new DocumentImpl(new URI("numberURI"), "3 3 9 4 3 2 0", "3 3 9 4 3 2 0".hashCode());
		assertEquals(numberDocument.wordCount("3"), 3);
		assertEquals(numberDocument.wordCount("0"), 1);
		assertEquals(numberDocument.wordCount("94"), 0);
	}
}