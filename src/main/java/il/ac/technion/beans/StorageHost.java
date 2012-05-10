/**
 * VmImageCoupling - Software Design, 236700 - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.beans;

import il.ac.technion.data_extraction.Binable;

public class StorageHost implements Binable {

	private final Host host;
	
	public StorageHost(Host host) {
		this.host = host;
	}
	
	@Override
	public int capacity() {
		return host.availableStorage();
	}
}
