package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;
/**
 * Instances of HashTable should be constructed with two type parameters, one for the type of the keys in the table and one for the type of the values
 * Students who are not familiar with Generics should see Chapter 20 in "Java How To Program" by Deitel
 * @param <Key>
 * @param <Value>
 */

public class HashTableImpl<Key, Value> implements HashTable<Key,Value> {
	private Entry<Key,Value>[] entries;
	final private float loadFactor;
	private int totalEntries;

	public HashTableImpl(){
		this.entries = new Entry[5];
		this.loadFactor = 3;
	}

	/**
	 * @param k the key whose value should be returned
	 * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
	 */
	@Override

	public Value get(Key k) {
		Entry<Key, Value> entry = this.getEntry(k);
		if(entry == null){
			return null;
		}
		return entry.getValue();
	}

	/**
	 * @param k the key at which to store the value
	 * @param v the value to store
	 * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
	 */
	@Override
	public Value put(Key k, Value v) {
		if(v == null){
			return this.deleteEntry(k);
		}
		if(((float) this.totalEntries / this.entries.length) >= (this.loadFactor)){
			this.doubleSize();
		}
		if(this.getEntry(k) != null) {
			Entry<Key,Value> entry = this.getEntry(k);
			Value oldValue = entry.getValue();
			entry.setValue(v);
			return oldValue;
		}
		Entry<Key, Value> newEntry = new Entry<>(k, v);
		this.putEntry(newEntry);
		this.totalEntries++;
		return null;
	}
	int hashFunction(int hashcode) {
		return (hashcode & 0x7fffffff) % this.entries.length;
	}
	private Value deleteEntry(Key k) {
		int element = this.hashFunction(k.hashCode());
		Entry<Key,Value> current = entries[element];
		if(current == null){
			return null;
		}
		if(current.getKey().equals(k)){
			Value previousValue = current.getValue();
			entries[element] = current.getNext();
			return previousValue;
		}
		Entry<Key,Value> previous = current;
		while(current.getNext() != null){
			current = current.getNext();
			if(current.getKey().equals(k)){
				Value previousValue = current.getValue();
				previous.setNext(current.getNext());
				return previousValue;
			}
			previous = current;
		}
		return null;
	}
	private Entry<Key,Value> getEntry(Key k) {
		int element = this.hashFunction(k.hashCode());
		Entry<Key,Value> entry = entries[element];
		while (entry != null) {
			if (entry.getKey().equals(k)){
				break;
			}
			entry = entry.getNext();
		}
		return entry;
	}
	private void doubleSize(){
		Entry<Key,Value>[] previousEntries = new Entry[this.totalEntries];
		int n = 0;
		for(int i = 0; i < this.entries.length; i++) {
			if(this.entries[i] == null){
				continue;
			}
			Entry<Key,Value>[] entryChain = this.entries[i].getChain();
			for(Entry<Key,Value> entry: entryChain){
				previousEntries[n] = entry;
				n++;
			}
		}
		this.entries = new Entry[this.entries.length * 2];
		for(Entry<Key,Value> entry : previousEntries){
			entry.setNext(null);
			this.putEntry(entry);
		}
	}

	private void putEntry(Entry<Key,Value> entry){
		int element = this.hashFunction(entry.getKey().hashCode());
		Entry<Key,Value> current = this.entries[element];
		if(current == null) {
			this.entries[element] = entry;
		}else{
			while(current.getNext() != null){
				current = current.getNext();
			}
			current.setNext(entry);
		}
	}

	private class Entry<K, V>{
		final private K k;
		private V v;
		private Entry<K,V> next;

		Entry(K k, V v) {
			this.k = k;
			this.v = v;
			this.next = null;
		}
		K getKey() {
			return this.k;
		}
		void setValue(V v) {
			this.v = v;
		}
		V getValue() {
			return this.v;
		}
		void setNext(Entry<K,V> next) {
			this.next = next;
		}
		Entry<K, V> getNext() {
			return this.next;
		}
		boolean hasNext(){
			return this.getNext() != null;
		}
		Entry<K,V>[] getChain(){
			Entry<K,V> entry = this;
			int size = 1;
			while(entry.hasNext()) {
				entry = entry.getNext();
				size++;
			}
			Entry<K,V>[] chain = new Entry[size];
			entry = this;
			for(int i = 0; i < size; i++){
				chain[i] = entry;
				entry = entry.getNext();
			}
			return chain;
		}
	}
}
