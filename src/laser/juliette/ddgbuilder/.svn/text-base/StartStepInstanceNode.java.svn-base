package laser.juliette.ddgbuilder;

import laser.ddg.AgentConfiguration;
import laser.ddg.ProvenanceData;
import laser.ddg.StartProcedureInstanceNode;
import laser.lj.Step;

/**
 * The Start SIN is the first part of a PDG step that comprises any number of
 * other calls (including zero).
 * 
 * @author Sophia
 * 
 */
public class StartStepInstanceNode extends StepInstanceNode implements
		StartProcedureInstanceNode {

	/**
	 * @param stepDef
	 * @param ac
	 *            agent performing the operation
	 * @param provData
	 *            the provenance data this node belongs to
	 */
	public StartStepInstanceNode(Step stepDef, AgentConfiguration ac,
			ProvenanceData provData) {
		super(stepDef, ac, provData);
	}

	/**
	 * @param stepName
	 * @param ac
	 *            agent performing the operation
	 * @param provData
	 */
	public StartStepInstanceNode(String stepName, AgentConfiguration ac,
			ProvenanceData provData) {
		super(stepName, ac, provData);
	}

	/**
	 * @see laser.ddg.AbstractProcedureInstanceNode#getType()
	 */
	@Override
	public String getType() {
		return "Start";
	}

	@Override
	public Object getProcedureDefinition() {
		return ((StepProxy) super.getProcedureDefinition()).getWrappedStep();
	}

}
