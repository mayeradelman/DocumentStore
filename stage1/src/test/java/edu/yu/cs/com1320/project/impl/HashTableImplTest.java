package edu.yu.cs.com1320.project.impl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class HashTableImplTest {
	HashTableImpl<String,String> hashTable;
	@Before
	public void setUp(){
		this.hashTable = new HashTableImpl<>();
		hashTable.put("Mayer", "Adelman");
		hashTable.put("Josh","Beer");
		hashTable.put("Roni","Kupchik");
		hashTable.put("Zack","Steiner");
		hashTable.put("Yitzi","Weiss");
		hashTable.put("Aron","Isaacs");
		hashTable.put("Doni","Schanzer");
	}
	@Test
	public void getReturnValueNormal() {
		assertEquals(this.hashTable.get("Mayer"), "Adelman");
		assertEquals(this.hashTable.get("Josh"),"Beer");
		assertEquals(this.hashTable.get("Roni"),"Kupchik");
		assertEquals(this.hashTable.get("Zack"),"Steiner");
		assertEquals(this.hashTable.get("Yitzi"),"Weiss");
		assertEquals(this.hashTable.get("Aron"),"Isaacs");
		assertEquals(this.hashTable.get("Doni"),"Schanzer");
		hashTable.put("Josh","Wine");
		assertEquals(this.hashTable.get("Josh"),"Wine");
	}
	@Test
	public void getReturnValueNull() {
		assertEquals(this.hashTable.get("Tani"),null);
	}
	@Test
	public void putReturnValue() {
		assertEquals(this.hashTable.put("Josh","Wine"), "Beer");
		assertEquals(this.hashTable.put("Mayer", "Adelman"), "Adelman");
	}
	@Test
	public void putReturnValueNull(){
		assertEquals(this.hashTable.put("Ezra","Wildes"), null);
	}
	@Test
	public void hashFunction() {
		assertEquals(this.hashTable.hashFunction(6), 1);
		assertEquals(this.hashTable.hashFunction(9), 4);
		assertEquals(this.hashTable.hashFunction(10), 0);
	}
}