/**
 * Greedy_Recovery - Technion, Israel Institute of Technology
 * 
 * Author: Assaf Israel, 2012
 * Created: 12/03/2012
 */
package il.ac.technion.knapsack;


public class AssignedItem extends Item {

	public final Bin assignedToBin;

	@Override
	public String toString() {
		return super.toString() + " assigned to " + assignedToBin.id;
	}
	
	public AssignedItem(int id, int size, double value, Bin bin) {
		super(id, size, value);
		this.assignedToBin = bin;
	}

	public AssignedItem(Item item, Bin bin) {
		this(item.id, item.size, item.value, bin);
	}

	@Override
	public boolean equals(Object obj) {
		if (!obj.getClass().equals(AssignedItem.class)) {
			return false;
		}
		return super.equals(obj)
				&& ((AssignedItem) obj).assignedToBin.equals(assignedToBin);
	}
}
