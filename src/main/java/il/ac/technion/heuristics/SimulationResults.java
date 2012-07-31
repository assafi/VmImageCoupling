/**
 * VmImageCoupling - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.heuristics;

import il.ac.technion.beans.Host;

import java.util.ArrayList;
import java.util.List;

public class SimulationResults {
	/**
	 * 
	 */
	private static final String DELIM = ",";
	public int imageCount;
	public int totalVMCount;
	public int redundancy;
	public int greedyImprovement;
	public int localCount;
	public int remoteCount;
	public boolean feasiblePacking;
	public List<Host> placement = new ArrayList<Host>(0);
	
	/**
	 * @param imageCount
	 * @param totalVMCount
	 * @param imageRedundancy
	 */
	public SimulationResults(int imageCount, int vmCount, int redundancy) {
		this.imageCount = imageCount;
		this.totalVMCount = vmCount;
		this.redundancy = redundancy;
	}

	@Override
	public String toString() {
		if (feasiblePacking) {
			return "Image count: " + imageCount + ", " +
					"Image redundancy: " + redundancy + ", " +
					"Total VM count: " + totalVMCount + ", " +
					"Improvemnt: " + greedyImprovement + ", " +
					"Local VM count: " + localCount + ", " +
					"Remote VM count: " + remoteCount;
		} 
		return "Image count: " + imageCount + ", " +
			"Total VM count: " + totalVMCount + ". No feasible packing.";
	}
	
	public String toCsv() {
		return feasiblePacking + DELIM + imageCount + DELIM + redundancy + DELIM
				+ totalVMCount + DELIM + greedyImprovement + DELIM + localCount + DELIM
				+ remoteCount;
	}
}
