package laser.juliette.ddgbuilder.test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import laser.juliette.ams.AMSException;
import laser.juliette.ams.Agenda;
import laser.juliette.ams.AgendaItem;
import laser.juliette.ams.AgendaItemHierarchyListener;
import laser.juliette.ams.AgendaItemListener;
import laser.juliette.ams.AgendaItemStateListener;
import laser.juliette.ams.IllegalTransition;
import laser.juliette.ams.UnknownAnnotation;
import laser.juliette.ams.UnknownParameter;
import laser.lj.Step;

public class StubAgendaItem implements AgendaItem {
	private StubStep step;
	private AgendaItem parent;
	private Set<AgendaItem> children = new HashSet<AgendaItem>();
	private Map<String,Serializable> paramValues = new HashMap<String,Serializable>();
	private String state = "";
	
	public StubAgendaItem (String name, boolean leaf, StubAgendaItem parent) {
		this.parent = parent;
		this.step = new StubStep(name, leaf);
		if (parent != null) {
			parent.children.add(this);
		}
	}
	public StubAgendaItem(boolean parallel, String name, StubAgendaItem parent) {
		this.parent = parent;
		this.step = new StubStep(parallel, name);
		if (parent != null) {
			parent.children.add(this);
		}
	}
	
	public StubAgendaItem(String name, boolean leaf, StubAgendaItem parent,
			StubAgendaItem prereqItem) {
		this(name, leaf, parent);
		try {
			if (prereqItem != null) {
				step.setPrereq(prereqItem.getStep());
				prereqItem.setParent(this);
				children.add(prereqItem);   // Interpreter does this.
			}
		} catch (AMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public StubAgendaItem(String name, boolean leaf, StubAgendaItem parent, boolean isRequisite) {
		this(name, leaf, parent);
		step.setIsRequisite(isRequisite);
	}

	public StubAgendaItem(String name, boolean leaf, StubAgendaItem parent,
			StubAgendaItem prereqItem, StubAgendaItem postreqItem) {
		this(name, leaf, parent, prereqItem);
		try {
			step.setPostreq(postreqItem.getStep());
			postreqItem.setParent(this);
		} catch (AMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setParent(StubAgendaItem parent) {
		this.parent = parent;
	}
	public void addAgendaItemHierarchyListener(AgendaItemHierarchyListener arg0)
			throws AMSException {
		// TODO Auto-generated method stub

	}

	
	public void addAgendaItemListener(AgendaItemListener arg0)
			throws AMSException {
		// TODO Auto-generated method stub

	}

	
	public void addAgendaItemStateListener(AgendaItemStateListener arg0)
			throws AMSException {
		// TODO Auto-generated method stub

	}

	
	public Set<String> annotationNames() throws AMSException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void complete() throws AMSException, IllegalTransition {
		state = AgendaItem.COMPLETED;
	}

	
	public void createAnnotation(String arg0, Serializable arg1)
			throws AMSException {
		// TODO Auto-generated method stub

	}

	
	public Agenda getAgenda() throws AMSException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Serializable getAnnotation(String arg0) throws AMSException,
			UnknownAnnotation {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Set<AgendaItem> getChildren() throws AMSException {
		// TODO Auto-generated method stub
		return children;
	}

	
	public Set<Serializable> getExceptions() throws AMSException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public int getPID() throws AMSException {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public Serializable getParameter(String name) throws AMSException,
			UnknownParameter {
		return paramValues.get(name);
	}

	
	public AgendaItem getParent() throws AMSException {
		return parent;
	}

	
	public String getState() throws AMSException {
		return state;
	}

	
	public Step getStep() throws AMSException {
		// TODO Auto-generated method stub
		return step;
	}

	
	public boolean isOptional() throws AMSException {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void optOut() throws AMSException, IllegalTransition {
		// TODO Auto-generated method stub

	}

	
	public Set<String> parameterNames() throws AMSException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void removeAgendaItemHierarchyListener(
			AgendaItemHierarchyListener arg0) throws AMSException {
		// TODO Auto-generated method stub

	}

	
	public void removeAgendaItemListener(AgendaItemListener arg0)
			throws AMSException {
		// TODO Auto-generated method stub

	}

	
	public void removeAgendaItemStateListener(AgendaItemStateListener arg0)
			throws AMSException {
		// TODO Auto-generated method stub

	}

	
	public void removeAnnotation(String arg0) throws AMSException,
			UnknownAnnotation {
		// TODO Auto-generated method stub

	}

	
	public void setAnnotation(String arg0, Serializable arg1)
			throws AMSException, UnknownAnnotation {
		// TODO Auto-generated method stub

	}

	
	public void setParameter(String name, Serializable value)
			throws AMSException, UnknownParameter {
		paramValues.put(name, value);
	}

	
	public void start() throws AMSException, IllegalTransition {
		// TODO Auto-generated method stub

	}

	
	public void terminate(Set<Serializable> arg0) throws AMSException,
			IllegalTransition {
		// TODO Auto-generated method stub

	}
	public void addLocalParameter(String name, Serializable value) {
		step.addParameter(name);
		paramValues.put(name, value);
	}
	
	public void addOutputParameter(String name) {
		step.addOutputParameter(name);
		
	}
	public void addInputParameter(String name, Serializable value) {
		step.addInputParameter(name);
		paramValues.put(name, value);
	}
	public void addInOutParameter(String name, Serializable value) {
		step.addInOutParameter(name);
		paramValues.put(name, value);
	}

}
