package laser.juliette.ddg.persist;

import laser.ddg.ProcedureInstanceNode;
import laser.ddg.ProvenanceData;
import laser.juliette.ddgbuilder.DataInstanceNode;
import laser.juliette.ddgbuilder.ExceptionInstanceNode;
import laser.juliette.ddgbuilder.FinishStepInstanceNode;
import laser.juliette.ddgbuilder.FinishVirtualNode;
import laser.juliette.ddgbuilder.IntermStepInstanceNode;
import laser.juliette.ddgbuilder.IntermVirtualNode;
import laser.juliette.ddgbuilder.InterpreterBindingNode;
import laser.juliette.ddgbuilder.StartStepInstanceNode;
import laser.juliette.ddgbuilder.StartVirtualNode;
import laser.juliette.ddgbuilder.StepInstanceNode;

/**
 * This class reads provenance data from a Jena database.
 * 
 * @author Sophia. Created Jan 10, 2012.
 */

public class JenaLoader extends laser.ddg.persist.JenaLoader {
	public JenaLoader(String dir) {
		super (dir);
	}

	/**
	 * Create the appropriate type of data instance node for this resource
	 * and add it to the provenance data
	 * 
	 * @param name The parameter name 
	 * @param type The type of node
	 * @param val The data value
	 * @param pd  The provenance data
	 * @return the node created
	 */
	@Override
	protected DataInstanceNode createDataInstanceNode(String name,
			String type, String val, ProvenanceData pd, ProcedureInstanceNode pin) {
		DataInstanceNode din = null;
		if (type.equals("Data")) {
			din = new DataInstanceNode(val, name, pin, pd);
		} else if (type.equals("Exception")) {
			// fix parameters - find sin that output the exception
			din = new ExceptionInstanceNode(val, name, (StepInstanceNode) pin, pd);
		} else {
			assert false;
		}
		return din;
	}

	protected ProcedureInstanceNode createProcedureInstanceNode(String name,
			String type, ProvenanceData provData) {

		ProcedureInstanceNode sin = null;
		if (type.equals("Start")) {
			sin = new StartStepInstanceNode(name, null, provData);
		} else if (type.equals("Interm")) {
			sin = new IntermStepInstanceNode(name, null, provData);
		} else if (type.equals("Finish")) {
			sin = new FinishStepInstanceNode(name, null, provData);
		} else if (type.equals("Leaf")) {
			sin = new StepInstanceNode(name, null, provData);
		} else if (type.equals("VStart")) {
			sin = new StartVirtualNode(name, null, provData);
		} else if (type.equals("VInterm")) {
			sin = new IntermVirtualNode(name, null, provData);
		} else if (type.equals("VFinish")) {
			sin = new FinishVirtualNode(name, null, provData);
		} else if (type.equals("Binding")) {
			sin = new InterpreterBindingNode(name, provData);
		}

		else {
			assert false;
		}

		return sin;
	}

}
