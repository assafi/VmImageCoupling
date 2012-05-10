/**
 * VmImageCoupling - Software Design, 236700 - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.data_extraction;

import il.ac.technion.knapsack.Bin;

import java.util.List;

public interface DataTranslator {

	public int[] prepareCapacitiesVector(List<?> bins);

	public int[][] prepareSizesMatrix(int size, List<?> items);

	public double[][] prepareWeightsMatrix(List<?> bins, List<?> items);

	public void commitAssignments(Bin[] answer, List<?> hosts, List<?> items);
}
