/**
 * VmImageCoupling - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.tdvp;

import il.ac.technion.tdvp.TDVP.Solution;

public class MultiTDVP {
	
	private TDVP tdvp = new TDVP();
	
	public class MtdvpSolution {
		public int profit = 0;
		public Solution[] assignments;
		
		public MtdvpSolution(Solution[] assignments) {
			this.assignments = assignments;
			for (Solution solution : assignments) {
				this.profit += solution.profit;
			}
		}
	}
	
	public MtdvpSolution solve(int[] vArr, int[] cArr, int[][] vmSz, int[][] vmPr, int[][] imSz, int[] imVmCount, int[][] ids) {
		Solution[] assignments = new Solution[vArr.length];
		
		for (int hid = 0; hid < vArr.length; hid++) {
			assignments[hid] = tdvp.solve(vArr[hid], cArr[hid], vmSz, vmPr, imSz[hid], imVmCount, ids);
			clean(vmSz, vmPr, imVmCount, ids, assignments[hid], hid);
		}
		return new MtdvpSolution(assignments);
	}

	private void clean(int[][] vmSz, int[][] vmPr, int[] imVmCount,
			int[][] ids, Solution tdvpSol, int hid) {

		for (int id : tdvpSol.ids) {
			boolean found = false;
			for (int mi = 0; mi < ids.length && !found; mi++) {
				for (int vi = 0; vi < ids[mi].length; vi++) {
					if (id == ids[mi][vi]) {
						swap(vmSz[mi], vi, imVmCount[mi] - 1);
						swap(vmPr[mi], vi, imVmCount[mi] - 1);
						swap(ids[mi], vi, imVmCount[mi] - 1);
						imVmCount[mi]--;
						
						found = true;
						break;
					}
				}
			}
		}
	}
	
	public void swap(int[] arr, int i, int j) {
		if (i == j) return;
		arr[i] = arr[i] + arr[j];
		arr[j] = arr[i] - arr[j];
		arr[i] = arr[i] - arr[j];
	}
}
