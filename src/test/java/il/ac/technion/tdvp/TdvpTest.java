/**
 * VmImageCoupling - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.tdvp;

import il.ac.technion.beans.Host;
import il.ac.technion.beans.Image;
import il.ac.technion.beans.VM;
import il.ac.technion.configuration.Configuration;
import il.ac.technion.configuration.ConfigurationException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;



public class TdvpTest {

	private TDVP tdvp = new TDVP();
	
	@Test
	public void verSimpleTest() throws IOException, ConfigurationException {
		String confFilePath = TdvpTest.class.getResource("verySimple.xml").getPath();
		Configuration conf = new Configuration(confFilePath);
		Host host = conf.getHosts().get(0);
		int V = host.availableRAM();
		int C = host.availableStorage();
		Map<Image, List<VM>> im2vms = conf.getImageMap();
		Map<Image, List<VM>> sol = tdvp.solve(V, C, im2vms);
		Assert.assertEquals(2, sol.keySet().size());
		Assert.assertEquals(5, totalSize(sol.values()));
//		printSolution(sol);
	}

	@Test
	public void simpleTest() throws IOException, ConfigurationException {
		String confFilePath = TdvpTest.class.getResource("simple.xml").getPath();
		Configuration conf = new Configuration(confFilePath);
		Host host = conf.getHosts().get(0);
		int V = host.availableRAM();
		int C = host.availableStorage();
		Map<Image, List<VM>> im2vms = conf.getImageMap();
		Map<Image, List<VM>> sol = tdvp.solve(V, C, im2vms);
		Assert.assertEquals(1, sol.keySet().size());
//		printSolution(sol);
		Assert.assertEquals(5, totalSize(sol.values()));
	}
	
	@Test
	public void complexTest() throws IOException, ConfigurationException {
		String confFilePath = TdvpTest.class.getResource("complex.xml").getPath();
		Configuration conf = new Configuration(confFilePath);
		Host host = conf.getHosts().get(0);
		int V = host.availableRAM();
		int C = host.availableStorage();
		Map<Image, List<VM>> im2vms = conf.getImageMap();
		Map<Image, List<VM>> sol = tdvp.solve(V, C, im2vms);
		Assert.assertEquals(2, sol.keySet().size());
//		printSolution(sol);
		Assert.assertEquals(7, totalSize(sol.values()));
	}
	
	private int totalSize(Collection<List<VM>> values) {
		int count = 0;
		for (List<VM> list : values) {
			count += list.size();
		}
		return count;
	}

	private static void printSolution(Map<Image, List<VM>> sol) {
		for (Image im : sol.keySet()) {
			System.out.print(im + " - ");
			for (VM vm : sol.get(im)) {
				System.out.print(vm + "; ");
			}
			System.out.println();
		}
	}
}
