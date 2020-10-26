//package edu.yu.cs.com1320.project.impl;
//
//import edu.yu.cs.com1320.project.stage4.Document;
//import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;
//import org.junit.Before;
//import org.junit.Test;
//
//import javax.print.Doc;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.InputStream;
//import java.net.URI;
//import java.util.Comparator;
//import java.util.LinkedList;
//import java.util.List;
//
//import static org.junit.Assert.*;
//
//public class TrieImplTest {
//	TrieImpl<Document> trie = new TrieImpl<>();
//	Document hiDoc;
//	Document hiHiDoc;
//	Document hiHiHiDoc;
//	Document hiHiHiHiDoc;
//
//	@Before
//	public void setUp() throws Exception {
//		this.hiDoc = new DocumentImpl(new URI("hi"), "Hi", "Hi".hashCode());
//		this.hiHiDoc = new DocumentImpl(new URI("hiHi"), "hi Hi", "hi Hi".hashCode());
//		this.hiHiHiDoc = new DocumentImpl(new URI("hiHiHi"), "hi Hi Hi", "hi Hi Hi".hashCode());
//		this.hiHiHiHiDoc = new DocumentImpl(new URI("hiHiHiHiBye"), "hi Hi Hi Hi bYe", "hi Hi Hi Hi bYe".hashCode());
//		trie.put("HI", this.hiDoc);
//		trie.put("HI", this.hiHiDoc);
//		trie.put("HI", this.hiHiHiDoc);
//		trie.put("HI", this.hiHiHiHiDoc);
//		trie.put("bYe", this.hiHiHiHiDoc);
//	}
//
//	@Test
//	public void put() {
//	}
//
//	@Test
//	public void getAllSorted() {
//		List<Document> hiDocs = this.trie.getAllSorted("hI", this.generateKeywordComparator("hI"));
//		assertEquals(hiDocs.get(0), this.hiHiHiHiDoc);
//		assertEquals(hiDocs.get(1), this.hiHiHiDoc);
//		assertEquals(hiDocs.get(2), this.hiHiDoc);
//		assertEquals(hiDocs.get(3), this.hiDoc);
//		List<Document> byeDocs = this.trie.getAllSorted("BYE", this.generateKeywordComparator("BYE"));
//		assertEquals(byeDocs.get(0), this.hiHiHiHiDoc);
//	}
//
//	@Test
//	public void getAllWithPrefixSorted() {
//		List<Document> hDocs = this.trie.getAllWithPrefixSorted("h", this.generateKeywordPrefixComparator("h"));
//		assertEquals(hDocs.get(0), this.hiHiHiHiDoc);
//		assertEquals(hDocs.get(1), this.hiHiHiDoc);
//		assertEquals(hDocs.get(2), this.hiHiDoc);
//		assertEquals(hDocs.get(3), this.hiDoc);
//	}
//
//	@Test
//	public void deleteAllWithPrefix() {
//		this.trie.deleteAllWithPrefix("h");
//		assertEquals(new LinkedList<>(), this.trie.getAllWithPrefixSorted("h", this.generateKeywordPrefixComparator("h")));
//		List<Document> byPrefixDocs = this.trie.getAllWithPrefixSorted("by", this.generateKeywordPrefixComparator("h"));
//		assertEquals(this.hiHiHiHiDoc, byPrefixDocs.get(0));
//	}
//
//	@Test
//	public void deleteAll() {
//	}
//
//	@Test
//	public void delete() {
//	}
//	private Comparator<Document> generateKeywordComparator(String keyword){
//		return (v1, v2) -> (v2.wordCount(keyword) - v1.wordCount(keyword));
//	}
//	private Comparator<Document> generateKeywordPrefixComparator(String keywordPrefix){
//		return (v1, v2) -> {
//			int v1PrefixCount = 0;
//			for (String word : ((DocumentImpl) v1).getWords()) {
//				if (word.startsWith(keywordPrefix)) {
//					v1PrefixCount++;
//				}
//			}
//			int v2PrefixCount = 0;
//			for (String word : ((DocumentImpl) v2).getWords()) {
//				if (word.startsWith(keywordPrefix)) {
//					v2PrefixCount++;
//				}
//			}
//			return v2PrefixCount - v1PrefixCount;
//		};
//	}
//}