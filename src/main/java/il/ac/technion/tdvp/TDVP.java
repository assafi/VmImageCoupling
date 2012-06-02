/**
 * VmImageCoupling - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.tdvp;

import il.ac.technion.beans.Image;
import il.ac.technion.beans.VM;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * An optimal pseudo-polynomial time algorithm for the 2-Dimensional Vector
 * Packing
 */
public class TDVP {

	private static Logger log = Logger.getLogger(TDVP.class);
	
	private class Data_Pack {
		int[][][] data;
		int[][][] data_bt;
	}
	
	public class Solution {
		public int profit;
		public Set<Integer> ids = new HashSet<Integer>(); 
	}
	
	public Solution solve(int V, int C, int[][] vmSz, int[][] vmPr, int[] imSz, int[] imVmCount, int[][] ids) {
		int Max_Nk = maxValue(imVmCount);
		int P = V; // This should be changed in the case of general cost functions
		int M = imVmCount.length;

		Data_Pack hp = buildH(P, Max_Nk,M,imVmCount,ids,vmSz,	vmPr);
		Data_Pack fp = buildF(hp.data, M, P, C, imSz,imVmCount);
		
		Solution sol = extractOptimum(fp,hp,M,P,C,V,imVmCount,imSz,vmPr);
		log.trace("Solution profit: " + sol.profit);
		return sol;
	}

	/**
	 * @param P
	 *          Bound of the profit
	 * @param max_Nk
	 *          Maximum number of items per color
	 * @param M
	 *          Number of colors
	 * @param im2vms
	 * @return <code>H[k][r][a]</code> denotes the minimum total size of a subset
	 *         of items, out of the first <code>r+1</code> in color <code>k</code>
	 *         , such that the total profit of the subset is <code>a</code>.
	 */
	private Data_Pack buildH(int P, int max_Nk, int M,int[] n, int[][] ids, int[][] sizes,
			int[][] profits) {
		int[][][] H = new int[M][max_Nk][P + 1];
		int[][][] H_bt = new int[M][max_Nk][P + 1];
		
		Data_Pack hp = new Data_Pack();
		hp.data = H;
		hp.data_bt = H_bt;
		
		for (int k = 0; k < M; k++) {
			for (int r = 0; r < n[k]; r++) {
				for (int a = 0; a <= P; a++) {
					H[k][r][a] = h(H, k, r - 1, a);
					if (h(H, k, r - 1, a - profits[k][r]) == Integer.MAX_VALUE) { // Avoiding over-flow
						continue;
					}
					if (h(H, k, r - 1, a) > sizes[k][r] + h(H, k, r - 1, a - profits[k][r])) {
						H[k][r][a] = sizes[k][r] + h(H, k, r - 1, a - profits[k][r]);
						H_bt[k][r][a] = ids[k][r];
					} 
				}
			}
		}
		return hp;
	}

	private int h(int[][][] H, int k, int r, int a) {
		if (a == 0)
			return 0; // Stop conditions
		if (a < 0 || r <= -1)
			return Integer.MAX_VALUE;
		return H[k][r][a];
	}

	/**
	 * @param H
	 *          The H matrix
	 * @param M
	 *          Number of colors
	 * @param P
	 *          Bound of the profit
	 * @param C
	 *          Number of compartments
	 * @param c
	 *          Vector of color sizes
	 * @param n
	 *          Vector of number of items per color
	 * @return <code>F[k][a][l]</code> denote the minimum total size of a subset
	 *         of items whose colors are among the first <code>k</code>, such that
	 *         the items use <code>l</code> compartments, and the total profit of
	 *         the items is <code>a</code>.
	 */
	private Data_Pack buildF(int[][][] H, int M, int P, int C, int[] c, int[] n) {
		int[][][] F = new int[M][P + 1][C + 1];
		int[][][] F_bt = new int[M][P + 1][C + 1];
		
		Data_Pack fp = new Data_Pack();
		fp.data = F;
		fp.data_bt = F_bt;
		
		for (int k = 0; k < M; k++) {
			for (int a = 0; a <= P; a++) {
				for (int l = 0; l <= C; l++) {
					F[k][a][l] = f(F, k - 1, a, l);
					for (int a_prime = 1; a_prime <= a; a_prime++) {
						if (f(F, k - 1, a - a_prime, l - c[k]) == Integer.MAX_VALUE || h(H,k,n[k]-1,a_prime) == Integer.MAX_VALUE) {
							continue;
						}
						if (F[k][a][l] > f(F, k - 1, a - a_prime, l - c[k]) + h(H,k,n[k]-1,a_prime)) {
							F[k][a][l] = f(F, k - 1, a - a_prime, l - c[k]) + h(H,k,n[k]-1,a_prime);
							F_bt[k][a][l] = a_prime;
						}
					}
				}
			}
		}
		return fp;
	}

