/**
 * VmImageProximity - Joint Research - Technion & IBM Research
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.beans;

import il.ac.technion.misc.HashCodeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Host {
	public final int id;
	public final int ramCapacity;
	public final int storageCapacity;

	private Map<Image, Integer> images = new HashMap<Image, Integer>();
	private Map<Image, List<VM>> imageVms = new HashMap<Image, List<VM>>();
	private Set<VM> vms = new HashSet<VM>();

	private int totalMemSize = 0;
	private int totalStorage = 0;

	private int fHashCode = 0;

	public Host(int id, int vmCapacity, int imageCapacity) {
		this.id = id;
		this.ramCapacity = vmCapacity;
		this.storageCapacity = imageCapacity;
	}

	public int availableRAM() {
		return ramCapacity - totalMemSize;
	}
	
	public int availableStorage() {
		return storageCapacity - totalStorage;
	}
	
	public int numVMs() {
		return vms.size();
	}
	
	public int numImages() {
		return images.keySet().size();
	}
	public boolean add(Image im) {
		if (images.containsKey(im)) return true;
		
		if (!canAdd(im)) {
			return false;
		}
		
		images.put(im, 0);
		imageVms.put(im, new ArrayList<VM>(totalMemSize));
		totalStorage += im.size;
		im.addedTo(this);
		return true;
	}
	
	public boolean add(VM vm) {
		if (vms.contains(vm.id)) {
			return true;
		}
		
		if (availableRAM() < vm.ram || !add(vm.image)) {
			return false;
		}
		
		images.put(vm.image, images.get(vm.image) + 1);
		imageVms.get(vm.image).add(vm);
		vms.add(vm);
		totalMemSize += vm.ram;
		return true;
	}

	public List<VM> remove(Image im) {
		if (!images.containsKey(im)) {
			return new ArrayList<VM>();
		}

		images.remove(im);
		List<VM> $ = imageVms.remove(im);
		for (VM vm : $) {
			totalMemSize -= vm.ram;
			vms.remove(vm);
		}
		totalStorage -= im.size;
		im.removedFrom(this);
		return $;
	}

	public boolean remove(VM vm) {
		if (!vms.contains(vm)) {
			return false;
		}

		vms.remove(vm);
		totalMemSize -= vm.ram;

		if (images.get(vm.image) == 1) {
			images.remove(vm.image);
			imageVms.remove(vm.image);
			totalStorage -= vm.image.size;
		} else {
			images.put(vm.image, images.get(vm.image) - 1);
		}

		return true;
	}

	public Collection<Image> images() {
		return images.keySet();
	}
	
	public Collection<VM> vms() {
		return vms;
	}
	
	public Map<Image, List<VM>> assignment() {
		Map<Image, List<VM>> $ = new HashMap<Image, List<VM>>();
		for (Image im : imageVms.keySet()) {
			List<VM> l = new ArrayList<VM>(imageVms.get(im));
			$.put(im, l);
		}
		return $;
	}
	
	public boolean canAdd(VM vm) {
		if (availableRAM() < vm.ram ) {
			return false;
		}

		if (!images.containsKey(vm.image) && canAdd(vm.image)) {
			return false;
		}
		return true;
	}

	public boolean canAdd(Image im) {
		return availableStorage() >= im.size;
	}

	public boolean colocated(VM vm) {
		return images.containsKey(vm.image);
	}
	
	public String description() {
		StringBuilder dd = new StringBuilder(summary() + " VMs: ");
		for (VM vm : vms) {
			dd.append(vm + " ");
		}
		dd.append(", Images: ");
		for (Image im : images.keySet()) {
			dd.append("Im #" + im.id + " ");
		}
		return dd.toString();
	}

	public String summary() {
		return "Host #" + id + " - RAM: " + totalMemSize + "/" + ramCapacity
				+ ", Storage: " + totalStorage + "/" + storageCapacity;
	}

	@Override
	public String toString() {
		return "Host #" + id;
	}

	@Override
	public int hashCode() {
		if (fHashCode == 0) {
			int result = HashCodeUtil.SEED;
			result = HashCodeUtil.hash(result, ramCapacity);
			result = HashCodeUtil.hash(result, storageCapacity);
			result = HashCodeUtil.hash(result, images);
			result = HashCodeUtil.hash(result, imageVms);
			result = HashCodeUtil.hash(result, vms);
			result = HashCodeUtil.hash(result, totalMemSize);
			result = HashCodeUtil.hash(result, totalStorage);
			fHashCode = result;
		}
		return fHashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof Host))
			return false;

		Host aHost = (Host) obj;
		return aHost.id == id && aHost.ramCapacity == ramCapacity && 
			aHost.storageCapacity == storageCapacity && 
			aHost.images.equals(images) && aHost.vms.equals(vms);
	}

	public boolean contains(Image im) {
		return images.containsKey(im);
	}
	
	public boolean contains(VM vm) {
		return vms.contains(vm);
	}

	public void reset() {
		while (!images.isEmpty()) {
			remove(images.keySet().iterator().next());
		}
	}
}
