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
import il.ac.technion.tdvp.MultiTDVP.MtdvpSolution;
import il.ac.technion.tdvp.TDVP.Solution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;



public class MultilTdvpTest {

	private static Logger logger = Logger.getLogger(MultilTdvpTest.class);
	
	private MultiTDVP mtdvp = new MultiTDVP();
	
	
	@Test
	public void multiTest() throws IOException, ConfigurationException {
		String confFilePath = MultilTdvpTest.class.getResource("multi_tdvp.xml").getPath();
		Configuration conf = new Configuration(confFilePath);
		List<Host> hosts = runAndAssign(conf);
		for (Host host : hosts) {
			logger.info(host.description());
		}
		Assert.assertEquals(7, hosts.get(0).numVMs());
		Assert.assertEquals(4, hosts.get(1).numVMs());
	}

	@Test 
	public void zeroSizeImages() throws IOException, ConfigurationException {
		String confFilePath = MultilTdvpTest.class.getResource("half_empty_tdvp.xml").getPath();
		Configuration conf = new Configuration(confFilePath);
		List<Host> hosts = runAndAssign(conf);
		for (Host host : hosts) {
			logger.info(host.description());
		}
		
		Assert.assertEquals(10, hosts.get(0).numVMs());
		Assert.assertEquals(6, hosts.get(1).numVMs());
	}
	
	private List<Host> runAndAssign(Configuration conf) {
		List<Host> hosts = conf.getHosts();
		Map<Integer, VM> vms = conf.getId2VmMap();
		Map<Image, List<VM>> im2vms = conf.getImageMap();
		List<Image> images = new ArrayList<Image>(im2vms.keySet());
		
		int[] vArr = new int[hosts.size()];
		int[] cArr = new int[hosts.size()];
		
		for (int i = 0; i < cArr.length; i++) {
			vArr[i] = hosts.get(i).ramCapacity;
			cArr[i] = hosts.get(i).storageCapacity;
		}
		
		int M = im2vms.keySet().size();
		int[] imVmCount = CouplingUtils.imageVmsCount(images,im2vms);
		int[][] imSz = new int[2][];
		imSz[0] = CouplingUtils.imageSizes(im2vms.keySet());
		imSz[1] = imSz[0];
		int max_Nk = maxValue(imVmCount);
		int[][] vmSz = CouplingUtils.vmSizes(M, max_Nk, images, im2vms);
		int[][] vmPr = CouplingUtils.vmProfits(M, max_Nk, im2vms);
		int[][] ids = CouplingUtils.vmIds(M, max_Nk, images, im2vms);
		MtdvpSolution sol = mtdvp.solve(vArr, cArr, vmSz, vmPr, imSz, imVmCount, ids);

		assignSolution(sol.assignments,hosts,vms);
		return hosts;
	}
	
	private void assignSolution(Solution[] sol, List<Host> hosts,Map<Integer, VM> vms) {
		int i = 0;
		for (Host host : hosts) {
			for (int id : sol[i].ids) {
				host.add(vms.get(id));
			}
			i++;
		}
	}

	private int maxValue(int[] n_ks) {
		int max = 0;
		for (int n_k : n_ks) {
			max = Math.max(max, n_k);
		}
		return max;
	}
}
