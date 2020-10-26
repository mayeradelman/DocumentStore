package edu.yu.cs.com1320.project.stage5.impl;
import edu.yu.cs.com1320.project.stage5.Document;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URI;

import static org.junit.Assert.*;

public class DocumentPersistenceManagerTest {
	String expectedText;
	URI uri;
	Document document;
	DocumentPersistenceManager dpm;
	@Before
	public void setUp() throws Exception{
		this.dpm = new DocumentPersistenceManager(null);
		this.expectedText = "this is the expected text";
		this.uri = new URI("http://www.mayer.adelman/documents/testDocument");
		this.document = new DocumentImpl(uri, this.expectedText, this.expectedText.hashCode());
	}
	@Before
	@Test
	public void serialize() throws IOException {
		this.dpm.serialize(this.uri, this.document);
	}
	@Test
	public void deserialize() throws IOException {
		Document document = this.dpm.deserialize(this.uri);
		assertEquals(this.document.getKey(), document.getKey());
		assertEquals(this.document.getDocumentAsTxt().hashCode(), document.getDocumentAsTxt().hashCode());
	}
}
