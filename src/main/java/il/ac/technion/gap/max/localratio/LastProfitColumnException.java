/**
 * Greedy_Recovery - Technion, Israel Institute of Technology
 * 
 * Author: Assaf Israel, 2012
 * Created: 12/03/2012
 */
package il.ac.technion.gap.max.localratio;

import il.ac.technion.gap.GAP_Exception;

public class LastProfitColumnException extends GAP_Exception {

	public LastProfitColumnException() {
		super();
	}
	
	public LastProfitColumnException(Exception e) {
		super(e);
	}

	private static final long serialVersionUID = -2296365254942471138L;
}
