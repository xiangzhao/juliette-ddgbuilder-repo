package laser.juliette.ddgbuilder;

import laser.lj.Step;

/**
 * This class allows us to associate "steps" with virtual nodes in a DFG.
 * Virtual nodes correspond to the grouping together of a step and its
 * requisites. As a result, there is no corresponding step in the Little-JIL
 * diagram.
 * 
 * @author Barbara Lerner
 * @version Mar 14, 2011
 * 
 */
public class VirtualStep implements StepProxy {
	// The step that has requisites attached to it
	private final Step stepBeingWrapped;

	/**
	 * Creates a "virtual" step to group together a pre/postrequisite with the
	 * step it is a requisite for as a pseudo-sequential step.
	 * 
	 * @param virtualChild
	 *            the step being wrapped
	 */
	public VirtualStep(Step virtualChild) {
		stepBeingWrapped = virtualChild;
	}

	/**
	 * @return name that was created for this step
	 */
	public String getName() {
		return "virtual";
	}

	/**
	 * @return the step that has requisites
	 */
	public Step getWrappedStep() {
		return stepBeingWrapped;
	}
}
