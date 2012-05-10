/**
 * Greedy_Recovery - Technion, Israel Institute of Technology
 * 
 * Author: Assaf Israel, 2012
 * Created: 12/03/2012
 */
package il.ac.technion.knapsack.dp_knapsack.ep;

import il.ac.technion.knapsack.Item;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class EfficientPairs {
	private Collection<Pair> collection;

	@Inject
	public EfficientPairs(@Named("EP_Collection") Collection<Pair> _collection) {
		collection = _collection;
		collection.add(new Pair(0,0));
	}

	public void build(EfficientPairs eps, Item item, int capacity) {
		if (this != eps) {
			collection.clear();
			collection.addAll(eps.collection);
		}
		
		for (Pair pair : eps.collection) {
			if (pair.getSize() + item.size <= capacity) {
				Pair newPair = new Pair(pair,item);
				collection.add(newPair);
				removeDominated(collection,newPair);
			}
		}
	}

	/**
	 * Assuming the collection was dominated-free before the addition of <code>aPair</code>
	 * the method will remove all dominated pairs from the collection.
	 * @param collection The collection to be cleaned from dominated pairs.
	 * @param aPair The last pair that was added to the collection, which may cause it 
	 * to have dominated pairs.
	 */
	private static void removeDominated(Collection<Pair> collection, Pair aPair) {
		List<Pair> removeList = new LinkedList<Pair>();
		for (Pair pair : collection) {
			if (pair.equals(aPair)) continue;
			if (aPair.dominate(pair)) {//aPair is the dominator
				removeList.add(pair);
				continue;
			}
			if (pair.dominate(aPair)) { //aPair is dominated
				collection.remove(aPair);
				break; 
			}
		}
		for (Pair pair : removeList) {
			collection.remove(pair);
		}
	}
	
	public void swap(EfficientPairs eps) {
		Collection<Pair> tmpColl = eps.collection;
		eps.collection = this.collection;
		this.collection = tmpColl;
	}

	/**
	 * @return The maximum value pair.
	 */
	public Pair max() {
		return Collections.max(collection);
	}
}
