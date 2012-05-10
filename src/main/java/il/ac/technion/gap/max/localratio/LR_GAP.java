/**
 * Greedy_Recovery - Technion, Israel Institute of Technology
 * 
 * Author: Assaf Israel, 2012
 * Created: 12/03/2012
 */
package il.ac.technion.gap.max.localratio;

import il.ac.technion.data_extraction.DataTranslator;
import il.ac.technion.gap.GAP_Alg;
import il.ac.technion.knapsack.Bin;
import il.ac.technion.knapsack.Item;
import il.ac.technion.knapsack.KnapsackAlg;
import il.ac.technion.knapsack.dp_knapsack.ep.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.inject.Inject;
import com.google.java.contract.Requires;

public class LR_GAP extends GAP_Alg {

	/**
	 * 
	 */
	private static final int UNASSIGNED = -1;
	private KnapsackAlg knapsack;

	@Inject
	public LR_GAP(KnapsackAlg knapsackAlg, DataTranslator dt) {
		super(dt);
		this.knapsack = knapsackAlg;
	}

	@Override
	@Requires({ "binsCapacities.length == itemSizes.length",
			"itemSizes.length == itemProfits.length" })
	public Bin[] solve(int[] binsCapacities, int[][] itemSizes, double[][] itemProfits) {

		Bin[] bins = new Bin[binsCapacities.length];
		for (int i = 0; i < bins.length; i++) {
			bins[i] = new Bin(i,binsCapacities[i]);
		}
		
		if (binsCapacities.length == 0 || itemSizes[0].length == 0) {
			return bins;
		}

		int numItems = itemSizes[0].length;
		int[] itemsAssignments = initItemsAssignments(numItems);
		
		ProfitsMatrix pm = new ProfitsMatrix(itemProfits);

		for (int binIdx = 0; binIdx < binsCapacities.length; binIdx++) {
			Collection<Item> items = prepareItems(itemSizes[binIdx],
					pm.getCurrentColumn());
			Pair p = knapsack.solve(items, binsCapacities[binIdx]);
			for (Item item : p.getItems()) {
				itemsAssignments[item.id] = binIdx;
			}
			if (!pm.lastColumn()) {
				pm = pm.getResidualProfitMatrix(itemIndexes(p.getItems()));
			}
		}
		
		for (int itemIdx = 0; itemIdx < itemsAssignments.length; itemIdx++) {
			if (unassigned(itemsAssignments[itemIdx])) continue;
			int binIdx = itemsAssignments[itemIdx];
			bins[binIdx].assign(new Item(itemIdx,itemSizes[binIdx][itemIdx], itemProfits[binIdx][itemIdx]));
		}
		
		return bins;
	}

	private int[] initItemsAssignments(int numItems) {
		int[] ia = new int[numItems];
		for (int i = 0; i < ia.length; i++) {
			ia[i] = UNASSIGNED;
		}
		return ia;
	}
	
	private static boolean unassigned(int i) {
		return i == UNASSIGNED;
	}

	private static Collection<Integer> itemIndexes(Set<Item> items) {
		Collection<Integer> indexes = new ArrayList<Integer>(items.size());
		Iterator<Item> iter = items.iterator();
		while (iter.hasNext()) {
			indexes.add(iter.next().id);
		}
		
		return indexes;
	}

	private Collection<Item> prepareItems(int[] iSizes, double[] iProfits) {
		Collection<Item> items = new ArrayList<Item>(iSizes.length);
		for (int itemIdx = 0; itemIdx < iSizes.length; itemIdx++) {
			items.add(new Item(itemIdx, iSizes[itemIdx], iProfits[itemIdx]));
		}
		return items;
	}
}
