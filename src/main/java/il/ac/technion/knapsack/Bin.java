/**
 * Greedy_Recovery - Software Design, 236700 - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.knapsack;


import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import com.google.java.contract.Requires;

public class Bin {

	public final int capacity;
	private int remainingCapacity;
	public final int id;
	
	private Collection<Item> assignedItems = new LinkedList<Item>();
	
	@Override
	public String toString() {
		StringBuilder desc =  new StringBuilder();
		desc.append("Bin #" + id + " [" + capacity + "]: ");
		for (Item item : assignedItems) {
			desc.append(item.toString());
		}
		return desc.toString();
	}
	
	public Bin(int _id, int _capacity) {
		this.capacity = _capacity;
		this.remainingCapacity = _capacity;
		this.id = _id;
	}

	@Requires("remainingCapacity >= item.size")
	public boolean assign(Item item) {
		if (!canHold(item)) {
			return false;
		}
		remainingCapacity -= item.size;
		return assignedItems.add(item);
	}
	
	public boolean unassign(Item item) {
		if (assignedItems.remove(item)) {
			remainingCapacity += item.size;
			return true;
		}
		return false;
	}
	
	public Collection<Item> assignedItems() {
		return new ArrayList<Item>(assignedItems);
	}
	
	public int remainingCapacity() {
		return remainingCapacity;
	}
	
	public boolean canHold(Item item) {
		return remainingCapacity >= item.size;
	}
}
