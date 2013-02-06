package laser.juliette.ddgbuilder;

import laser.lj.Step;

/**
 * @author B. Lerner (edited by Sophia)
 * 
 */
public interface StepProxy {
	/**
	 * @return step name
	 */
	public String getName();

	/**
	 * @return Step
	 */
	public Step getWrappedStep();
}
