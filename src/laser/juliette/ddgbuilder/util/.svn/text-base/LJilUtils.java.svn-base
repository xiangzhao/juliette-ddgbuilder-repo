package laser.juliette.ddgbuilder.util;

import laser.juliette.ams.AMSException;
import laser.juliette.ams.AgendaItem;
import laser.lj.ResolutionException;
import laser.lj.Step;

public class LJilUtils {
	// Avoid chance to instantiate
	private LJilUtils() {
		
	}
	
	/**
	 * Returns true if this is the first child or the parent to start
	 * 
	 * @param parentItem
	 *            the parent to check
	 * @return true if this is the first child or the parent to start
	 * @throws AMSException
	 */
	public static boolean isFirstSiblingToStart(AgendaItem parentItem)
		throws AMSException {
		int numSiblingsStarted = 0;
		for (AgendaItem siblingItem : parentItem.getChildren()) {
			if (alreadyStarted(siblingItem)) {
				numSiblingsStarted++;
			}
		}
		return numSiblingsStarted == 1;
	}

	/**
	 * Returns true if this step has been started.
	 * 
	 * @param item
	 *            the step to check
	 * @return true if this step has been started.
	 * @throws AMSException
	 */
	private static boolean alreadyStarted(AgendaItem item) throws AMSException {
		String state = item.getState();

		if (state.equals(AgendaItem.INITIAL)
				|| state.equals(AgendaItem.OPTED_OUT)
				|| state.equals(AgendaItem.OPTING_OUT)
				|| state.equals(AgendaItem.POSTED)
				|| state.equals(AgendaItem.RETRACTED)
				|| state.equals(AgendaItem.STARTING)) {
			return false;
		}

		return true;
	}

	/**
	 * Returns true if the agenda item passed in is a prerequisite for a step
	 * 
	 * @param item
	 *            the agenda item to check
	 * @return if this agenda item is a prerequisite
	 * @throws AMSException
	 *             if we can't communicate with AMS
	 */
	public static boolean isPrereq(AgendaItem item) throws AMSException {
		Step step = item.getStep();
		if (!step.isRequisite()) {
			System.out.println("Not a requisite");
			return false;
		}

		Step parent = item.getParent().getStep();
		try {
			if (step.equals(parent.getPrerequisite())) {
				System.out.println("Is a prerequisite");
				return true;
			}
			//System.out.println("Parent prereq is " + parent.getPrerequisite().getName());
			//System.out.println("Parent prereq is " + parent.getPrerequisite());
			//System.out.println("Prereq is " + step);
		} catch (ResolutionException e) {
			return false;
		}

		System.out.println("Not a prerequisite");
		return false;
	}

}
