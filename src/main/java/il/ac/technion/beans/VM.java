/**
 * VmImageProximity - Joint Research - Technion & IBM Research
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.beans;

import il.ac.technion.misc.HashCodeUtil;

public class VM {

	public final int id;
	public final Image image;
	public final int ram;

	private int fHashCode = 0;
	
	public VM(int id, Image image, int ram) {
		this.id = id;
		this.image = image;
		this.ram = ram;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof VM))
			return false;

		VM vm = (VM) obj;
		return vm.id == id && vm.image == image && vm.ram == ram;
	}
	
	@Override
	public int hashCode() {
		if (fHashCode == 0) {
			int result = HashCodeUtil.SEED;
			result = HashCodeUtil.hash(result, id);
			result = HashCodeUtil.hash(result, image);
			result = HashCodeUtil.hash(result, ram);
			fHashCode = result;
		}
		return fHashCode;
	}
	
	public String summary() {
		return "VM #" + id + " - RAM: " + ram + ", Image: " + image.id;
	}
	
	@Override
	public String toString() {
		return "VM #" + id;
	}
}
