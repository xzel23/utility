package com.dua3.utility.lang;

public class RingBuffer<T> {

	private Object[] data;
	private int entries;
	private int start;
	
	public RingBuffer(int size) {
		data = new Object[size];
		start = 0;
		entries = 0;
	}
	
	public int size() {
		return entries;
	}
	
	public int capacity() {
		return data.length;
	}

	@SuppressWarnings("unchecked")
	public T get(int i) {
		checkIndex(i);
		return (T) data[index(i)];
	}
	
	private int index(int i) {
		return (start+i)%capacity();
	}

	private void checkIndex(int i) {
		if (i<0 || i>=size()) {
			throw new IndexOutOfBoundsException("size="+size()+", index="+i);
		}
	}
	
	public void add(T item) {
		if (entries<capacity()) {
			data[index(entries++)] = item;
		} else {
			start = (start+1)%capacity();
			data[index(entries-1)] = item;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(16*(1+size()));
		sb.append("[");
		String d = "";
		for (int i=0; i<size(); i++) {
			sb.append(d);
			sb.append(get(i));
			d = ", ";			
		}
		sb.append("]");
		return sb.toString();
	}
	
	public void setCapacity(int n) {
		if (n!=capacity()) {
			Object[] dataNew = new Object[n];
			for (int i=0; i<Math.min(size(), n); i++) {
				dataNew[i] = get(i);
			}
			start=0;
			entries = Math.min(entries, n);
		}
	}

	public void clear() {
		start = entries = 0;
	}
	
	public boolean isEmpty() {
		return entries==0;
	}
}
