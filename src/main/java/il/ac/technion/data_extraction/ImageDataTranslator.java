/**
 * VmImageCoupling - Software Design, 236700 - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.data_extraction;

import il.ac.technion.beans.Host;
import il.ac.technion.beans.Image;
import il.ac.technion.knapsack.Bin;
import il.ac.technion.knapsack.Item;

import java.util.Iterator;
import java.util.List;

public class ImageDataTranslator implements DataTranslator {

	@Override
	@SuppressWarnings("unchecked")
	public int[] prepareCapacitiesVector(List<?> hList) {
		List<Host> hosts = (List<Host>)hList;
		int[] binsCapacities = new int[hosts.size()];
		int i = 0;
		Iterator<Host> iter = hosts.iterator();
		while (iter.hasNext()) {
			binsCapacities[i++] = iter.next().availableStorage();
		}
		return binsCapacities;
	}

	@Override
	@SuppressWarnings("unchecked")
	public int[][] prepareSizesMatrix(int numBins, List<?> iList) {
		List<Image> images = (List<Image>)iList;
		int[][] itemSizes = new int[numBins][images.size()];
		for (int binIdx = 0; binIdx < numBins; binIdx++) {
			for (int vmIdx = 0; vmIdx < images.size(); vmIdx++) {
				itemSizes[binIdx][vmIdx] = images.get(vmIdx).size;
			}
		}
		return itemSizes;
	}

	@Override
	public double[][] prepareWeightsMatrix(List<?> hList, List<?> iList) {
		double[][] itemPrices = new double[hList.size()][iList.size()];
		for (int binIdx = 0; binIdx < hList.size(); binIdx++) {
			for (int itemIdx = 0; itemIdx < iList.size(); itemIdx++) {
				itemPrices[binIdx][itemIdx] = 1.0;
			}
		}
		return itemPrices;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void commitAssignments(Bin[] bins, List<?> hList, List<?> iList) {
		List<Host> hosts = (List<Host>)hList;
		List<Image> images = (List<Image>)iList;
		for (Bin bin : bins) {
			Host h = hosts.get(bin.id);
			for (Item item : bin.assignedItems()) {
				Image i = images.get(item.id);
				h.add(i);
			}
		}		
	}
}
