package laser.juliette.ddgbuilder;

import laser.ddg.AbstractProcedureInstanceNode;
import laser.ddg.ProvenanceData;

public class InterpreterBindingNode extends AbstractProcedureInstanceNode {
	public InterpreterBindingNode(String binding, ProvenanceData provData) {
		super(binding, null, null, provData);
	}


	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "Binding";
	}


	@Override
	public boolean canBeRoot() {
		return false;
	}

}
