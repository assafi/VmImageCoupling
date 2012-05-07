/**
 * VmImageCoupling - Software Design, 236700 - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.configuration;


import il.ac.technion.configuration.Configuration;
import il.ac.technion.configuration.ConfigurationException;

import java.io.IOException;

import org.junit.Test;

public class TestConfiguration {

	@Test
	public void testSimpleConfiguration() throws IOException, ConfigurationException {
		String simpleConfigFile = getClass().getResource("simple.xml").getPath();
		Configuration conf = new Configuration(simpleConfigFile);
		System.out.println(conf);
	}
}
