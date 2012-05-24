/**
 * VmImageProximity - Joint Research - Technion & IBM Research
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.beans;

import il.ac.technion.misc.HashCodeUtil;

import java.util.HashSet;
import java.util.Set;

public class Image {

	public final String id;
	public final int size;
	public final String description;
	
	private Set<Host> replicaLocations = new HashSet<Host>();

	private int fHashCode = 0;

	public Image(String id, int size, String desc) {
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
		return img.id.equals(id) && img.size == size && img.description.equals(description);
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

	public void addedTo(Host host) {
		replicaLocations.add(host);
	}

	public void removedFrom(Host host) {
		replicaLocations.remove(host);
	}
	
	public int numReplicas() {
		return replicaLocations.size();
	}
}
