package laser.juliette.ddgbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import laser.juliette.ams.AgendaItem;

/**
 * Hash map from Agenda Items (i.e. procedure instances) to Procedure Instance
 * Nodes
 * 
 * @author Sophia
 * 
 */
public class ParentItemToChildSin {
	private Map<AgendaItem, ArrayList<StepInstanceNode>> parentToChild
		= new HashMap<AgendaItem, ArrayList<StepInstanceNode>>();

	private Map<StepInstanceNode, StepInstanceNode> childToParent
		= new HashMap<StepInstanceNode, StepInstanceNode>();

	/**
	 * @param child
	 *            the ProcedureInstanceNode
	 * @param parentItem
	 * @param parentNode
	 */
	public void addChild(AgendaItem parentItem, StepInstanceNode parentNode,
			StepInstanceNode child) {
		// System.out.println("Adding child");
		// Thread.dumpStack();
		if (!parentToChild.containsKey(parentItem)) { // if agenda item is not
														// yet on the map
			parentToChild.put(parentItem, new ArrayList<StepInstanceNode>());
		}
		parentToChild.get(parentItem).add(child); // add PIN to collection of
													// PINs corresponding
		// to Agenda Item
		childToParent.put(child, parentNode);
	}

	/**
	 * @param parent
	 *            The agenda item of interest
	 * @return Procedure Instance Node corresponding to a given Agenda Item
	 * @throws NoSuchAgendaItemException
	 *             if the Agenda Item passed to the method is nonexistent
	 */
	public Iterator<StepInstanceNode> getChildPINS(AgendaItem parent) {
		if (parentToChild.containsKey(parent)) {
			return parentToChild.get(parent).iterator();
		} else {
			throw new NoSuchAgendaItemException("No such agenda item:  "
					+ parent);
		}
	}

	/**
	 * Returns the last PIN associated with any children of this agenda item
	 * 
	 * @param parent
	 *            the agenda item
	 * @return the last PIN for this agenda item. Returns null if there are no
	 *         child pins for this agenda item.
	 */
	public StepInstanceNode getLastChildPIN(AgendaItem parent) {
		if (parentToChild.containsKey(parent)) {
			ArrayList<StepInstanceNode> pins = parentToChild.get(parent);
			return pins.get(pins.size() - 1);
		} else {
			return null;
		}
	}

	/**
	 * @param thrower
	 * @return parent PIN
	 */
	public StepInstanceNode getParentPin(StepInstanceNode thrower) {
		return childToParent.get(thrower);
	}

	/**
	 * @param parent
	 * @return string representation of child PIN
	 */
	public String childPINSToString(AgendaItem parent) {
		StringBuilder children = new StringBuilder();
		children.append("Children: ");
		Iterator<StepInstanceNode> childPINs = getChildPINS(parent);
		while (childPINs.hasNext()) {
			children.append(childPINs.next().getId() + " ");
		}
		return children.toString();
	}

}
