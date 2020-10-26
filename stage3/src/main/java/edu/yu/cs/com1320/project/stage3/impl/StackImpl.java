package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.Stack;

/**
 * @param <T>
 */
public class StackImpl<T> implements Stack<T> {
	Node<T> top;
	int size;
	/**
	 * @param element object to add to the Stack
	 */
	public void push(T element){
		this.top = new Node<>(element, this.top);
		this.size++;
	}

	/**
	 * removes and returns element at the top of the stack
	 * @return element at the top of the stack, null if the stack is empty
	 */
	public T pop(){
		if(this.top == null) {
			return null;
		}
		Node<T> previousTop = this.top;
		this.top = previousTop.beneath;
		this.size--;
		return previousTop.data;
	}

	/**
	 *
	 * @return the element at the top of the stack without removing it
	 */
	public T peek(){
		if(this.top == null){
			return null;
		}
		return this.top.data;
	}

	/**
	 *
	 * @return how many elements are currently in the stack
	 */
	public int size(){
		if(this.top == null){
			return 0;
		}
		Node<T> current = this.top;
		int size = 1;
		while(current.hasBeneath()){
			current = current.beneath;
		}
		return size;
	}
	private class Node<D>{
		D data;
		Node<D> beneath;

		Node(D data, Node<D> beneath) {
			this.data = data;
			this.beneath = beneath;
		}
		boolean hasBeneath(){
			return this.beneath != null;
		}
	}
}
