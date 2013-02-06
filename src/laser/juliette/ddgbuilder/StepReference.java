package laser.juliette.ddgbuilder;

import laser.lj.Step;

/**
 * @author B. Lerner (edited by Sophia)
 * 
 */
public class StepReference implements StepProxy {
	private Step stepDef;

	/**
	 * Empty constructor required for class to be a java bean
	 */
	public StepReference() {
		// empty
	}

	/**
	 * @param stepDef
	 */
	public StepReference(Step stepDef) {
		this.stepDef = stepDef;
	}

	public Step getStepDef() {
		return stepDef;
	}

	public void setStepDef(Step stepDef) {
		this.stepDef = stepDef;
	}

	public String getName() {
		return stepDef.getName();
	}

	public Step getWrappedStep() {
		return stepDef;
	}
}
