/**
 * VmImageCoupling - Software Design, 236700 - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.configuration;

public class ConfigurationException extends Exception {

	private static final long serialVersionUID = -3094232569220931348L;

	public ConfigurationException() {
	}

	public ConfigurationException(String arg0) {
		super(arg0);
	}

	public ConfigurationException(Throwable arg0) {
		super(arg0);
	}

	public ConfigurationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ConfigurationException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}
}
