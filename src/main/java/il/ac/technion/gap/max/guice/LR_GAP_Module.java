/**
 * Greedy_Recovery - Technion, Israel Institute of Technology
 * 
 * Author: Assaf Israel, 2012
 * Created: 12/03/2012
 */
package il.ac.technion.gap.max.guice;

import il.ac.technion.gap.GAP_Alg;
import il.ac.technion.gap.max.localratio.LR_GAP;
import il.ac.technion.knapsack.guice.DP_EP_KnapsackModule;
import il.ac.technion.knapsack.guice.KnapsackModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;

public class LR_GAP_Module extends AbstractModule {

	@Override
	protected void configure() {
		bind(KnapsackModule.class).toInstance(new DP_EP_KnapsackModule());
	}
	
	@Provides
	public GAP_Alg getGAP(KnapsackModule module) {
		Injector inj = Guice.createInjector(module);
		return inj.getInstance(LR_GAP.class);
	}
}
