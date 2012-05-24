/**
 * VmImageCoupling - Software Design, 236700 - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.configuration;


import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

public class TestConfiguration {

	@Ignore
	@Test
	public void testSimpleConfiguration() throws IOException, ConfigurationException {
		String simpleConfigFile = getClass().getResource("simple.xml").getPath();
		Configuration conf = new Configuration(simpleConfigFile);
		System.out.println(conf);
	}
	
	@Test
	public void testRc2() throws IOException, ConfigurationException {
		String simpleConfigFile = getClass().getResource("rc2_extended.xml").getPath();
		Configuration conf = new Configuration(simpleConfigFile);
		System.out.println(conf);
	}
}
