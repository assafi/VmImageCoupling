/**
 * Greedy_Recovery - Technion, Israel Institute of Technology
 * 
 * Author: Assaf Israel, 2012
 * Created: 12/03/2012
 */
package il.ac.technion.knapsack.guice;

import il.ac.technion.knapsack.KnapsackAlg;
import il.ac.technion.knapsack.dp_knapsack.ep.DP_EP_Knapsack;

import com.google.inject.Guice;
import com.google.inject.Provides;

public class DP_EP_KnapsackModule extends KnapsackModule {

	@Override
	protected void configure() {
		bind(KnapsackAlg.class).to(DP_EP_Knapsack.class);
	}

	@Provides
	public DP_EP_Knapsack getEP_Knapsack() {
		return new DP_EP_Knapsack(Guice.createInjector(new EfficientPairsModule()));
	}
}
