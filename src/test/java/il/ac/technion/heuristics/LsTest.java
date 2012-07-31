/**
 * VmImageCoupling - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.heuristics;

import il.ac.technion.beans.Host;
import il.ac.technion.beans.Image;
import il.ac.technion.beans.VM;
import il.ac.technion.configuration.Configuration;
import il.ac.technion.configuration.ConfigurationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;



public class LsTest {

	private static Logger logger = Logger.getLogger(LsTest.class);
	
	LS ls = new LS();
	
	@Ignore
	@Test
	public void mtdvpTest() throws IOException, ConfigurationException {
		runTest("multi_tdvp.xml");
	}
	
	@Ignore
	@Test
	public void simpleTest() throws IOException, ConfigurationException {
		runTest("simple_ls.xml");
	}
	
	@Ignore
	@Test
	public void rc2SmallTest() throws IOException, ConfigurationException {
		runTest("rc2_extended_small.xml");
	}

	@Ignore
	@Test
	public void rc2Test() throws IOException, ConfigurationException {
		runTest("rc2_extended.xml");
	}
	
	@Ignore
	@Test
	public void rc2300sTest() throws IOException, ConfigurationException {
		runTest("rc2_extended_300s.xml");
	}

	@Test
	public void redundancyTest() throws IOException, ConfigurationException {
		runTest("simple_ls_k2.xml");
	}
	
	private void runTest(String fileName) throws IOException, ConfigurationException {
		String confFilePath = LsTest.class.getResource(fileName).getPath();
		Configuration conf = new Configuration(confFilePath);
		List<Host> hosts = runAndAssign(conf);
		
		int numLocals = 0;
		for (Host host : hosts) {
			logger.info(host.description());
			numLocals += host.numVMs();
		}
		logger.info("Number of local VMs: " + numLocals);
	}
	
	private List<Host> runAndAssign(Configuration conf) {
		List<Host> hosts = conf.getHosts();
		Map<Image, List<VM>> im2vms = conf.getImageMap();
		ls.solve(hosts, new ArrayList<Image>(im2vms.keySet()),im2vms, conf.getId2VmMap(),conf.getImageRedundancy());
		return hosts;
	}
}
