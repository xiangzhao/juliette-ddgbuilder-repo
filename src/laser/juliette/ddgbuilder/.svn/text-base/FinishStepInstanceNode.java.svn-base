package laser.juliette.ddgbuilder;

import laser.ddg.AgentConfiguration;
import laser.ddg.FinishProcedureInstanceNode;
import laser.ddg.ProvenanceData;
import laser.ddg.StatusAlreadySetException;
import laser.lj.Step;

/**
 * The Finish-SIN is the last part of a PDG step which comprises one or more
 * other calls.
 * 
 * @author Sophia
 * 
 */
public class FinishStepInstanceNode extends StepInstanceNode implements
		FinishProcedureInstanceNode {

	// variables indicating the state of the Finish SIN whence the state of the
	// entire PDG step being executed
	private boolean completed; // step completed normally
	private boolean terminated; // step terminated because of an exception

	/**
	 * @param stepDef
	 * @param ac agent configuration
	 * @param provData
	 */
	public FinishStepInstanceNode(Step stepDef, AgentConfiguration ac,
			ProvenanceData provData) {
		super(stepDef, ac, provData);
	}

	/**
	 * @param stepName
	 * @param ac
	 *            agent performing the operation
	 * @param provData
	 */
	public FinishStepInstanceNode(String stepName, AgentConfiguration ac,
			ProvenanceData provData) {
		super(stepName, ac, provData);
	}

	/**
	 * Set the state of the procedure to completed
	 * 
	 * @throws StatusAlreadySetException
	 */
	public void complete() throws StatusAlreadySetException {
		if (!completed && !terminated) {
			completed = true;
		} else {
			throw new StatusAlreadySetException("Status already set");
		}
	}

	/**
	 * Check whether the state of the procedure is completed
	 */
	public boolean isCompleted() {
		return completed;
	}

	/**
	 * Check whether the state of the procedure is terminated
	 */
	public boolean isTerminated() {
		return terminated;
	}

	/**
	 * Set the state of the procedure to terminated
	 * 
	 * @throws StatusAlreadySetException
	 */

	public void terminate() throws StatusAlreadySetException {
		if (!completed && !terminated) {
			terminated = true;
		} else {
			throw new StatusAlreadySetException("Status already set");
		}
	}

	/**
	 * @see laser.ddg.AbstractProcedureInstanceNode#getType()
	 */
	@Override
	public String getType() {
		return "Finish";
	}

	@Override
	public Object getProcedureDefinition() {
		return ((StepProxy) super.getProcedureDefinition()).getWrappedStep();
	}

}
