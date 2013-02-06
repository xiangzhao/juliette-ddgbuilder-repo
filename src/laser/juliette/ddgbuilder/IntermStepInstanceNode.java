package laser.juliette.ddgbuilder;

import laser.ddg.AgentConfiguration;
import laser.ddg.IntermediateProcedureInstanceNode;
import laser.ddg.ProvenanceData;
import laser.lj.Step;

/**
 * The Intermediate SIN is a part of a PDG step which comprises one or more
 * other calls. The SIN is neither the first nor the last part of the larger
 * procedure
 * 
 * @author Sophia
 * 
 */
public class IntermStepInstanceNode extends StepInstanceNode implements
		IntermediateProcedureInstanceNode {

	/**
	 * @param stepDef
	 * @param ac
	 *            agent that performs the operation represented by the SIN
	 * @param provData
	 *            the provenance data this node belongs to
	 */
	public IntermStepInstanceNode(Step stepDef, AgentConfiguration ac,
			ProvenanceData provData) {
		super(stepDef, ac, provData);

	}
	/**
	 * @param stepName
	 * @param ac
	 *            agent performing the operation
	 * @param provData
	 */
	public IntermStepInstanceNode(String stepName, AgentConfiguration ac,
			ProvenanceData provData) {
		super(stepName, ac, provData);
	}
	/**
	 * @see laser.ddg.AbstractProcedureInstanceNode#getType()
	 */
	@Override
	public String getType() {
		return "Interm";
	}

	@Override
	public Object getProcedureDefinition() {
		return ((StepProxy) super.getProcedureDefinition()).getWrappedStep();
	}

}
