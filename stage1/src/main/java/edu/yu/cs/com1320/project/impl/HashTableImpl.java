package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;
/**
 * Instances of HashTable should be constructed with two type parameters, one for the type of the keys in the table and one for the type of the values
 * Students who are not familiar with Generics should see Chapter 20 in "Java How To Program" by Deitel
 * @param <Key>
 * @param <Value>
 */

public class HashTableImpl<Key, Value> implements HashTable<Key,Value> {
	private Entry[] entries = new Entry[5];

	/**
	 * @param k the key whose value should be returned
	 * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
	 */
	@Override

	public Value get(Key k) {
		Entry entry = this.getEntry(k);
		if(entry == null){
			return null;
		}
		return (Value) entry.getValue();
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
		if(this.getEntry(k) != null) {
			Entry entry = this.getEntry(k);
			Value oldValue = (Value) entry.getValue();
			entry.setValue(v);
			return oldValue;
		}
		Entry<Key, Value> newEntry = new Entry<>(k, v);
		int element = this.hashFunction(k.hashCode());
		Entry current = this.entries[element];
		if(current == null) {
			this.entries[element] = newEntry;
		}else {
			while(current.getNext() != null){
				current = current.getNext();
			}
			current.setNext(newEntry);
		}
		return null;
	}
	private Value deleteEntry(Key k) {
		int element = this.hashFunction(k.hashCode());
		Entry current = entries[element];
		if(current == null){
			return null;
		}
		if(current.getKey().equals(k)){
			Value previousValue = (Value) current.getValue();
			entries[element] = current.getNext();
			return previousValue;
		}
		Entry previous = current;
		while(current.getNext() != null){
			current = current.getNext();
			if(current.getKey().equals(k)){
				Value previousValue = (Value) current.getValue();
				previous.setNext(current.getNext());
				return previousValue;
			}
			previous = current;
		}
		return null;
	}
	private Entry getEntry(Key k) {
		int element = this.hashFunction(k.hashCode());
		Entry entry = entries[element];
		while (entry != null) {
			if (entry.getKey().equals(k)){
				break;
			}
			entry = entry.getNext();
		}
		return entry;
	}
	private int hashFunction(int hashcode) {
		return (hashcode & 0x7fffffff) % this.entries.length;
	}
	private class Entry<Key, Value>{
		final private Key k;
		private Value v;
		private Entry next;

		Entry(Key k, Value v) {
			this.k = k;
			this.v = v;
			this.next = null;
		}
		Key getKey() {
			return this.k;
		}
		void setValue(Value v) {
			this.v = v;
		}
		Value getValue() {
			return this.v;
		}
		void setNext(Entry next) {
			this.next = next;
		}
		Entry getNext() {
			return this.next;
		}
	}
}
