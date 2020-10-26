package edu.yu.cs.com1320.project.impl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StackImplTest {
	StackImpl<Integer> stack;
	@Before
	public void setUp() {
		this.stack = new StackImpl<>();
		this.stack.push(1);
		this.stack.push(2);
		this.stack.push(3);
		this.stack.push(4);
		this.stack.push(5);
		this.stack.push(6);
		this.stack.push(7);
		this.stack.push(8);
		this.stack.push(9);
		this.stack.push(10);
	}

	@Test
	public void pop() {
		assertEquals(this.stack.pop().intValue(), 10);
		assertEquals(this.stack.pop().intValue(), 9);
		assertEquals(this.stack.pop().intValue(), 8);
		assertEquals(this.stack.pop().intValue(), 7);
		assertEquals(this.stack.pop().intValue(), 6);
		assertEquals(this.stack.pop().intValue(), 5);
		assertEquals(this.stack.pop().intValue(), 4);
		assertEquals(this.stack.pop().intValue(), 3);
		assertEquals(this.stack.pop().intValue(), 2);
		assertEquals(this.stack.pop().intValue(), 1);
		assertNull(this.stack.pop());
	}

	@Test
	public void peek() {
		assertEquals(this.stack.peek().intValue(), 10);
		assertEquals(this.stack.peek().intValue(), 10);
	}

	@Test
	public void size() {
		assertEquals(this.stack.size, 10);
		this.stack.pop();
		assertEquals(this.stack.size, 9);
	}
}