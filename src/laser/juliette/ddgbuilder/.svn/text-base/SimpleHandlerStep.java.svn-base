package laser.juliette.ddgbuilder;

import laser.lj.Step;

/**
 * This class allows us to associate "steps" with simple handler nodes in a DFG.
 * Simple handler nodes correspond to simple exception handlers, that is,
 * handlers with no attached step.
 * 
 * @author Barbara Lerner (edited by Sophia)
 * @version Mar 14, 2011
 * 
 */
public class SimpleHandlerStep implements StepProxy {

	/**
	 * @return name that was created for this step
	 */
	public String getName() {
		return "simple handler";
	}

	/**
	 * @return null
	 */
	public Step getWrappedStep() {
		return null;
	}
}
