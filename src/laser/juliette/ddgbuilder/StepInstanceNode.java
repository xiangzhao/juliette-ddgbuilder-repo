package laser.juliette.ddgbuilder;

import laser.ddg.AbstractProcedureInstanceNode;
import laser.ddg.AgentConfiguration;
import laser.ddg.ProvenanceData;
import laser.lj.Step;

/**
 * Information about the execution of a single step instance node
 * 
 * @author Sophia
 */
public class StepInstanceNode extends AbstractProcedureInstanceNode {

	/**
	 * @param stepDef
	 * @param ac
	 *            agent performing the operation
	 * @param provData
	 */
	public StepInstanceNode(StepProxy stepDef, AgentConfiguration ac, ProvenanceData provData) {
		super(stepDef.getName(), stepDef, ac, provData);
	}
	
	/**
	 * @param stepName
	 * @param ac
	 *            agent performing the operation
	 * @param provData
	 */
	public StepInstanceNode(String stepName, AgentConfiguration ac,
			ProvenanceData provData) {
		super(stepName, null, ac, provData);
	}

	/**
	 * @param stepDef
	 * @param ac
	 * @param provData
	 */
	public StepInstanceNode(Step stepDef, AgentConfiguration ac,
			ProvenanceData provData) {
		super (stepDef.getName(), new StepReference(stepDef), ac, provData);
	}

	/**
	 * @see laser.ddg.AbstractProcedureInstanceNode#getType()
	 */
	public String getType() {
		return "Leaf";
	}

	@Override
	public boolean canBeRoot() {
		return true;
	}
	
}
