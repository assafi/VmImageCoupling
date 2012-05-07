/**
 * VmImageProximity - Joint Research - Technion & IBM Research
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.beans;

import il.ac.technion.misc.HashCodeUtil;

public class Image {

	public final int id;
	public final int size;
	public final String description;

	private int fHashCode = 0;

	public Image(int id, int size, String desc) {
		this.id = id;
		this.size = size;
		this.description = desc;
	}

	@Override
	public int hashCode() {
		if (fHashCode == 0) {
			int result = HashCodeUtil.SEED;
			result = HashCodeUtil.hash(result, id);
			result = HashCodeUtil.hash(result, size);
			result = HashCodeUtil.hash(result, description);
			fHashCode = result;
		}
		return fHashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof Image))
			return false;

		Image img = (Image) obj;
		return img.id == id && img.size == size && img.description == description;
	}
	
	public String description() {
		return summary() + ", description: " + description;
	}
	
	public String summary() {
		return "Image #" + id + ", Size: " + size;
	}
	
	@Override
	public String toString() {
		return "Image #" + id;
	}
}