	private int f(int[][][] F, int k, int a, int l) {
		if (k == -1 || a <= 0 || l <= 0) {
			if (a == 0 && l == 0) {
				return 0;
			}
			if (k >= 0 && a > 0 && l == 0) {
				return F[k][a][l];
			}
			return Integer.MAX_VALUE;
		}
		return F[k][a][l];
	}

	private int maxValue(int[] n_ks) {
		int max = 0;
		for (int n_k : n_ks) {
			max = Math.max(max, n_k);
		}
		return max;
	}

	private Solution extractOptimum(Data_Pack fp, Data_Pack hp, int M, int P, int C, int V, int[] imVmCount, int[] imSz, int[][] vmPr) {
		int[][][] F = fp.data;
		
		for (int a = P; a >= 0; a--) {
			for (int l = 0; l <= C; l++) {
				if (F[M - 1][a][l] <= V) {
					log.trace("Opt value: " + a + ", using " + (l) + " compartments");
					return buildSolution(fp,hp,M-1,a,l,imVmCount,imSz,vmPr);
				}
			}
		}
		return new Solution();
	}

	@SuppressWarnings("unused")
	private void printTable(int[][][] data) {
		for (int[][] is : data) {
			System.out.println("-------");
			for (int[] is2 : is) {
				String line = "";
				for (int i : is2) {
					line += i + " ";
				}
				System.out.println(line);
			}
		}
	}

	private Solution buildSolution(Data_Pack fp, Data_Pack hp,int k, int a, int l, int[] imVmCount, int[] imSz, int[][] vmPr) {
		Solution sol = new Solution();
		sol.profit = a;
		recursiveF_bt(sol.ids,fp.data_bt,k,a,l,hp.data_bt,imVmCount,imSz,vmPr);
		return sol;
	}
	
	private void recursiveF_bt(Set<Integer> ids, int[][][] F_bt, int k, int a, int l, int[][][] H_bt, int[] imVmCount,int[] imSz,int[][] vmPr) {
		log.trace("recursive_F: k = " + k + ", l = " + l + ", a = " + a);
		if (a < 0 || l < 0 || k < 0) return;
		int a_prime = F_bt[k][a][l];
		
		if (a_prime == 0) {
			recursiveF_bt(ids,F_bt,k-1,a,l,H_bt,imVmCount,imSz,vmPr);
			return;
		}
		
		int n_k = imVmCount[k] - 1;
		int c_k = imSz[k]; 
		recursiveH_bt(ids,H_bt,k,n_k,a_prime,vmPr);
		recursiveF_bt(ids,F_bt,k-1,a-a_prime,l-c_k,H_bt,imVmCount,imSz,vmPr);
	}

	private void recursiveH_bt(Set<Integer> ids,int[][][] H_bt, int k, int r, int a,	int[][] Pr) {
		log.trace("recursive_H: k = " + k + ", r = " + r + ", a = " + a);
		if (k < 0 || r < 0 || a < 0) return;
		int id = H_bt[k][r][a];
		
		if (id == 0) {
			recursiveH_bt(ids,H_bt,k,r-1,a,Pr);
			return;
		}
		
		log.trace("Adding " + id);
		ids.add(id);
		recursiveH_bt(ids,H_bt,k,r-1,a - Pr[k][r],Pr);
	}
	
	private Map<Image, List<VM>> solutionToMap(Solution sol,
			Map<Image, List<VM>> im2vms) {
		Map<Image, List<VM>> $ = new HashMap<Image, List<VM>>();
		
		List<VM> vms = flatten(im2vms.values());
		
		for (VM vm : vms) {
			if (sol.ids.contains(vm.id)) {
				if (!$.keySet().contains(vm.image)) {
					$.put(vm.image, new LinkedList<VM>());
				}
				$.get(vm.image).add(vm);
			}
		}
		
		return $;
	}

	private List<VM> flatten(Collection<List<VM>> values) {
		List<VM> vmList = new LinkedList<VM>();
		for (List<VM> vml : values) {
			vmList.addAll(vml);
		}
		return vmList;
	}
}
