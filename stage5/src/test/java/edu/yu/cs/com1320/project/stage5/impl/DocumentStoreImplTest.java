package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;

import org.junit.Before;
import org.junit.Test;


import java.awt.*;
import java.io.*;
import java.net.URI;

import static org.junit.Assert.*;

public class DocumentStoreImplTest {
	File baseDir = new File("C:\\Users\\mayer\\Google Drive\\AdelmanMark\\DataStructures\\project\\stage5");
	DocumentStore store;
	String string1;
	InputStream stream1;
	String string2;
	InputStream stream2;
	URI uri1And2;
	String string3;
	InputStream stream3;
	URI uri3;


	@Before
	public void setUp() throws Exception {
		this.store = new DocumentStoreImpl();
		this.string1 = "text1";
		this.stream1 = new ByteArrayInputStream(string1.getBytes());
		this.string2 = "text2";
		this.stream2 = new ByteArrayInputStream(string2.getBytes());
		this.uri1And2 = new URI("http://URI1And2.com");
		this.string3 = "text3";
		this.stream3 = new ByteArrayInputStream(this.string3.getBytes());
		this.uri3  = new URI("http://URI3.com");
	}
	@Test
	public void putDocument() throws Exception{
		this.store.putDocument(this.stream1, this.uri1And2, DocumentStore.DocumentFormat.TXT);
		assertEquals(this.string1, this.store.getDocumentAsTxt(this.uri1And2));
	}

	@Test
	public void putDocumentOverride() throws Exception{
		this.store.putDocument(this.stream1, this.uri1And2, DocumentStore.DocumentFormat.TXT);
		assertEquals(this.string1, this.store.getDocumentAsTxt(this.uri1And2));
		this.store.putDocument(this.stream2, this.uri1And2, DocumentStore.DocumentFormat.TXT);
		assertEquals(this.string2, this.store.getDocumentAsTxt(this.uri1And2));
	}
	@Test
	public void putDocumentNullInput() throws Exception{
		this.store.putDocument(this.stream1, this.uri1And2, DocumentStore.DocumentFormat.TXT);
		assertEquals(this.string1, this.store.getDocumentAsTxt(this.uri1And2));
		this.store.putDocument(null, this.uri1And2, DocumentStore.DocumentFormat.TXT);
		assertEquals(null, this.store.getDocumentAsTxt(this.uri1And2));
	}

	@Test
	public void deleteDocument() throws Exception{
		this.store.putDocument(this.stream1, this.uri1And2, DocumentStore.DocumentFormat.TXT);
		assertEquals(this.string1, this.store.getDocumentAsTxt(this.uri1And2));
		this.store.deleteDocument(this.uri1And2);
		assertEquals(null, this.store.getDocumentAsTxt(this.uri1And2));
	}
	@Test
	public void undo() throws Exception{
		this.store.putDocument(this.stream1, this.uri1And2, DocumentStore.DocumentFormat.TXT);
		assertEquals(this.string1, this.store.getDocumentAsTxt(this.uri1And2));
		this.store.putDocument(this.stream3, this.uri3, DocumentStore.DocumentFormat.TXT);
		assertEquals(this.string3, this.store.getDocumentAsTxt(this.uri3));
		this.store.putDocument(this.stream2, this.uri1And2, DocumentStore.DocumentFormat.TXT);
		assertEquals(this.string2, this.store.getDocumentAsTxt(this.uri1And2));
		this.store.undo();
		assertEquals(this.string3, this.store.getDocumentAsTxt(this.uri3));
		assertEquals(this.string1, this.store.getDocumentAsTxt(this.uri1And2));
	}
	@Test
	public void undoWithURI() throws Exception{
		this.store.putDocument(this.stream1, this.uri1And2, DocumentStore.DocumentFormat.TXT);
		assertEquals(this.string1, this.store.getDocumentAsTxt(this.uri1And2));
		this.store.putDocument(this.stream3, this.uri3, DocumentStore.DocumentFormat.TXT);
		assertEquals(this.string3, this.store.getDocumentAsTxt(this.uri3));
		this.store.putDocument(this.stream2, this.uri1And2, DocumentStore.DocumentFormat.TXT);
		assertEquals(this.string2, this.store.getDocumentAsTxt(this.uri1And2));
		this.store.undo(this.uri3);
		assertEquals(null, this.store.getDocumentAsTxt(this.uri3));
		assertEquals(this.string2, this.store.getDocumentAsTxt(this.uri1And2));
	}
	@Test
	public void setMaxDocumentCount() throws Exception{
		this.store.putDocument(this.stream1, this.uri1And2, DocumentStore.DocumentFormat.TXT);
		assertEquals(this.string1, this.store.getDocumentAsTxt(this.uri1And2));
		this.store.putDocument(this.stream3, this.uri3, DocumentStore.DocumentFormat.TXT);
		assertEquals(this.string3, this.store.getDocumentAsTxt(this.uri3));
		this.store.putDocument(this.stream2, this.uri1And2, DocumentStore.DocumentFormat.TXT);
		assertEquals(this.string2, this.store.getDocumentAsTxt(this.uri1And2));
		this.store.setMaxDocumentCount(1);
		String jsonFilePath1And2 = this.baseDir + this.uri1And2.toString().replace("/", File.separator).replaceFirst(this.uri1And2.getScheme() + ":" + File.separator + File.separator, "") + ".json";
		String jsonFilePath3 = this.baseDir + this.uri3.toString().replace("/", File.separator).replaceFirst(this.uri3.getScheme() + ":" + File.separator + File.separator, "") + ".json";
		File jsonFile1And2 = new File(jsonFilePath1And2);
		File jsonFile3 = new File(jsonFilePath3);
		assertTrue(!jsonFile1And2.exists());
		assertTrue(jsonFile3.exists());
		this.store.getDocumentAsTxt(this.uri3);
		assertTrue(jsonFile1And2.exists());
		assertTrue(!jsonFile3.exists());
	}
}