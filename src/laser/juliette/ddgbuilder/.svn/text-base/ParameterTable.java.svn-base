package laser.juliette.ddgbuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import laser.ddg.ProcedureInstanceNode;

/**
 * @author B. Lerner (edited by Sophia)
 * 
 */
public class ParameterTable {
	private Map<Serializable, Serializable> copiedFrom
		= new HashMap<Serializable, Serializable>();
	
	private Map<Serializable, ProcedureInstanceNode> writers
		= new HashMap<Serializable, ProcedureInstanceNode>();
	
	private Map<Serializable, DataInstanceNode> originals
		= new HashMap<Serializable, DataInstanceNode>();

	/**
	 * Add a binding from one object to another.
	 * @param from the object being copied 
	 * @param to the copy of the object
	 */
	public void bind(Serializable from, Serializable to) {
		/*LinkedList<Serializable> copies = bindings.get(from);
		if (copies == null) {
			copies = new LinkedList<Serializable>();
			bindings.put(from, copies);
		}
		copies.add(to);*/
		copiedFrom.put(to, from);
	}
	
	/**
	 * @param param
	 * @param writer
	 */
	public void setWriter (Serializable param, ProcedureInstanceNode writer) {
		writers.put(param, writer);
	}

	/**
	 * @param param
	 * @param original
	 */
	public void setOriginal(Serializable param, DataInstanceNode original) {
		originals.put(param, original);
	}

	/**
	 * @param parameter
	 * @return last writer PIN
	 */
	public ProcedureInstanceNode getLastWriter(Serializable parameter) {
		ProcedureInstanceNode writer = writers.get(parameter);
		Serializable copy = parameter;
		
		while (writer == null) {
			copy = copiedFrom.get(copy);
			if (copy == null) {
				assert false;
				return null;
			}
			writer = writers.get(copy);
		}
		
		return writer;
	}

	/**
	 * @param parameter
	 * @return original DIN
	 */
	public DataInstanceNode getOriginal(Serializable parameter) {
		DataInstanceNode original = originals.get(parameter);
		Serializable copy = parameter;
		
		while (original == null) {
			copy = copiedFrom.get(copy);
			if (copy == null) {
				return null;
			}
			original = originals.get(copy);
		}
		
		return original;
	}

}
