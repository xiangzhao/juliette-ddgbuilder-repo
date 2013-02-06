package laser.juliette.ddgbuilder;

import laser.ddg.AgentConfiguration;
import laser.ddg.FinishProcedureInstanceNode;
import laser.ddg.ProvenanceData;
import laser.ddg.StatusAlreadySetException;
import laser.lj.Step;

/**
 * The Finish Virtual node is the last part of a PDG step which comprises one or
 * more other calls.
 * 
 * @author Sophia
 * 
 */
public class FinishVirtualNode extends Virtual implements
		FinishProcedureInstanceNode {

	// variables indicating the state of the Finish Virtual Node whence the
	// state of the
	// entire PDG step being executed
	private boolean completed; // step completed normally
	private boolean terminated; // step terminated because of an exception

	/**
	 * @param stepDef
	 * @param ac
	 * @param provData
	 *            the provenance data this node belongs to
	 */
	public FinishVirtualNode(Step stepDef, AgentConfiguration ac,
			ProvenanceData provData) {
		super(stepDef, ac, provData);
	}
	/**
	 * @param stepName
	 * @param ac
	 *            agent performing the operation
	 * @param provData
	 */
	public FinishVirtualNode(String stepName, AgentConfiguration ac,
			ProvenanceData provData) {
		super(stepName, ac, provData);
	}
	
	/**
	 * Set the state of the procedure to completed
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
		return "VFinish";
	}

}
