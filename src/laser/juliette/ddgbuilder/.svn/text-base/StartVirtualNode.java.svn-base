package laser.juliette.ddgbuilder;

import laser.ddg.AgentConfiguration;
import laser.ddg.ProvenanceData;
import laser.ddg.StartProcedureInstanceNode;
import laser.lj.Step;

/**
 * The Start Virtual Node is the first part of a PDG step that comprises any
 * number of other calls (including zero).
 * 
 * @author Sophia
 * 
 */
public class StartVirtualNode extends Virtual implements
		StartProcedureInstanceNode {

	/**
	 * @param stepDef
	 * @param ac
	 *            agent performing the operation
	 * @param provData
	 *            the provenance data this node belongs to
	 */
	public StartVirtualNode(Step stepDef, AgentConfiguration ac,
			ProvenanceData provData) {
		super(stepDef, ac, provData);
	}

	/**
	 * @param stepName
	 * @param ac
	 *            agent performing the operation
	 * @param provData
	 */
	public StartVirtualNode(String stepName, AgentConfiguration ac,
			ProvenanceData provData) {
		super(stepName, ac, provData);
	}
	/**
	 * @see laser.ddg.AbstractProcedureInstanceNode#getType()
	 */
	@Override
	public String getType() {
		return "VStart";
	}
}
