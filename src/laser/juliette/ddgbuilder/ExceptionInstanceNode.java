package laser.juliette.ddgbuilder;

import java.io.Serializable;

import laser.ddg.ProvenanceData;

/**
 * A special data object to denote an exception
 * 
 * @author B. Lerner & S. Taskova
 */
public class ExceptionInstanceNode extends DataInstanceNode {

	/**
	 * Inherit constructor of DataInstanceNode
	 * 
	 * @param val
	 *            the value being passed in the process
	 * @param n
	 *            the name of the node
	 * @param sin
	 *            the step instance node where the exception occurred, i.e. the
	 *            producer of the exception instance node
	 * @param provData
	 *            the provenance data this node belongs to
	 */
	public ExceptionInstanceNode(Serializable val, String n,
			StepInstanceNode sin, ProvenanceData provData) {
		super(val, n, sin, provData);
	}

	/**
	 * @see laser.ddg.AbstractDataInstanceNode#getType()
	 */
	@Override
	public String getType() {
		return "Exception";
	}
}
