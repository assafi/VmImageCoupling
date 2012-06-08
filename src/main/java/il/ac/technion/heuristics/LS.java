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
	
	public void solve(List<Host> hosts, List<Image> images, Map<Image, List<VM>> im2vms, Map<Integer, VM> id2vmMap) {
		phase1(hosts, images);
		phase2(hosts, im2vms, id2vmMap);
		phase3(hosts, im2vms, id2vmMap);
	}

	private void phase1(List<Host> hosts, List<Image> images) {
		int count = 0;
		for (Image image : images) {
			boolean placed = false;
			for (Host host : hosts) {
				if (host.canAdd(image)) {
					host.add(image);
					count++;
					placed = true;
					break;
				}
			}
			if (!placed)
				throw new RuntimeException("Could not complete phase 1 (Image placement). Could not place " + image);
		}
		
		logger.info("===== End of phase 1 =====");
		for (Host host : hosts) {
			logger.debug(host.description());
		}
	}
	
	private void phase2(List<Host> hosts, Map<Image, List<VM>> im2vms, Map<Integer, VM> vms) {
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
		MtdvpSolution sol = mtdvp.solve(vArr, cArr, vmSz, vmPr, imSz, imVmCount, ids);
		assignSolution(sol.assignments,hosts,vms,im2vms);
	}

	private void phase3(List<Host> hosts, Map<Image, List<VM>> im2vms, Map<Integer, VM> id2vmMap) {
		logger.info("===== Start of phase 3 =====");
		logger.info("Number of local VMs at start of phase: " + countLocal(hosts));
		boolean changed = !im2vms.isEmpty(); // If all VMs have been placed we've reached the optimum
		DiffCache cache = new DiffCache(hosts.size());
		while (changed && !im2vms.isEmpty()) {
			changed = false;
			Host maxHost1 = null, maxHost2 = null;
			MtdvpSolution maxSolution = null;
			int maxI = -1, maxJ = -1;
			int maxDiff = 0;
			for (int i = 0; i < hosts.size(); i++) {
				Host host1 = hosts.get(i);
				for (int j = i + 1; j < hosts.size(); j++) {
					Host host2 = hosts.get(j);
					int base = host1.numVMs() + host2.numVMs();
					if (cache.validEntry(i, j)) {
						int cachedDiff = cache.getEntry(i, j);
						if (cachedDiff > maxDiff) {
							maxSolution = null;
							maxHost1 = host1;
							maxI = i;
							maxHost2 = host2;
							maxJ = j;
							maxDiff = cachedDiff;
							changed = true;
						}
					} else {
						MtdvpSolution solution = calcImprovement(host1,host2,im2vms);
						cache.updateCache(i, j, solution.profit - base);
						if (solution.profit - base > maxDiff) {
							maxSolution = solution;
							maxHost1 = host1;
							maxI = i;
							maxHost2 = host2;
							maxJ = j;
							maxDiff = solution.profit - base;
							changed = true; 
						}
					}
				}
			}
			if (changed) {
				if (maxSolution == null) {
					maxSolution = calcImprovement(maxHost1,maxHost2,im2vms);
				}
				int base = maxHost1.numVMs() + maxHost2.numVMs();
				int diff = maxSolution.profit - base;
				logger.info("Current improvement: " + diff + ", Current improvement at hosts: " + maxHost1.id + " & " + maxHost2.id + ", #Local VMs: " + countLocal(hosts));
				assignImprovement(maxHost1,maxHost2,maxSolution, id2vmMap);
				cache.invalidateEntry(maxI, maxJ);
				if (maxHost1.numVMs() + maxHost2.numVMs() != maxSolution.profit) throw new RuntimeException(maxHost1.numVMs() + maxHost2.numVMs() + " != " + maxSolution.profit);
			}
		}
	}

	private int countLocal(List<Host> hosts) {
		int count = 0;
		for (Host host : hosts) {
			count += host.numVMs();
		}
		return count;
	}

	private void assignImprovement(Host maxHost1, Host maxHost2,
			MtdvpSolution maxSolution, Map<Integer, VM> id2vmMap) {
		maxHost1.reset();
		maxHost2.reset();
		for (int id : maxSolution.assignments[0].ids) {
			if (!maxHost1.add(id2vmMap.get(id))) throw new RuntimeException("Unable to assign VM " + id2vmMap.get(id) + " to host: " + maxHost1);
		}
		for (int id : maxSolution.assignments[1].ids) {
			if (!maxHost2.add(id2vmMap.get(id))) throw new RuntimeException("Unable to assign VM " + id2vmMap.get(id) + " to host: " + maxHost2);
		}
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
				throw new RuntimeException("Invalid cache entry [i=" + i + ",j=" + j + "]");
			cache[i][j] = value;
			valid[i][j] = true;
			cache[j][i] = value;
			valid[j][i] = true;
		}
		
		void invalidateEntry(int i, int j) {
			if (i < 0 || j < 0 || i >= length || j >= length) 
				throw new RuntimeException("Invalid cache entry [i=" + i + ",j=" + j + "]");
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
				throw new RuntimeException("Invalid cache entry [i=" + i + ",j=" + j + "]");
			return valid[i][j];
		}
		
		int getEntry(int i, int j) {
			if (i < 0 || j < 0 || i >= length || j >= length) 
				throw new RuntimeException("Invalid cache entry [i=" + i + ",j=" + j + "]");
			return cache[i][j];
		}
	}
	
	private MtdvpSolution calcImprovement(Host host1, Host host2
			, Map<Image, List<VM>> im2vms) {
		int k = 1; // Temp fix, will generalize later

		Set<Image> up2Kreplicas = new HashSet<Image>();
		extractUp2Kreplicas(host1.images(),host2.images(),up2Kreplicas,k);

		MultiTDVP mtdvp = new MultiTDVP();
		MtdvpSolution maxSol = null;
		
		updateMapByHost(host1,im2vms);
		updateMapByHost(host2,im2vms);
		int M = im2vms.keySet().size();

		for (int i = 0; i < Math.pow(2, up2Kreplicas.size()); i++) {
			Host clone1 = new Host(1,host1.ramCapacity,host1.storageCapacity);
			Host clone2 = new Host(2,host2.ramCapacity,host2.storageCapacity);
			
			if (!recImgAssignment(clone1,clone2,i,up2Kreplicas.iterator())) {
				clone1.reset();
				clone2.reset();
				continue;
			}
			
			int[] vArr = new int[]{clone1.availableRAM(), clone2.availableRAM()};
			int[] cArr = new int[]{clone1.availableStorage(), clone2.availableStorage()};
			
			int[][] imSz = CouplingUtils.imageSizesPerHost(Arrays.asList(clone1,clone2), im2vms.keySet());
			int[] imVmCount = CouplingUtils.imageVmsCount(im2vms);
			int max_Nk = maxValue(imVmCount);
			int[][] vmSz = CouplingUtils.vmSizes(M, max_Nk,im2vms);
			int[][] vmPr = CouplingUtils.vmProfits(M, max_Nk, im2vms);
			int[][] ids = CouplingUtils.vmIds(M, max_Nk, im2vms);
			
			clone1.reset();
			clone2.reset();
			
			MtdvpSolution sol = mtdvp.solve(vArr, cArr, vmSz, vmPr, imSz, imVmCount, ids);
			if (maxSol == null || maxSol.profit < sol.profit) {
				maxSol = sol;
			}
		}
		
		resetMapByHost(host2,im2vms);
		resetMapByHost(host1,im2vms);
		return maxSol;
	}

	private void resetMapByHost(Host host, Map<Image, List<VM>> im2vms) {
		for (VM	vm : host.vms()) {
			if (!im2vms.containsKey(vm.image)) {
				continue;
			}
			im2vms.get(vm.image).remove(vm);
			if (im2vms.get(vm.image).isEmpty()) {
				im2vms.remove(vm.image);
			}
		}
	}

	private void updateMapByHost(Host host, Map<Image, List<VM>> im2vms) {
		for (VM	vm : host.vms()) {
			if (!im2vms.containsKey(vm.image)) {
				im2vms.put(vm.image, new LinkedList<VM>());
			}
			im2vms.get(vm.image).add(vm);
		}
	}

	private boolean recImgAssignment(Host clone1, Host clone2,
			int i, Iterator<Image> iter) {
		
		if (!iter.hasNext()) return true;
		if (i % 2 == 0) {
			if (!clone1.add(iter.next())) return false;
		} else {
			if (!clone2.add(iter.next())) return false;
		}
		return recImgAssignment(clone1,clone2,i/2,iter);
	}

	private void extractUp2Kreplicas(Collection<Image> images,
			Set<Image> up2Kreplicas, int k) {
		for (Image image : images) {
			if (image.numReplicas() <= k) {
				up2Kreplicas.add(image);
			} 
		}
	}
	
	private void extractUp2Kreplicas(Collection<Image> imageCollection1, Collection<Image> imageCollection2,
			Set<Image> up2Kreplicas, int k) {
		
		extractUp2Kreplicas(imageCollection1,up2Kreplicas,k);
		extractUp2Kreplicas(imageCollection2,up2Kreplicas,k);
		
		for (Image image : imageCollection1) {
			if (imageCollection2.contains(image) && image.numReplicas() == k+1) {
				up2Kreplicas.add(image);
			}
		}
		
		for (Image image : imageCollection2) {
			if (imageCollection1.contains(image) && image.numReplicas() == k+1) {
				up2Kreplicas.add(image);
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
	
	private void assignSolution(Solution[] sol, List<Host> hosts, Map<Integer, VM> vms, Map<Image, List<VM>> im2vms) {
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
