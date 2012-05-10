/**
 * Greedy_Recovery - Technion, Israel Institute of Technology
 * 
 * Author: Assaf Israel, 2012
 * Created: 12/03/2012
 */
package il.ac.technion.knapsack.dp_knapsack.ep;

import il.ac.technion.knapsack.Item;
import il.ac.technion.knapsack.KnapsackAlg;

import java.util.Collection;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class DP_EP_Knapsack implements KnapsackAlg {
	
	Injector inj;
	
	@Inject
	public DP_EP_Knapsack(Injector injector) {
		this.inj = injector;
	}
	
	@Override
	public Pair solve(Collection<Item> items, int capacity) {
		EfficientPairs ep1 = inj.getInstance(EfficientPairs.class);
		EfficientPairs ep2 = inj.getInstance(EfficientPairs.class);
		
		for (Item item : items) {
			ep2.build(ep1, item, capacity);
			ep2.swap(ep1);
		}
		
		return ep1.max();
	}
}
