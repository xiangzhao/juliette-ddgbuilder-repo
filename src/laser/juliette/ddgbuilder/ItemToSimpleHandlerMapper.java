package laser.juliette.ddgbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import laser.juliette.ams.AgendaItem;

/**
 * @author B. Lerner (edited by Sophia)
 * 
 */
public class ItemToSimpleHandlerMapper {
	private Map<AgendaItem, ArrayList<StepInstanceNode>> parentToSimpleHandler
		= new HashMap<AgendaItem, ArrayList<StepInstanceNode>>();

	/**
	 * @param handler
	 *            the ProcedureInstanceNode
	 * @param parentItem
	 */
	public void addHandler(AgendaItem parentItem, StepInstanceNode handler) {
		if (!parentToSimpleHandler.containsKey(parentItem)) { // if agenda item
																// is not yet on
																// the map
			parentToSimpleHandler.put(parentItem,
					new ArrayList<StepInstanceNode>());
		}
		parentToSimpleHandler.get(parentItem).add(handler); // add PIN to
															// collection of
															// PINs
															// corresponding
		// to Agenda Item
	}

	/**
	 * @param parent
	 *            The agenda item of interest
	 * @return Procedure Instance Node corresponding to a given Agenda Item
	 * @throws NoSuchAgendaItemException
	 *             if the Agenda Item passed to the method is nonexistent
	 */
	public Iterator<StepInstanceNode> getHandlerPINS(AgendaItem parent) {
		if (parentToSimpleHandler.containsKey(parent)) {
			return parentToSimpleHandler.get(parent).iterator();
		} else {
			return new Iterator<StepInstanceNode>() {

				public boolean hasNext() {
					return false;
				}

				public StepInstanceNode next() {
					throw new NoSuchElementException();
				}

				public void remove() {
					throw new IllegalStateException();
				}

			};
		}
	}

}
