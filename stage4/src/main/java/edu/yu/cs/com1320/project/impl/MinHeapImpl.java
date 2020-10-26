package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable> extends MinHeap<E> {
	public MinHeapImpl(){
		this.elements = (E[]) new Comparable[5];
		this.count = 0;
		this.elementsToArrayIndex = new HashMap<>();
	}
	@Override
	public void reHeapify(E element) {
		Integer index = this.elementsToArrayIndex.get(element);
		if(index == null){
			throw new NoSuchElementException();
		}
		if(count < 2){
			return;
		}
		this.upHeap(index);
		this.downHeap(index);
	}

	@Override
	protected int getArrayIndex(E element) {
		Integer index = this.elementsToArrayIndex.get(element);
		if(index == null){
			throw new NoSuchElementException();
		}
		return index;
	}

	@Override
	protected void doubleArraySize() {
		this.elements = Arrays.copyOf(this.elements, this.elements.length * 2);
	}
	@Override
	protected void swap(int i, int j) {
		E temp = this.elements[i];
		this.elements[i] = this.elements[j];
		this.elements[j] = temp;
		this.elementsToArrayIndex.put(this.elements[i], i);
		this.elementsToArrayIndex.put(this.elements[j], j);
	}
	@Override
	public void insert(E x) {
		// double size of array if necessary
		if (this.count >= this.elements.length - 1)
		{
			this.doubleArraySize();
		}
		//add x to the bottom of the heap
		this.elements[++this.count] = x;
		this.elementsToArrayIndex.put(x, this.count);
		//percolate it up to maintain heap order property
		this.upHeap(this.count);
	}

	@Override
	public E removeMin() {
		if (isEmpty())
		{
			throw new NoSuchElementException("Heap is empty");
		}
		E min = this.elements[1];
		//swap root with last, decrement count
		this.swap(1, this.count--);
		//move new root down as needed
		this.downHeap(1);
		this.elements[this.count + 1] = null; //null it to prepare for GC
		this.elementsToArrayIndex.remove(min);
		return min;
	}
}
