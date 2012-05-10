/**
 * Greedy_Recovery - Technion, Israel Institute of Technology
 * 
 * Author: Assaf Israel, 2012
 * Created: 7 באפר 2012
 */
package il.ac.technion.data_extraction;

import il.ac.technion.beans.Host;
import il.ac.technion.beans.VM;
import il.ac.technion.knapsack.Bin;
import il.ac.technion.knapsack.Item;

import java.util.Iterator;
import java.util.List;

public class VmDataTranslator implements DataTranslator {
	
	@Override
	@SuppressWarnings("unchecked")
	public double[][] prepareWeightsMatrix(List<?> hList, List<?> vList) {
		List<Host> hosts = (List<Host>)hList;
		List<VM> vms = (List<VM>)vList;
		double[][] itemPrices = new double[hosts.size()][vms.size()];
		for (int binIdx = 0; binIdx < hosts.size(); binIdx++) {
			Host h = hosts.get(binIdx);
			for (int vmIdx = 0; vmIdx < vms.size(); vmIdx++) {
				VM vm = vms.get(vmIdx);
				itemPrices[binIdx][vmIdx] = h.colocated(vm) ? 1.0 : 0.0;
			}
		}
		return itemPrices;
	}

	@Override
	@SuppressWarnings("unchecked")
	public int[][] prepareSizesMatrix(int numBins, List<?> vList) {
		List<VM> vms = (List<VM>)vList;
		int[][] itemSizes = new int[numBins][vms.size()];
		for (int binIdx = 0; binIdx < numBins; binIdx++) {
			for (int vmIdx = 0; vmIdx < vms.size(); vmIdx++) {
				itemSizes[binIdx][vmIdx] = vms.get(vmIdx).ram;
			}
		}
		return itemSizes;
	}

	@Override
	@SuppressWarnings("unchecked")
	public int[] prepareCapacitiesVector(List<?> hList) {
		List<Host> hosts = (List<Host>)hList;
		int[] binsCapacities = new int[hosts.size()];
		int i = 0;
		Iterator<Host> iter = hosts.iterator();
		while (iter.hasNext()) {
			binsCapacities[i++] = iter.next().availableRAM();
		}
		return binsCapacities;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void commitAssignments(Bin[] bins, List<?> hList, List<?> vList) {
		List<Host> hosts = (List<Host>)hList;
		List<VM> vms = (List<VM>)vList;
		for (Bin bin : bins) {
			Host h = hosts.get(bin.id);
			for (Item item : bin.assignedItems()) {
				VM v = vms.get(item.id);
				h.add(v);
			}
		}		
	}
}
