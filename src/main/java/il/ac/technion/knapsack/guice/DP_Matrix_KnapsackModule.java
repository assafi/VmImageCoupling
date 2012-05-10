/**
 * Greedy_Recovery - Technion, Israel Institute of Technology
 * 
 * Author: Assaf Israel, 2012
 * Created: 11 באפר 2012
 */
package il.ac.technion.knapsack.guice;

import il.ac.technion.knapsack.KnapsackAlg;
import il.ac.technion.knapsack.dp_knapsack.matrix.DP_Matrix_Knapsack;

/**
 * @author Assaf
 *
 */
public class DP_Matrix_KnapsackModule extends KnapsackModule {

	@Override
	protected void configure() {
		bind(KnapsackAlg.class).to(DP_Matrix_Knapsack.class);
	}
}
