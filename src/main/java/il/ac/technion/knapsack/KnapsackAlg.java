/**
 * Greedy_Recovery - Technion, Israel Institute of Technology
 * 
 * Author: Assaf Israel, 2012
 * Created: 12/03/2012
 */
package il.ac.technion.knapsack;

import il.ac.technion.knapsack.dp_knapsack.ep.Pair;

import java.util.Collection;

public interface KnapsackAlg {
	public Pair solve(Collection<Item> items, int capacity);
}
