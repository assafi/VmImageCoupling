/**
 * Greedy_Recovery - Technion, Israel Institute of Technology
 * 
 * Author: Assaf Israel, 2012
 * Created: 12/03/2012
 */
package il.ac.technion.knapsack.guice;

import il.ac.technion.knapsack.dp_knapsack.ep.Pair;

import java.util.Collection;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

public class EfficientPairsModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(new TypeLiteral<Collection<Pair>>(){})
			.annotatedWith(Names.named("EP_Collection"))
			.toProvider(EfficientPairsCollectionProvider.class);
	}
}
