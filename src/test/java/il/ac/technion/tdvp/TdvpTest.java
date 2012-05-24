/**
 * VmImageCoupling - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.tdvp;

import il.ac.technion.beans.CouplingUtils;
import il.ac.technion.beans.Host;
import il.ac.technion.beans.Image;
import il.ac.technion.beans.VM;
import il.ac.technion.configuration.Configuration;
import il.ac.technion.configuration.ConfigurationException;
import il.ac.technion.tdvp.TDVP.Solution;

import java.io.IOException;
import java.util.ArrayList;
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
		
		Solution sol = runTdvp(V, C, im2vms);
		Assert.assertEquals(5, sol.profit);
	}

	private Solution runTdvp(int V, int C, Map<Image, List<VM>> im2vms) {
		int M = im2vms.keySet().size();
		List<Image> images = new ArrayList<Image>(im2vms.keySet());
		int[] imVmCount = CouplingUtils.imageVmsCount(images, im2vms);
		int[] imSz = CouplingUtils.imageSizes(im2vms.keySet());
		int max_Nk = maxValue(imVmCount);
		int[][] vmSz = CouplingUtils.vmSizes(M, max_Nk, images, im2vms);
		int[][] vmPr = CouplingUtils.vmProfits(M, max_Nk, im2vms);
		int[][] ids = CouplingUtils.vmIds(M, max_Nk, images, im2vms);
		Solution sol = tdvp.solve(V, C, vmSz, vmPr, imSz, imVmCount, ids);
		return sol;
	}

	@Test
	public void simpleTest() throws IOException, ConfigurationException {
		String confFilePath = TdvpTest.class.getResource("simple.xml").getPath();
		Configuration conf = new Configuration(confFilePath);
		Host host = conf.getHosts().get(0);
		int V = host.availableRAM();
		int C = host.availableStorage();
		Map<Image, List<VM>> im2vms = conf.getImageMap();
		Solution sol = runTdvp(V, C, im2vms);
		Assert.assertEquals(5, sol.profit);
	}
	
	@Test
	public void complexTest() throws IOException, ConfigurationException {
		String confFilePath = TdvpTest.class.getResource("complex.xml").getPath();
		Configuration conf = new Configuration(confFilePath);
		Host host = conf.getHosts().get(0);
		int V = host.availableRAM();
		int C = host.availableStorage();
		Map<Image, List<VM>> im2vms = conf.getImageMap();
		Solution sol = runTdvp(V, C, im2vms);
		Assert.assertEquals(7, sol.profit);
	}
	
	private int maxValue(int[] n_ks) {
		int max = 0;
		for (int n_k : n_ks) {
			max = Math.max(max, n_k);
		}
		return max;
	}
}
