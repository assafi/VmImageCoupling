/**
 * Greedy_Recovery - Technion, Israel Institute of Technology
 * 
 * Author: Assaf Israel, 2012
 * Created: 11/04/2012
 */
package il.ac.technion.knapsack.dp_knapsack.matrix;

import il.ac.technion.knapsack.Item;
import il.ac.technion.knapsack.KnapsackAlg;
import il.ac.technion.knapsack.dp_knapsack.ep.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DP_Matrix_Knapsack implements KnapsackAlg {

	@Override
	public Pair solve(Collection<Item> itemsCollection, int capacity) {
		List<Item> items = new ArrayList<Item>(itemsCollection);
		
		// opt[n][w] = max profit of packing items 1..n with weight limit w
		// sol[n][w] = does opt solution to pack items 1..n with weight limit w
		// include item n?
		final int N = items.size();
		final int W = capacity;
		
		double[][] opt = new double[N + 1][W + 1];
		boolean[][] sol = new boolean[N + 1][W + 1];

		for (int n = 1; n <= N; n++) {
			for (int w = 1; w <= W; w++) {

				// don't take item n
				double option1 = opt[n - 1][w];

				// take item n
				double option2 = Double.NEGATIVE_INFINITY;
				if (items.get(n-1).size <= w)
					option2 = items.get(n-1).value + opt[n - 1][w - items.get(n-1).size];

				// select better of two options
				opt[n][w] = Math.max(option1, option2);
				sol[n][w] = (option2 > option1);
			}
		}

		// determine which items to take
		Pair $ = new Pair(0,0.0);
		for (int n = N, w = W; n > 0; n--) {
			if (sol[n][w]) {
				w = w - items.get(n-1).size;
				$.add(items.get(n-1));
			} 
		}

		return $;
	}

}
