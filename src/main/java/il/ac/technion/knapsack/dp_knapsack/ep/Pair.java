/**
 * Greedy_Recovery - Technion, Israel Institute of Technology
 * 
 * Author: Assaf Israel, 2012
 * Created: 12/03/2012
 */
package il.ac.technion.knapsack.dp_knapsack.ep;

import il.ac.technion.knapsack.Item;

import java.util.HashSet;
import java.util.Set;

public class Pair implements Comparable<Pair> {
	private int size;
	private double value;
	private Set<Item> items = new HashSet<Item>();
	
	public Pair(int size, double value) {
		this.size = size;
		this.value = value;
	}
	
	public Pair(Pair aPair, Item item) {
		this(aPair.size + item.size, aPair.value + item.value);
		items.addAll(aPair.items);
		items.add(item);
	}
	
	public Pair(Item item) {
		this(item.size, item.value);
		items.add(item);
	}
	
	/**
	 * A pair dominates over <code>aPair</code> if it has at most aPair's size 
	 * and at least its value. Note that the domination relation does not induce order,  
	 * however it is reflexive, transitive and antisymmetric. 
	 * @param aPair
	 * @return
	 */
	public boolean dominate(Pair aPair) {
		return this.size <= aPair.size && this.value >= aPair.value;
	}

	/**
	 * Compares pairs according to their value.
	 */
	@Override
	public int compareTo(Pair aPair) {
		if (this.value - aPair.value < 0) {
			return -1;
		}
		if (this.value - aPair.value > 0) {
			return 1;
		}
		return 0;
	}
	
	@Override
	public String toString() {
		return "Size: " + size + ", Value: " + value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		Pair aPair = (Pair)obj;
		return this.size == aPair.size && this.value == aPair.value;
	}
	
	public Set<Item> items() {
		return new HashSet<Item>(items);
	}

	public int getSize() {
		return size;
	}

	public double getValue() {
		return value;
	}

	public Set<Item> getItems() {
		return items;
	}
	
	public boolean add(Item item) {
		if (items.contains(item)) 
			return false;
		items.add(item);
		size += item.size;
		value += item.value;
		return true;
	}
}
