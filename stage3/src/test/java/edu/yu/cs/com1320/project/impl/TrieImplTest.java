package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.impl.DocumentImpl;
import org.junit.Before;
import org.junit.Test;

import javax.print.Doc;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class TrieImplTest {
	TrieImpl<Document> trie = new TrieImpl<>();
	Document hiDoc;
	Document hiHiDoc;
	Document hiHiHiDoc;
	Document hiHiHiHiDoc;

	@Before
	public void setUp() throws Exception {
		this.hiDoc = new DocumentImpl(new URI("hi"), "Hi", "Hi".hashCode());
		this.hiHiDoc = new DocumentImpl(new URI("hiHi"), "hi Hi", "hi Hi".hashCode());
		this.hiHiHiDoc = new DocumentImpl(new URI("hiHiHi"), "hi Hi Hi", "hi Hi Hi".hashCode());
		this.hiHiHiHiDoc = new DocumentImpl(new URI("hiHiHiHiBye"), "hi Hi Hi Hi bYe", "hi Hi Hi Hi bYe".hashCode());
		trie.put("HI", this.hiDoc);
		trie.put("HI", this.hiHiDoc);
		trie.put("HI", this.hiHiHiDoc);
		trie.put("HI", this.hiHiHiHiDoc);
		trie.put("bYe", this.hiHiHiHiDoc);
	}

	@Test
	public void put() {
	}

	@Test
	public void getAllSorted() {

	}

	@Test
	public void getAllWithPrefixSorted() {

	}

	@Test
	public void deleteAllWithPrefix() {

	}

	@Test
	public void deleteAll() {
	}

	@Test
	public void delete() {
	}
}