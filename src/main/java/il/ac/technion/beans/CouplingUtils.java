/**
 * VmImageCoupling - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.beans;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CouplingUtils {
	public static int[][] vmIds(int M, int max_Nk, List<Image> images, Map<Image, List<VM>> im2vms) {
		int[][] ids = new int[M][max_Nk];
		Iterator<Image> iter = images.iterator();
		for (int k = 0; k < M; k++) {
			Image im = iter.next();
			for (int r = 0; r < im2vms.get(im).size(); r++) {
				ids[k][r] = im2vms.get(im).get(r).id;
			}
		}
		return ids;
	}
	
	public static int[][] vmSizes(int M, int max_Nk, List<Image> images, Map<Image, List<VM>> im2vms) {
		int[][] sizes = new int[M][max_Nk];
		Iterator<Image> iter = images.iterator();
		for (int k = 0; k < M; k++) {
			Image im = iter.next();
			for (int r = 0; r < im2vms.get(im).size(); r++) {
				sizes[k][r] = im2vms.get(im).get(r).size();
			}
		}
		return sizes;
	}

	public static int[][] vmProfits(int M, int max_Nk, Map<Image, List<VM>> im2vms) {
		int[][] profits = new int[M][max_Nk];
		for (int k = 0; k < M; k++) {
			for (int r = 0; r < max_Nk; r++) {
				profits[k][r] = 1; // Profit == # VMs in Knapsack
			}
		}
		return profits;
	}

	public static int[] imageSizes(Set<Image> images) {
		int[] sizes = new int[images.size()];
		Iterator<Image> iter = images.iterator();
		for (int i = 0; i < sizes.length; i++) {
			sizes[i] = iter.next().size;
		}
		return sizes;
	}

	public static int[] imageVmsCount(List<Image> images, Map<Image, List<VM>> im2vms) {
		int[] vmCounts = new int[im2vms.keySet().size()];
		Iterator<Image> iter = images.iterator();
		for (int i = 0; i < vmCounts.length; i++) {
			vmCounts[i] = im2vms.get(iter.next()).size();
		}
		return vmCounts;
	}

	public static int[][] imageSizesPerHost(List<Host> hosts, List<Image> images) {
		int[][] $ = new int[hosts.size()][images.size()];
		int i = 0;
		for (Host host : hosts) {
			int j = 0;
			for (Image im : images) {
				$[i][j++] = host.contains(im) ? 0 : im.size;
			}
			i++;
		}
		return $;
	}
}
