package com.dua3.utility.lang;

import org.junit.Assert;
import org.junit.Test;

public class RingBufferTest {

	private final int CAPACITY = 10;
	private RingBuffer<Object> buffer = new RingBuffer<>(CAPACITY);
	 
	@Test
	public void testCapacity() {
		Assert.assertEquals(CAPACITY, buffer.capacity());
		for (int i=0;i<2*CAPACITY; i++) {
			buffer.add("test "+i);
			Assert.assertEquals(CAPACITY, buffer.capacity());
		}
	}
	
	@Test
	public void testSize() {
		for (int i=0;i<2*CAPACITY; i++) {
			Assert.assertEquals(Math.min(CAPACITY,i), buffer.size());
			buffer.add("test "+i);
		}
	}
		
	@Test
	public void testAddAndGet() {
		for (int i=0;i<2*CAPACITY; i++) {
			Assert.assertEquals(Math.min(CAPACITY,i), buffer.size());
			buffer.add("test "+i);
			// test buffer content
			int last = i;
			int first = Math.max(0,  last-CAPACITY+1);
			for (int j=first; j<=last; j++) {				
				Assert.assertEquals("test "+j, buffer.get(j-first));
			}
		}
	}
	
}
