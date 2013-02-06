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
public class AgendaItemToStepInstanceNode {
	private Map<AgendaItem, ArrayList<StepInstanceNode>> aiToPin
		= new HashMap<AgendaItem, ArrayList<StepInstanceNode>>();

	private Map<StepInstanceNode, AgendaItem> nodeToItem
		= new HashMap<StepInstanceNode, AgendaItem>();

	/**
	 * @param pin
	 *            the ProcedureInstanceNode
	 * @param ai
	 */
	public void connect(AgendaItem ai, StepInstanceNode pin) {
		if (!aiToPin.containsKey(ai)) { // if agenda item is not yet on the map
			aiToPin.put(ai, new ArrayList<StepInstanceNode>());
		}
		aiToPin.get(ai).add(pin); // add PIN to collection of PINs corresponding
									// to Agenda Item
		nodeToItem.put(pin, ai);
	}

	/**
	 * @param ai
	 *            The agenda item of interest
	 * @return Procedure Instance Node corresponding to a given Agenda Item
	 * @throws NoSuchAgendaItemException
	 *             if the Agenda Item passed to the method is nonexistent
	 */
	public Iterator<StepInstanceNode> getPINS(AgendaItem ai) {
		if (aiToPin.containsKey(ai)) {
			return aiToPin.get(ai).iterator();
		} else {
			throw new NoSuchAgendaItemException("No such agenda item:  " + ai);
		}
	}

	/**
	 * Returns the last PIN associated with this agenda item
	 * 
	 * @param ai
	 *            the agenda item
	 * @return the last PIN for this agenda item. Return null if there are no
	 *         PINs for this agenda item.
	 */
	public StepInstanceNode getLastPIN(AgendaItem ai) {
//		try {
//			System.out.println("Looking up agenda item " + ai.getStep().getName());
//		} catch (AMSException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		if (aiToPin.containsKey(ai)) {
			ArrayList<StepInstanceNode> pins = aiToPin.get(ai);
			return pins.get(pins.size() - 1);
		} else {
			return null;
		}
	}

	/**
	 * @param ai
	 *            Agenda Item of interest
	 * @param pin
	 *            Procedure Instance node of interest
	 * @return true if the collection of PINs corresponding to an Agenda Item
	 *         contains a given PIN
	 */
	public boolean mapContainsPinAtAgendaItem(AgendaItem ai,
			StepInstanceNode pin) {
		return aiToPin.get(ai).contains(pin);
	}

	/**
	 * @param ai
	 *            Agenda Item of interest
	 * @return true if the map from Agenda Items to PINs contains a given Agenda
	 *         Item
	 */
	public boolean containsAI(AgendaItem ai) {
		return aiToPin.containsKey(ai);
	}

	/**
	 * Returns the agenda item that corresponds to a step instance node
	 * 
	 * @param node
	 *            the node whose agenda item we want
	 * @return the agenda item that corresponds to a step instance node
	 */
	public AgendaItem getItem(StepInstanceNode node) {
		return nodeToItem.get(node);
	}

}
