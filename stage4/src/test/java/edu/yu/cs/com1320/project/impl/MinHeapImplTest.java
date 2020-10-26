package edu.yu.cs.com1320.project.impl;

import org.junit.Before;
import org.junit.Test;


import java.net.Inet4Address;

import static org.junit.Assert.*;

public class MinHeapImplTest {
	MinHeapImpl<Integer> minHeap = new MinHeapImpl<>();

	@Before
	public void setUp() throws Exception{
		this.minHeap.insert(1);
		this.minHeap.insert(2);
		this.minHeap.insert(5);
		this.minHeap.insert(4);
		this.minHeap.insert(3);
		this.minHeap.insert(10);
	}

	@Test
	public void getArrayIndex() {
		assertEquals(1,this.minHeap.getArrayIndex(1));
		assertEquals(6,this.minHeap.getArrayIndex(10));
	}

	@Test
	public void removeMin() {
		assertEquals(1, this.minHeap.removeMin().intValue());
		assertEquals(2, this.minHeap.removeMin().intValue());
		int expectedThree = this.minHeap.removeMin().intValue();
		assertNotEquals(5, expectedThree);
		assertEquals(3, expectedThree);
		assertEquals(4, this.minHeap.removeMin().intValue());
		assertEquals(5, this.minHeap.removeMin().intValue());
		this.minHeap.insert(7);
		assertEquals(7, this.minHeap.removeMin().intValue());
		assertEquals(10, this.minHeap.removeMin().intValue());
	}
}