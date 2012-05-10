/**
 * Greedy_Recovery - Technion, Israel Institute of Technology
 * 
 * Author: Assaf Israel, 2012
 * Created: 12/03/2012
 */
package il.ac.technion.knapsack.guice;

import il.ac.technion.knapsack.dp_knapsack.ep.Pair;

import java.util.Collection;
import java.util.LinkedList;

import com.google.inject.Provider;

public class EfficientPairsCollectionProvider implements Provider<Collection<Pair>> {

	@Override
	public Collection<Pair> get() {
		return new LinkedList<Pair>();
	}
}
