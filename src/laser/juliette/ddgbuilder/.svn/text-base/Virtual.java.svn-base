package laser.juliette.ddgbuilder;

import laser.ddg.AgentConfiguration;
import laser.ddg.ProvenanceData;
import laser.lj.Step;

/**
 * Used as the parent of any process that has prerequisites and/or
 * post-requisites (the children of the Virtual node are the process node itself
 * and the nodes that represent the execution of the pre- and/or post-requisite
 * checks)
 */
public class Virtual extends StepInstanceNode {
	/**
	 * @param stepDef
	 * @param ac
	 *            - the agent that performs the operation
	 * @param provData
	 *            the provenance data this node belongs to
	 */
	public Virtual(Step stepDef, AgentConfiguration ac, ProvenanceData provData) {
		super(new VirtualStep(stepDef), ac, provData);

	}
	/**
	 * @param stepName
	 * @param ac
	 *            agent performing the operation
	 * @param provData
	 */
	public Virtual(String stepName, AgentConfiguration ac,
			ProvenanceData provData) {
		super(stepName, ac, provData);
	}
}
