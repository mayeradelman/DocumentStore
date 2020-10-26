package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.stage3.Document;

import java.util.*;

/**
 * FOR STAGE 3
 * @param <Value>
 */
public class TrieImpl<Value> implements Trie<Value> {
	private TrieNode<Value> root; // root of trie

	public TrieImpl() {
		this.root = new TrieNode<>(Character.MIN_VALUE);
	}



	/**
	 * add the given value at the given key
	 * @param key
	 * @param val
	 */
	@Override
	public void put(String key, Value val){
		if(key == null){
			throw new IllegalArgumentException();
		}
		if(val == null){
			return;
		}
		TrieNode<Value> current = this.root;
		for(char character: key.toUpperCase().toCharArray()){
			if(current.hasThisChild((character))){
				current = current.getThisChild(character);
				continue;
			}
			TrieNode<Value> newNode = new TrieNode<>(character);
			current.addChild(newNode);
			current = newNode;
		}
		current.addValue(val);
	}

	/**
	 * get all exact matches for the given key, sorted in descending order.
	 * Search is CASE INSENSITIVE.
	 * @param key
	 * @param comparator
	 * @return a List of matching Values, in descending order
	 */
	@Override
	public List<Value> getAllSorted(String key, Comparator<Value> comparator) {
		if(key == null) {
			return new LinkedList<>();
		}
		TrieNode<Value> node = this.getNode(key);
		if(node == null){
			return new LinkedList<>();
		}
		List<Value> values = new LinkedList<>(node.getValuesAsSet());
		values.sort(comparator);
		return values;
	}

	/**
	 * get all matches which contain a String with the given prefix, sorted in descending order.
	 * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
	 * Search is CASE INSENSITIVE.
	 * @param prefix
	 * @param comparator
	 * @return a List of all matching Values containing the given prefix, in descending order
	 */
	@Override
	public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
		if(prefix == null) {
			return new LinkedList<>();
		}
		TrieNode<Value> node = this.getNode(prefix);
		if(node == null){
			return new LinkedList<>();
		}
		List<Value> values = new LinkedList<>(this.getAllValuesDescendingFromNode(node));
		values.sort(comparator);
		return values;
	}

	/**
	 * Delete the subtree rooted at the last character of the prefix.
	 * Search is CASE INSENSITIVE.
	 * @param prefix
	 * @return a Set of all Values that were deleted.
	 */
	@Override
	public Set<Value> deleteAllWithPrefix(String prefix) {
		if (prefix == null) {
			return new HashSet<>();
		}
		TrieNode<Value> node = this.getNode(prefix);
		if(node == null){
			return null;
		}
		Set<Value> deletedValues = this.getAllValuesDescendingFromNode(node);
		String parentKey = prefix.substring(0, prefix.length() - 1);
		Objects.requireNonNull(this.getNode(parentKey)).deleteThisChild(node);
		this.deleteExtras(parentKey);
		return deletedValues;
	}
	private Set<Value> getAllValuesDescendingFromNode(TrieNode<Value> node){
		Set<Value> values = node.getValuesAsSet();
		for(TrieNode<Value> child : node.getChildrenAsArray()) {
			values.addAll(this.getAllValuesDescendingFromNode(child));
		}
		return values;
	}

	/**
	 * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
	 * @param key
	 * @return a Set of all Values that were deleted.
	 */
	@Override
	public Set<Value> deleteAll(String key) {
		if (key == null) {
			return new HashSet<>();
		}
		TrieNode<Value> node = this.getNode(key);
		if(node == null){
			return new HashSet<>();
		}
		Set<Value> values = node.getValuesAsSet();
		node.deleteAllValues();
		this.deleteExtras(key);
		return values;
	}

	/**
	 * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
	 * @param key
	 * @param val
	 * @return the value which was deleted. If the key did not contain the given value, return null.
	 */
	@Override
	public Value delete(String key, Value val) {
		TrieNode<Value> node = this.getNode(key);
		if(node == null){
			return null;
		}
		if(!node.deleteValue(val)){
			return null;
		}
		this.deleteExtras(key);
		return val;
	}

	private void deleteExtras(String key) {
		TrieNode<Value> node = this.getNode(key);
		if(node == null){
			return;
		}
		if(!node.isRoot() && !node.hasValues() && !node.hasChildren()){
			String parentKey = key.substring(0, key.length() - 1);
			Objects.requireNonNull(this.getNode(parentKey)).deleteThisChild(node);
			this.deleteExtras(parentKey);
		}
	}

	private TrieNode<Value> getNode(String key){
		char[] characters = key.toUpperCase().toCharArray();
		TrieNode<Value> current = this.root;
		for(char character: characters) {
			if(!current.hasThisChild((character))){
				return null;
			}
			current = current.getThisChild(character);
		}
		return current;
	}

	static class TrieNode<Value> {
		private static final int alphaNumericUpperCaseCharacters = 36;
		private static final int asciiLetterStartIndex = 65;
		private static final int asciiNumberStartIndex = 48;

		final char character;
		Set<Value> values = new HashSet<>();
		TrieNode<Value>[] children;

		TrieNode(char character){
			this.character = character;
			this.children = new TrieNode[alphaNumericUpperCaseCharacters];
		}
		void addChild(TrieNode<Value> newChild){
			this.children[this.getIndex(newChild.getCharacter())] = newChild;
		}
		char getCharacter(){
			return this.character;
		}
		void addValue(Value value){
			this.values.add(value);
		}
		boolean hasValues(){
			return !this.values.isEmpty();
		}
		Set<Value> getValuesAsSet(){
			return this.values;
		}
		boolean hasThisChild(char character){
			return this.children[this.getIndex(character)] != null;
		}
		boolean hasChildren(){
			for(TrieNode<Value> child : this.children){
				if(child != null){
					return true;
				}
			}
			return false;
		}
		TrieNode<Value> getThisChild(char character){
			return this.children[this.getIndex(character)];
		}
		TrieNode<Value>[] getChildrenAsArray(){
			int numberOfChildren = 0;
			for(TrieNode<Value> child : this.children){
				if(child != null){
					numberOfChildren++;
				}
			}
			TrieNode<Value>[] existentChildren = new TrieNode[numberOfChildren];
			int existentChildrenIndex = 0;
			for(TrieNode<Value> child : this.children){
				if(child != null){
					existentChildren[existentChildrenIndex] = child;
					existentChildrenIndex++;
				}
			}
			return existentChildren;
		}
		void deleteThisChild(TrieNode<Value> child){
			this.children[this.getIndex(child.getCharacter())] = null;
		}
		boolean deleteValue(Value value){
			return this.values.remove(value);
		}
		void deleteAllValues(){
			this.values.clear();
		}
		boolean isRoot(){
			return this.character == Character.MIN_VALUE;
		}
		private int getIndex(char character){
			if(Character.isLetter(character)){
				return character - asciiLetterStartIndex;
			}else if(Character.isDigit(character)){
				return character - asciiNumberStartIndex;
			}else{
				throw new IllegalArgumentException();
			}
		}
	}
}
