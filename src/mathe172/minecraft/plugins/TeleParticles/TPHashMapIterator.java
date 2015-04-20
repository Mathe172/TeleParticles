package mathe172.minecraft.plugins.TeleParticles;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class TPHashMapIterator<M extends LinkedHashMap<K, ArrayList<I>>, K, I> implements ListIterator<I> {

	private M toIterate;
	private ArrayList<I> allItems;
	private int index = 0;
	private int lastIndex = 0;
	
	public TPHashMapIterator(M toIterate) {
		this.toIterate = toIterate;
		this.allItems = new ArrayList<I>();
		
		for (K key : toIterate.keySet()) {
			for (I item : toIterate.get(key)) {
				this.allItems.add(item);
			}
		}
	}
	
	@Override
	public void add(I e) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();		
	}

	@Override
	public boolean hasNext() {
		return this.index < this.allItems.size();
	}

	@Override
	public boolean hasPrevious() {
		return this.index > 0;
	}

	@Override
	public I next() throws NoSuchElementException {
		this.lastIndex = this.index;
		if (this.index == this.allItems.size()) throw new NoSuchElementException();
		this.index++;
		return this.allItems.get(this.index - 1);
	}

	@Override
	public int nextIndex() {
		return (this.index < this.allItems.size()) ? this.index + 1 : this.allItems.size();
	}

	@Override
	public I previous() throws NoSuchElementException {
		if (this.index == 0) throw new NoSuchElementException();
		this.index--;
		this.lastIndex = this.index;
		return this.allItems.get(this.index);
	}

	@Override
	public int previousIndex() {
		return (this.index > 0) ? this.index - 1 : 0;
	}

	@Override
	public void remove() throws IllegalStateException {
		if (this.lastIndex == -1) throw new IllegalStateException();
		
		int counter = 0;
		
		for (K key : this.toIterate.keySet()) {
			ListIterator<I> iterator = this.toIterate.get(key).listIterator();
			while (iterator.hasNext()) {
				iterator.next();
				
				if (counter == this.lastIndex) {
					//ArrayList<I> newList = this.toIterate.get(key);
					iterator.remove();
					//this.toIterate.put(key, newList);
				}
				counter++;
			}
		}
		
		this.allItems.remove(this.lastIndex);
		this.lastIndex = -1;
	}

	@Override
	public void set(I e) throws IllegalStateException {
		if (this.lastIndex == -1) throw new IllegalStateException();
		
		int counter = 0;
		
		for (K key : this.toIterate.keySet()) {
			ListIterator<I> iterator = this.toIterate.get(key).listIterator();
			while (iterator.hasNext()) {
				iterator.next();
				
				if (counter == this.lastIndex) {
					//ArrayList<I> newList = this.toIterate.get(key);
					iterator.set(e);
					//this.toIterate.put(key, newList);
				}
				counter++;
			}
		}
		
		this.allItems.set(this.lastIndex, e);
		this.lastIndex = -1;
	}

	public K previousKey() throws NoSuchElementException {
		if (!this.hasPrevious()) throw new NoSuchElementException();
		
		int counter = 0;
		
		for (K key : this.toIterate.keySet()) {			
			counter += this.toIterate.get(key).size();
			
			if (this.index - 1 < counter)
				return key;
		}
		throw new NoSuchElementException();
	}
	
	public K nextKey() throws NoSuchElementException {
		if (!this.hasNext()) throw new NoSuchElementException();
		
		int counter = 0;
		
		for (K key : this.toIterate.keySet()) {			
			counter += this.toIterate.get(key).size();
			
			if (this.index + 1 < counter)
				return key;
		}
		throw new NoSuchElementException();
	}
	
	public int totalLength() {
		return this.allItems.size();
	}
}
