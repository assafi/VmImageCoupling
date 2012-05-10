/**
 * Greedy_Recovery - Technion, Israel Institute of Technology
 * 
 * Author: Assaf Israel, 2012
 * Created: 12/03/2012
 */
package il.ac.technion.gap;

import il.ac.technion.beans.Host;
import il.ac.technion.data_extraction.DataTranslator;
import il.ac.technion.knapsack.Bin;

import java.util.List;

public abstract class GAP_Alg {
	
	private DataTranslator dt;
	
	public GAP_Alg(DataTranslator dt) {
		this.dt = dt;
	}
	
	public abstract Bin[] solve(int[] binsCapacities, int[][] _itemSizes, double[][] _itemWeights);
	
	public void solve(List<Host> hosts, List<?> items) {
		int[] binsCapacities = dt.prepareCapacitiesVector(hosts);
		int[][] itemSizes = dt.prepareSizesMatrix(hosts.size(), items);
		double[][] itemCosts = dt.prepareWeightsMatrix(hosts, items);
		
		Bin[] answer = solve(binsCapacities,itemSizes,itemCosts);
		
		dt.commitAssignments(answer,hosts,items);
	}
}
