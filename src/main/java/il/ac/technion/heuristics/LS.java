/**
 * VmImageCoupling - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.heuristics;

import il.ac.technion.beans.CouplingUtils;
import il.ac.technion.beans.Host;
import il.ac.technion.beans.Image;
import il.ac.technion.beans.VM;
import il.ac.technion.tdvp.MultiTDVP;
import il.ac.technion.tdvp.MultiTDVP.MtdvpSolution;
import il.ac.technion.tdvp.TDVP.Solution;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class LS {

	private static Logger logger = Logger.getLogger(LS.class);

	public SimulationResults solve(List<Host> hosts, List<Image> images,
			Map<Image, List<VM>> im2vms, Map<Integer, VM> id2vmMap,
			int imageRedundancy) {
		SimulationResults sr = new SimulationResults(images.size(), countRemote(im2vms.values()), imageRedundancy);
		if (!phase1(hosts, images, imageRedundancy)) {
			return sr;
		}
		sr.feasiblePacking = true;
		phase2(hosts, im2vms, id2vmMap);
		return phase3(hosts, im2vms, id2vmMap, imageRedundancy, sr);
	}

	private boolean phase1(List<Host> hosts, List<Image> images, int imageRedundancy) {
		for (Image image : images) {
			int k = imageRedundancy;
			boolean placed = false;
			for (Host host : hosts) {
				if (host.canAdd(image)) {
					host.add(image);
					k--;
				}
				if (k == 0) {
					placed = true;
					break;
				}
			}
			if (!placed)
				return false;
		}

		logger.info("===== End of phase 1 =====");
		for (Host host : hosts) {
			logger.debug(host.description());
		}
		return true;
	}

	private void phase2(List<Host> hosts, Map<Image, List<VM>> im2vms,
			Map<Integer, VM> vms) {
		MultiTDVP mtdvp = new MultiTDVP();
		int[] vArr = new int[hosts.size()];
		int[] cArr = new int[hosts.size()];

		for (int i = 0; i < cArr.length; i++) {
			vArr[i] = hosts.get(i).availableRAM();
			cArr[i] = hosts.get(i).availableStorage();
		}

		int M = im2vms.keySet().size();
		int[] imVmCount = CouplingUtils.imageVmsCount(im2vms);
		int[][] imSz = CouplingUtils.imageSizesPerHost(hosts, im2vms.keySet());
		int max_Nk = maxValue(imVmCount);
		int[][] vmSz = CouplingUtils.vmSizes(M, max_Nk, im2vms);
		int[][] vmPr = CouplingUtils.vmProfits(M, max_Nk, im2vms);
		int[][] ids = CouplingUtils.vmIds(M, max_Nk, im2vms);
		MtdvpSolution sol = mtdvp.solve(vArr, cArr, vmSz, vmPr, imSz, imVmCount,
				ids);
		assignSolution(sol.assignments, hosts, vms, im2vms);
	}

	private SimulationResults phase3(List<Host> hosts, Map<Image, List<VM>> im2vms,
			Map<Integer, VM> id2vmMap, int imageRedundancy, SimulationResults sr) {
		logger.info("===== Start of phase 3 =====");
		logger.info("Number of local VMs at start of phase: " + countLocal(hosts));
		boolean changed = true;
		
		while (changed && !im2vms.isEmpty()) {
			changed = false;
			for (int i = 0; i < hosts.size(); i++) {
				Host host1 = hosts.get(i);
				for (int j = i + 1; j < hosts.size(); j++) {
					Host host2 = hosts.get(j);
					int base = host1.numVMs() + host2.numVMs();

					MtdvpSolution solution = calcImprovement(host1, host2, im2vms,
							imageRedundancy);

					if (solution != null && solution.profit - base > 0) {
						logger.info("Current improvement: " + (solution.profit - base)
								+ ", Current improvement at hosts: " + host1.id + " & "
								+ host2.id + ", #Local VMs: " + countLocal(hosts));
						assignImprovement(host1, host2, solution, id2vmMap, im2vms);
						changed = true;
					}

				}
			}
		}

		sr.localCount = countLocal(hosts);
		sr.remoteCount = countRemote(im2vms.values());
		sr.placement = hosts;
		
		logger.info("==== Phase 3 Completed =====");
		if (!im2vms.isEmpty()) {
			logger.info("Incomplete assignment. Local VMs: "
					+ countLocal(hosts)
					+ ", Total VMs: "
					+ (countLocal(hosts) + countRemote(im2vms.values())
							+ ", Remote VMs: " + countRemote(im2vms.values())));
		} else {
			logger.info("Complete assignment. Total VMs (all Local): "
					+ countLocal(hosts));
		}
		
		return sr;
	}

	private int countRemote(Collection<List<VM>> values) {
		int count = 0;
		for (List<VM> list : values) {
			count += list.size();
		}
		return count;
	}

	private int countLocal(List<Host> hosts) {
		int count = 0;
		for (Host host : hosts) {
			count += host.numVMs();
		}
		return count;
	}

	private void assignImprovement(Host host1, Host host2,
			MtdvpSolution maxSolution, Map<Integer, VM> id2vmMap,
			Map<Image, List<VM>> im2vms) {
		addToMapByHost(host1, im2vms);
		addToMapByHost(host2, im2vms);
		host1.reset();
		host2.reset();
		for (int id : maxSolution.assignments[0].ids) {
			if (!host1.add(id2vmMap.get(id)))
				throw new RuntimeException("Unable to assign VM " + id2vmMap.get(id)
						+ " to host: " + host1);
		}
		for (int id : maxSolution.assignments[1].ids) {
			if (!host2.add(id2vmMap.get(id)))
				throw new RuntimeException("Unable to assign VM " + id2vmMap.get(id)
						+ " to host: " + host2);
		}
		removeFromMapByHost(host1, im2vms);
		removeFromMapByHost(host2, im2vms);
	}

	private class DiffCache {

		private int[][] cache;
		private boolean[][] valid;
		private int length;

		DiffCache(int size) {
			this.cache = new int[size][size];
			this.valid = new boolean[size][size];
			this.length = size;
		}

		void updateCache(int i, int j, int value) {
			if (i < 0 || j < 0 || i >= length || j >= length)
				throw new RuntimeException("Invalid cache entry [i=" + i + ",j=" + j
						+ "]");
			cache[i][j] = value;
			valid[i][j] = true;
			cache[j][i] = value;
			valid[j][i] = true;
		}

		void invalidateEntry(int i, int j) {
			if (i < 0 || j < 0 || i >= length || j >= length)
				throw new RuntimeException("Invalid cache entry [i=" + i + ",j=" + j
						+ "]");
			for (int r = 0; r < length; r++) {
				cache[i][r] = 0;
				valid[i][r] = false;
				cache[j][r] = 0;
				valid[j][r] = false;
				cache[r][i] = 0;
				valid[r][i] = false;
				cache[r][j] = 0;
				valid[r][j] = false;
			}
		}

		boolean validEntry(int i, int j) {
			if (i < 0 || j < 0 || i >= length || j >= length)
				throw new RuntimeException("Invalid cache entry [i=" + i + ",j=" + j
						+ "]");
			return valid[i][j];
		}

		int getEntry(int i, int j) {
			if (i < 0 || j < 0 || i >= length || j >= length)
				throw new RuntimeException("Invalid cache entry [i=" + i + ",j=" + j
						+ "]");
			return cache[i][j];
		}
	}

	private MtdvpSolution calcImprovement(Host host1, Host host2,
			Map<Image, List<VM>> im2vms, int imageRedundancy) {

		Set<Image> up2Kreplicas = new HashSet<Image>();
		Set<Image> fixedImages = new HashSet<Image>();

		extractUp2Kreplicas(host1.images(), host2.images(), up2Kreplicas,
				fixedImages, imageRedundancy);

		MultiTDVP mtdvp = new MultiTDVP();
		MtdvpSolution maxSol = null;

		addToMapByHost(host1, im2vms);
		addToMapByHost(host2, im2vms);
		int M = im2vms.keySet().size();

		for (int i = 0; i < Math.pow(2, up2Kreplicas.size()); i++) {
			Host clone1 = new Host(1, host1.ramCapacity, host1.storageCapacity);
			Host clone2 = new Host(2, host2.ramCapacity, host2.storageCapacity);

			assignFixed(fixedImages, clone1);
			assignFixed(fixedImages, clone2);

			if (!recImgAssignment(clone1, clone2, i, up2Kreplicas.iterator())) {
				clone1.reset();
				clone2.reset();
				continue;
			}

			int[] vArr = new int[] { clone1.availableRAM(), clone2.availableRAM() };
			int[] cArr = new int[] { clone1.availableStorage(),
					clone2.availableStorage() };

			int[][] imSz = CouplingUtils.imageSizesPerHost(
					Arrays.asList(clone1, clone2), im2vms.keySet());
			int[] imVmCount = CouplingUtils.imageVmsCount(im2vms);
			int max_Nk = maxValue(imVmCount);
			int[][] vmSz = CouplingUtils.vmSizes(M, max_Nk, im2vms);
			int[][] vmPr = CouplingUtils.vmProfits(M, max_Nk, im2vms);
			int[][] ids = CouplingUtils.vmIds(M, max_Nk, im2vms);

			clone1.reset();
			clone2.reset();

			MtdvpSolution sol = mtdvp.solve(vArr, cArr, vmSz, vmPr, imSz, imVmCount,
					ids);
			if (maxSol == null || maxSol.profit < sol.profit) {
				maxSol = sol;
			}
		}

		removeFromMapByHost(host2, im2vms);
		removeFromMapByHost(host1, im2vms);
		return maxSol;
	}

	private void assignFixed(Set<Image> fixedImages, Host host) {
		for (Image image : fixedImages) {
			host.add(image);
		}
	}

	private void removeFromMapByHost(Host host, Map<Image, List<VM>> im2vms) {
		for (VM vm : host.vms()) {
			if (!im2vms.containsKey(vm.image)) {
				continue;
			}
			im2vms.get(vm.image).remove(vm);
			if (im2vms.get(vm.image).isEmpty()) {
				im2vms.remove(vm.image);
			}
		}
	}

	private void addToMapByHost(Host host, Map<Image, List<VM>> im2vms) {
		for (VM vm : host.vms()) {
			if (!im2vms.containsKey(vm.image)) {
				im2vms.put(vm.image, new LinkedList<VM>());
			}
			if (!im2vms.get(vm.image).contains(vm)) {
				im2vms.get(vm.image).add(vm);
			}
		}
	}

	private boolean recImgAssignment(Host clone1, Host clone2, int i,
			Iterator<Image> iter) {

		if (!iter.hasNext())
			return true;
		if (i % 2 == 0) {
			if (!clone1.add(iter.next()))
				return false;
		} else {
			if (!clone2.add(iter.next()))
				return false;
		}
		return recImgAssignment(clone1, clone2, i / 2, iter);
	}

	private void extractUp2Kreplicas(Collection<Image> images,
			Set<Image> up2Kreplicas, int k) {
		for (Image image : images) {
			if (image.numReplicas() <= k) {
				up2Kreplicas.add(image);
			}
		}
	}

	private void extractUp2Kreplicas(Collection<Image> imageCollection1,
			Collection<Image> imageCollection2, Set<Image> up2Kreplicas,
			Set<Image> fixedImages, int k) {

		extractUp2Kreplicas(imageCollection1, up2Kreplicas, k);
		extractUp2Kreplicas(imageCollection2, up2Kreplicas, k);

		for (Image image : imageCollection1) {
			if (imageCollection2.contains(image)) {
				if (image.numReplicas() == k + 1) {
					up2Kreplicas.add(image);
				} else if (image.numReplicas() == k) {
					up2Kreplicas.remove(image);
					fixedImages.add(image);
				}
			}
		}
	}

	private int maxValue(int[] n_ks) {
		int max = 0;
		for (int n_k : n_ks) {
			max = Math.max(max, n_k);
		}
		return max;
	}

	private void assignSolution(Solution[] sol, List<Host> hosts,
			Map<Integer, VM> vms, Map<Image, List<VM>> im2vms) {
		int i = 0;

		logger.info("===== End of phase 2 =====");
		for (Host host : hosts) {
			for (int id : sol[i].ids) {
				VM vm = vms.get(id);
				if (!host.add(vm)) {
					throw new RuntimeException("Could not add vm to host");
				}
				im2vms.get(vm.image).remove(vm);
				if (im2vms.get(vm.image).isEmpty()) {
					im2vms.remove(vm.image);
				}
			}
			i++;
			logger.debug(host.description());
		}
	}
}
