package laser.juliette.ddgbuilder;

import java.io.Serializable;
import laser.ddg.AbstractDataInstanceNode;
import laser.ddg.ProcedureInstanceNode;
import laser.ddg.ProvenanceData;

/**
 * See comments for AbstractDataInstanceNode The actual data object
 * 
 * @author Sophia
 */
public class DataInstanceNode extends AbstractDataInstanceNode {

	/**
	 * @param val
	 *            a value to assign to the DataInstanceNode
	 * @param name
	 *            the name of the node
	 * @param producer
	 *            the call that produced the DataInstanceNode
	 * @param provData
	 *            the provenance that this node belongs to
	 */
	public DataInstanceNode(Serializable val, String name,
			ProcedureInstanceNode producer, ProvenanceData provData) {
		super(val, name, producer, provData);
	}

	/**
	 * Nullary constructor required for node to be stored by JenaBean
	 */
	public DataInstanceNode() {
		// empty
	}

	/**
	 * @param pd
	 * @return all outputs of a
	 * 
	 @Override public Set<laser.ddg.DataInstanceNode>
	 *           getProcessOutputsDerived(ProvenanceData pd) {
	 *           Set<laser.ddg.DataInstanceNode> processOutputs= new
	 *           HashSet<laser.ddg.DataInstanceNode>();
	 *           Iterator<AbstractDataInstanceNode> it=pd.outputDinIter(); while
	 *           (it.hasNext()) { DataInstanceNode currentDin;
	 *           currentDin=(laser.ddg.ljil.DataInstanceNode)
	 *           pd.outputDinIter().next(); if (currentDin.equals(this)){
	 *           processOutputs.add( currentDin); } }
	 *           Iterator<AbstractProcedureInstanceNode> it1=this.users(); while
	 *           (it1.hasNext()) { processOutputs.add (
	 *           (laser.ddg.ljil.DataInstanceNode)
	 *           it1.next().getProcessOutputsDerived() ); }
	 * 
	 *           return processOutputs; }
	 */
}
