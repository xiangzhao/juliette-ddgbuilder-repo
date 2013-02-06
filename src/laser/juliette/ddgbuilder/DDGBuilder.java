package laser.juliette.ddgbuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import laser.ddg.DataBindingListener;
import laser.ddg.ParameterAlreadyBoundException;
import laser.ddg.ProcedureInstanceNode;
import laser.ddg.ProvenanceData;
import laser.ddg.RootAlreadySetException;
import laser.ddg.persist.CPLWriter;
import laser.ddg.persist.JenaWriter;
import laser.ddg.visualizer.PrefuseGraphBuilder;
import laser.juliette.ams.AMSException;
import laser.juliette.ams.AgendaItem;
import laser.juliette.ams.UnknownParameter;
import laser.juliette.ddgbuilder.test.StubAgendaItem;
import laser.juliette.ddgbuilder.util.LJilUtils;
import laser.juliette.jul.JulFile;
import laser.lj.Binding;
import laser.lj.InterfaceDeclaration;
import laser.lj.InterfaceDeclaration.DeclarationKind;
import laser.lj.InterfaceDeclarationSet;
import laser.lj.ResolutionException;
import laser.lj.ScopeBinding;
import laser.lj.ScopeBinding.Mode;
import laser.lj.Step;

/**
 * Provides the methods to build a DDG from a running Little-JIL program. The
 * interpreter needs to notify this object when a step starts, completes,
 * terminates and when data is passed from one node to another.
 * 
 * Calls methods from package laser.juliette.PersistJB to build a persistent DDG
 * for a running Little-JIL process
 * 
 * As soon as the DDG building has been completed, all contents of the database
 * are printed out in XML format; finally, several test queries are performed on
 * the database of nodes and results are printed out.
 * 
 * @author Barbara Lerner
 * @version Mar 14, 2011 (edited 7/21/2011 by sophia)
 * 
 */
public class DDGBuilder implements laser.juliette.runtime.DDGBuilder {
	// Knows about the set of all step instance nodes and all data instance
	// nodes
	private ProvenanceData provData;

	// Maps from agenda items to step instance nodes
	// Made this static so we could have a static method for the
	// agent to call to get their hands on the step instance nodes.
	// There is probably a better way to get this effect and it
	// won't work if we ever have more than one ddg being managed
	// by one interpreter.   Barb  August 2012
	private static AgendaItemToStepInstanceNode agendaItemMapper;

	// Maps from a parent SIN to all its children SINs
	private ParentItemToChildSin childSinMapper;

	// Maps from an agenda item to the virtual steps associated with it.
	// Only steps with requisites have virtual steps.
	private AgendaItemToStepInstanceNode virtualStepMapper;

	// Maps from an agenda item to simple handlers attached to the
	// step that the agenda item is for if those handlers are
	// executed
	private ItemToSimpleHandlerMapper simpleHandlerMapper;

	// Maps the binding of one step's parameter to another as the
	// parameter value is copied.
	private ParameterTable paramTable;

	private Map<Serializable, StepInstanceNode> lastThrowerTable
		= new HashMap<Serializable, StepInstanceNode>();


	/**
	 * Creates a new DDG builder
	 * @param processName the name of the process being executed.  This is how it will
	 * be identified in the database.
	 * @param jul 
	 */
	public DDGBuilder(String processName, JulFile jul) {
		this.provData = new ProvenanceData(processName);
		this.agendaItemMapper = new AgendaItemToStepInstanceNode();
		this.virtualStepMapper = new AgendaItemToStepInstanceNode();
		this.childSinMapper = new ParentItemToChildSin();
		this.simpleHandlerMapper = new ItemToSimpleHandlerMapper();
		this.paramTable = new ParameterTable();
		System.out.println("Initialized DDG builder");

		String persistProperty = jul.getStringProperty("ddg-persist");
		if (persistProperty != null && persistProperty.equals("true")) {
			provData.addProvenanceListener(new JenaWriter());
			//provData.addProvenanceListener(new CPLWriter());
		}
		
		String drawProperty = jul.getStringProperty("ddg-draw");
		if (drawProperty != null && drawProperty.equals("true")) {
			provData.addProvenanceListener(new PrefuseGraphBuilder());
		}
		
		provData.notifyProcessStarted(processName);
	}
	
	public static AgendaItemToStepInstanceNode getAgendaItemMapper() {
		return agendaItemMapper;
	}

	/**
	 * Indicates that a step is about to start. If the step has a prerequisite
	 * or a postrequisite, it creates a Virtual Start Node, whose predecessor is
	 * the parent of the step that is starting.
	 * 
	 * If this step has no requisites, it does nothing.
	 * 
	 * @param item
	 *            the agenda item that is starting
	 */
	@Override
	public void starting(AgendaItem item) {
		/*
		 * If this step has neither a prereq nor a postreq, do nothing and
		 * return.
		 * 
		 * If this step has a prerequisite or a postrequisite, create a virtual
		 * start node. Set its predecessor the way it is set in started. Add it
		 * to a virtual node table.
		 */
		try {
			Step step = item.getStep();
			System.out.println(step.getName() + " starting");
			if (!step.hasPrerequisite() && !step.hasPostrequisite()) {
				return;
			}

			StepInstanceNode sin = new StartVirtualNode(step, null, provData);
			provData.addPIN(sin);

			// Remember that this step has a virtual layer in the DDG
			virtualStepMapper.connect(item, sin);
			connectParentAsPred(item, sin);
		} catch (AMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Indicates that a step has started. If this is a leaf step, it creates a
	 * StepInstanceNode. If it is a non-leaf, it creates a
	 * StartStepInstanceNode. Its predecessor is set to the step instance node
	 * corresponding to the parent step.
	 * 
	 * If the step declares any local variables, it creates a DataInstanceNode.
	 * 
	 * If this is a leaf step, the DataInstanceNodes that correspond to its
	 * parameters are set as inputs to the DataInstanceNode.
	 * 
	 * @param item
	 *            the item that was started
	 */
	@Override
	public void started(AgendaItem item) {
		try {
			// Create the step instance node
			Step step = item.getStep();
			System.out.println("DDGBuilder:  " + step.getName() + " started ");
			StepInstanceNode predSin = getPredecessor(item, step);
			StepInstanceNode sin = createStartStepInstanceNode(item, step);
			if (predSin == null) {
				System.out.println(" is the root.");
				provData.setRoot(sin);
			} else {
				addPredSuccLink(predSin, sin);
			}

			createDinsForLocals(item, step, sin);

			if (step.isLeaf()) {
				createDFEdgesForInput(item, step, sin);
			}
			// TODO: Agent connection
		} catch (RootAlreadySetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private StepInstanceNode getPredecessor(AgendaItem item, Step step)
		throws AMSException {
		if (step.hasPrerequisite() || step.hasPostrequisite()) {
			System.out.println("Has a requisite - predecessor is virtual");
			return getVirtualAsPredecessor(item);
		} else if (step.isRequisite()) {
			System.out.println("Is a requisite - predecessor is virtual");
			return getVirtualAsPredecessor(item.getParent());
		} else {
			System.out
					.println("No requisites involved - predecessor is parent");
			return getParentAsPredecessor(item);
		}
	}

	private StepInstanceNode getParentAsPredecessor(AgendaItem item)
		throws AMSException {
		AgendaItem parentItem = item.getParent();

		Step parentStep;
		boolean parentHasPrerequisite;
		if (parentItem == null) {
			parentStep = null;
			parentHasPrerequisite = false;
		} else {
			parentStep = parentItem.getStep();
			parentHasPrerequisite = parentStep.hasPrerequisite();
		}

		if (parentItem == null) {
			System.out.println(" is the root.");
			return null;
		}

		// All children of a parallel step have the parent start as their
		// predecessor
		else if (parentStep.isParallel()) {
			System.out.println(" is child of parallel.");
			return agendaItemMapper.getLastPIN(parentItem);
		}

		// If this is the first, its predecessor is the parent start
		// If the child terminates before starting, the parent will report
		// having
		// 0 children. Otherwise, the parent will report having 1 child.
		else if (!parentHasPrerequisite && parentItem.getChildren().size() <= 1) {
			System.out.println(" is the first child.");
			return agendaItemMapper.getLastPIN(parentItem);
		}

		else if (parentHasPrerequisite && parentItem.getChildren().size() <= 2) {
			System.out.println(" is the first child.");
			return agendaItemMapper.getLastPIN(parentItem);
		}

		// If this is the child of a choice and this is the first child to
		// start,
		// its predecessor is the parent start
		else if (parentStep.isChoice()
				&& LJilUtils.isFirstSiblingToStart(parentItem)) {
			System.out.println(" is the first child of choice to start.");
			return agendaItemMapper.getLastPIN(parentItem);
		}

		// We need to create an intermediate sin for the parent
		else {
			System.out.println(" is child #" + parentItem.getChildren().size());
			System.out.println(" creating an interm for parent.");
			return createParentInterm(parentItem);
		}
	}

	private StepInstanceNode createParentInterm(AgendaItem parentItem)
		throws AMSException {
		// System.out.println (" should follow an interm node.");
		StepInstanceNode parentInterm = new IntermStepInstanceNode(
				parentItem.getStep(), null, provData);
		provData.addPIN(parentInterm);
		agendaItemMapper.connect(parentItem, parentInterm);
		StepInstanceNode prevSibling = childSinMapper
				.getLastChildPIN(parentItem);
		addPredSuccLink(prevSibling, parentInterm);
		return parentInterm;
	}

	private StepInstanceNode getVirtualAsPredecessor(AgendaItem item) {
		StepInstanceNode virtualStep = virtualStepMapper.getLastPIN(item);
		assert virtualStep != null;
		return virtualStep;
	}

	/**
	 * Create the dataflow edges for a steps inputs.
	 * 
	 * @param item
	 *            the agenda item for the step that is starting
	 * @param step
	 *            the step definition
	 * @param sin
	 *            the step instance node.
	 * @throws AMSException
	 *             if AMS cannot be contacted to get parameter values
	 */
	private void createDFEdgesForInput(AgendaItem item, Step step,
			StepInstanceNode sin) throws AMSException {
		InterfaceDeclarationSet inParams = step.getInputParameters();
		if (inParams != null) {
			for (InterfaceDeclaration param : inParams) {
				try {
					String paramName = param.getName();

					Serializable parameter = item.getParameter(paramName);
					System.out.println(step.getName() + " reading " + paramName
							+ " " + parameter.hashCode());
					DataInstanceNode original = paramTable
							.getOriginal(parameter);
					sin.addInput(paramName, original);
					original.addUserPIN(sin);
				} catch (ParameterAlreadyBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownParameter e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Create Data Instance Nodes for local parameters
	 * 
	 * @param item
	 *            the item being started
	 * @param step
	 *            its step definition
	 * @param sin
	 *            the step instance node for the item
	 * @throws AMSException
	 *             if AMS cannot be contacted to get parameter values
	 */
	private void createDinsForLocals(AgendaItem item, Step step,
			StepInstanceNode sin) throws AMSException {
		InterfaceDeclarationSet parameters = step.getParameters();
		for (String name : parameters.getNames()) {
			try {
				InterfaceDeclaration parameter = step
						.getInterfaceDeclaration(name);
				if (parameter.getDeclarationKind() == DeclarationKind.LOCAL_PARAMETER) {
					Serializable param = item.getParameter(name);
					DataInstanceNode din = new DataInstanceNode(param, name,
							sin, provData);
					System.out.println(step.getName() + " writing " + name
							+ " " + param.hashCode());
					provData.addDIN(din);
					sin.addOutput(name, din);
					paramTable.setOriginal(param, din);
					System.out.println("saving data node " + din);

				}
			} catch (UnknownParameter e) {
				// Should not happen!!
				e.printStackTrace();
			}

		}
	}

	/**
	 * Create control flow edges for the predecessor when a step is starting or
	 * is terminated when there is no starting node for that step
	 * 
	 * @param item
	 *            the agenda item being started
	 * @param step
	 *            its step definition
	 * @param sin
	 *            the step instance node being updated
	 * @throws AMSException
	 *             if AMS can't be contacted
	 */
	private void createStartControlFlowEdge(AgendaItem item, Step step,
			StepInstanceNode sin) throws AMSException {
		if (step.hasPrerequisite() || step.hasPostrequisite()) {
			System.out.println("Has a requisite - predecessor is virtual");
			connectVirtualAsPred(item, sin);
		} else if (step.isRequisite()) {
			System.out.println("Is a requisite - predecessor is virtual");
			connectVirtualAsPred(item.getParent(), sin);
		} else {
			System.out
					.println("No requisites involved - predecessor is parent");
			connectParentAsPred(item, sin);
		}
	}

	/**
	 * Create a step instance node when a step is started
	 * 
	 * @param item
	 *            the agenda item being started
	 * @param step
	 *            its step definition
	 * @return the new step instance node.
	 */
	private StepInstanceNode createStartStepInstanceNode(AgendaItem item,
			Step step) {
		StepInstanceNode sin;
		if (step.isLeaf()) {
			sin = new StepInstanceNode(step, null, provData);
		} else {
			sin = new StartStepInstanceNode(step, null, provData);
		}
		provData.addPIN(sin);

		// Remember that this step instance node corresponds to this agenda
		// item.
		agendaItemMapper.connect(item, sin);
		return sin;
	}

	/**
	 * Connects a virtual node as a predecesoor of a step instance node
	 * 
	 * @param item
	 *            the item with an attached requisite
	 * @param sin
	 *            the step instance node for the item
	 */
	private void connectVirtualAsPred(AgendaItem item, StepInstanceNode sin) {
		StepInstanceNode virtualStep = virtualStepMapper.getLastPIN(item);

		assert virtualStep != null;

		addPredSuccLink(virtualStep, sin);
	}

	/**
	 * Connect the parent of a step as the predecessor
	 * 
	 * @param item
	 *            the agenda item to connect
	 * @param sin
	 *            its step instance node
	 * @throws AMSException
	 *             if the AMS is not responding
	 */
	@SuppressWarnings("null")
	private void connectParentAsPred(AgendaItem item, StepInstanceNode sin)
		throws AMSException {
		AgendaItem parentItem = item.getParent();

		Step parentStep;
		boolean parentHasPrerequisite;
		if (parentItem == null) {
			parentStep = null;
			parentHasPrerequisite = false;
		} else {
			parentStep = parentItem.getStep();
			parentHasPrerequisite = parentStep.hasPrerequisite();
		}

		if (parentItem == null) {
			System.out.println(" is the root.");
			provData.setRoot(sin);
		}

		// All children of a parallel step have the parent start as their
		// predecessor
		else if (parentStep.isParallel()) {
			System.out.println(" is child of parallel.");
			addPredSuccLink(parentItem, sin);
		}

		// If this is the first, its predecessor is the parent start
		// If the child terminates before starting, the parent will report
		// having
		// 0 children. Otherwise, the parent will report having 1 child.
		else if (!parentHasPrerequisite && parentItem.getChildren().size() <= 1) {
			System.out.println(" is the first child.");
			addPredSuccLink(parentItem, sin);
		}

		else if (parentHasPrerequisite && parentItem.getChildren().size() <= 2) {
			System.out.println(" is the first child.");
			addPredSuccLink(parentItem, sin);
		}

		// If this is the child of a choice and this is the first child to
		// start,
		// its predecessor is the parent start
		else if (parentStep.isChoice()
				&& LJilUtils.isFirstSiblingToStart(parentItem)) {
			System.out.println(" is the first child of choice to start.");
			addPredSuccLink(parentItem, sin);
		}

		// We need to create an intermediate sin for the parent
		else {
			System.out.println(" is child #" + parentItem.getChildren().size());
			System.out.println(" creating an interm for parent.");
			connectParentIntermAsPred(sin, parentItem);
		}
	}

	private void connectParentIntermAsPred(StepInstanceNode sin,
			AgendaItem parentItem) throws AMSException {
		// System.out.println (" should follow an interm node.");
		StepInstanceNode parentInterm = new IntermStepInstanceNode(
				parentItem.getStep(), null, provData);
		provData.addPIN(parentInterm);
		agendaItemMapper.connect(parentItem, parentInterm);
		StepInstanceNode prevSibling = childSinMapper
				.getLastChildPIN(parentItem);
		addPredSuccLink(prevSibling, parentInterm);
		addPredSuccLink(parentInterm, sin);
	}

	/**
	 * Indicates that all the children of a step have completed or a leaf has
	 * completed.
	 * <p>
	 * In the case of a leaf step, we do not create any more StepInstanceNodes.
	 * For a non-leaf step, we create a FinishStepInstanceNode. If it is a
	 * parallel step, the Finish nodes for all of its children are its
	 * predecessors. For all other steps, it is the Finish node of the last
	 * child to complete.
	 * 
	 * @param item
	 *            The item associated with the step that is completing.
	 */
	@Override
	public void completing(AgendaItem item) {
		try {
			Step step = item.getStep();
			System.out.println(step.getName() + " completing ");
			StepInstanceNode sin;
			if (step.isLeaf()) {
				sin = agendaItemMapper.getLastPIN(item);
				connectOutputParameters(item, step, sin);
			}

			else {
				sin = new FinishStepInstanceNode(step, null, provData);
				provData.addPIN(sin);

				// Remember that this step instance node corresponds to this
				// agenda item.
				agendaItemMapper.connect(item, sin);
				connectCompletingPredecessors(item, step, sin);

			}

			// Remember this sin in its parent map.
			AgendaItem parentItem = item.getParent();
			StepInstanceNode parentNode = agendaItemMapper
					.getLastPIN(parentItem);
			childSinMapper.addChild(parentItem, parentNode, sin);

			StepInstanceNode virtualSin = null;
			if (step.hasPostrequisite()) {
				virtualSin = new IntermVirtualNode(step, null, provData);
			} else if (step.hasPrerequisite()) {
				System.out
						.println("Step with a prereq and no postreq is completing --- creating a virtual finish node.");
				virtualSin = new FinishVirtualNode(step, null, provData);
				childSinMapper.addChild(parentItem, parentNode, virtualSin);
			}

			if (virtualSin != null) {
				provData.addPIN(virtualSin);
				virtualStepMapper.connect(item, virtualSin);
				addPredSuccLink(sin, virtualSin);
			}

		} catch (AMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Connect the predecessors to a step that is completing
	 * 
	 * @param item
	 *            the agenda item that is completing
	 * @param step
	 *            its step definition
	 * @param sin
	 *            the corresponding step instance node
	 * @throws AMSException
	 *             if AMS is not responding
	 */
	private void connectCompletingPredecessors(AgendaItem item, Step step,
			StepInstanceNode sin) throws AMSException {
		// All the children of a parallel step are predecessors of the
		// parallel step
		if (step.isParallel()) {
			for (AgendaItem child : item.getChildren()) {
				System.out.println("Child:  " + child.getStep().getName());
				if (virtualStepMapper.containsAI(child)) {
					connectVirtualAsPred(child, sin);
				} else if (child.getState().equals(AgendaItem.COMPLETED)) {
					addPredSuccLink(child, sin);
				}
			}

			// Add in the simple handlers that executed since they do not show
			// up as children of an agenda item.
			Iterator<StepInstanceNode> handlerIter = simpleHandlerMapper
					.getHandlerPINS(item);
			while (handlerIter.hasNext()) {
				StepInstanceNode handler = handlerIter.next();
				addPredSuccLink(handler, sin);
			}

		} else {
			addPredSuccLink(childSinMapper.getLastChildPIN(item), sin);
		}
	}

	/**
	 * Connect data flow edges to the output parameters
	 * 
	 * @param item
	 *            the agenda item that is completing
	 * @param step
	 *            its step definition
	 * @param sin
	 *            the corresponding step instance node
	 * @throws AMSException
	 *             if AMS is not responding
	 */
	private void connectOutputParameters(AgendaItem item, Step step,
			StepInstanceNode sin) throws AMSException {
		InterfaceDeclarationSet params = step.getOutputParameters();
		if (params != null) {
			for (InterfaceDeclaration parameter : params) {
				try {
					String name = parameter.getName();
					Serializable param = item.getParameter(name);
					DataInstanceNode din = new DataInstanceNode(param, name,
							sin, provData);
					System.out.println(step.getName() + " writing " + name
							+ " " + param.hashCode());
					provData.addDIN(din);
					sin.addOutput(name, din);
					paramTable.setOriginal(param, din);
					// make new node persistent
					// persistJB.save(din);
					System.out.println("saving data node " + din);

				} catch (ParameterAlreadyBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownParameter e) {
					// Should not happen
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("No output parameters");
		}
	}

	/**
	 * Connect two StepInstanceNodes with predecessor / successor links
	 * 
	 * @param predItem
	 *            the agenda item whose last StepInstanceNode should be the
	 *            predecessor
	 * @param succSin
	 *            the StepInstanceNode that should be the successor
	 */
	private void addPredSuccLink(AgendaItem predItem, StepInstanceNode succSin) {
		ProcedureInstanceNode predSin = agendaItemMapper.getLastPIN(predItem);
		addPredSuccLink(predSin, succSin);
	}

	/**
	 * Connect two StepInstanceNodes with predecessor / successor links
	 * 
	 * @param predSin
	 *            the StepInstanceNode that should be the predecessor
	 * @param succSin
	 *            the StepInstanceNode that should be the successor
	 */
	private void addPredSuccLink(ProcedureInstanceNode predSin,
			StepInstanceNode succSin) {
		succSin.addPredecessor(predSin);
		predSin.addSuccessor(succSin);
	}

	/**
	 * Indicates that a step and its postrequisite have completed.
	 * 
	 * If the step that completed is not a requisite, do nothing.
	 * 
	 * If the step that completed is a prerequisite, create a Virtual
	 * Intermediate node. If the step that completed is a postrequisite, create
	 * a Virtual Final node. Add to the virtual item mapper. Set its predecessor
	 * to be the last node for the requisite.
	 * 
	 * @param item
	 *            the agenda item that is completed
	 */
	@Override
	public void completed(AgendaItem item) {
		try {
			Step step = item.getStep();
			System.out.println(step.getName() + " completed ");
			if (item.getParent() == null) {
				saveDDG();
			}

			if (!step.isRequisite()) {
				return;
			}

			// Check if it is a postreq
			StepInstanceNode sin;
			AgendaItem parent = item.getParent();
			// if (agendaItemMapper.containsAI(parent)) {
			if (LJilUtils.isPrereq(item)) {
				sin = new IntermVirtualNode(item.getStep(), null, provData);
			} else {
				System.out
						.println("Postrequisite completed -- Creating virtual finish node");
				sin = new FinishVirtualNode(item.getStep(), null, provData);
				AgendaItem realParent = parent.getParent();
				childSinMapper.addChild(realParent,
						agendaItemMapper.getLastPIN(realParent), sin);
			}
			provData.addPIN(sin);

			virtualStepMapper.connect(parent, sin);

			addPredSuccLink(agendaItemMapper.getLastPIN(item), sin);

		} catch (AMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println();
		System.out.println();
	}

	/**
	 * Draws the DDG and saves it in the database.
	 */
	private void saveDDG() {
		System.out.println(provData.toString());
		provData.notifyProcessFinished();
		
		try {
			System.out.println("Hit return to finish.");
			System.in.read();
		} catch (IOException exception) {
			// TODO Auto-generated catch-block stub.
			exception.printStackTrace();
		}


		System.out.println("Finish drawgraph");
		System.out.println();

	}

	/**
	 * Called when a data value is passed from one step to another. We keep
	 * track of the fact that these two objects represent the same value.
	 * 
	 * @param from
	 *            the value as known in the originating step
	 * @param to
	 *            the data value as known in the receiving step
	 */
	@Override
	public void copied(Serializable from, Serializable to, Binding binding, Mode copyMode) {
		if (!(binding instanceof ScopeBinding)) {
			return;
		}

		ScopeBinding scoped = (ScopeBinding) binding;
		
		if (isCopyBinding(scoped)) {
			System.out
				.println("Copied " + from.toString() + " " + from.hashCode() + " to " + to.toString() + " " + to.hashCode());
		
			paramTable.bind(from, to);
		}
		else {
			if (copyMode == Mode.COPY_IN) {
				System.out.println("Derived " + bindingChildToString(scoped) + " with value " + to.toString() + " " + to.hashCode() + 
						"\n   from " + bindingParentToString(scoped) + " with value " + from.toString() + " " + from.hashCode() + " as " + bindingToString(scoped, copyMode));
			}
			else {
				System.out.println("Derived " + bindingParentToString(scoped) + " with value " + to.toString() + " " + to.hashCode() + 
						"\n   from " + bindingChildToString(scoped) + " with value " + from.toString() + " " + from.hashCode() + " as " + bindingToString(scoped, copyMode));
				
			}
			addInterpreterDerivation (from, to, scoped, copyMode);
		}
	}
	
	private void addInterpreterDerivation(Serializable from, Serializable to,
			ScopeBinding scoped, Mode copyMode) {
		System.out.println("addInterpreterDerivation:  from = " + from.toString());
		// Add an Interpreter PIN
		ProcedureInstanceNode pin = createInterperterPIN(scoped, copyMode);
		
		// Add an input edge from "from"
		createDFEdgesForDerivedFrom (pin, from, scoped);
		
		// Add a data node for "to" and an output edge to "to"
		createDinForDerivedTo(pin, to, scoped);
	}

	private void createDFEdgesForDerivedFrom(ProcedureInstanceNode pin,
			Serializable from, ScopeBinding binding) {
		DataInstanceNode original = paramTable.getOriginal(from);
		pin.addInput(bindingParentToString(binding), original);
		original.addUserPIN(pin);
	}
	
	private void createDinForDerivedTo(ProcedureInstanceNode pin, Serializable to, ScopeBinding binding) {
		DataInstanceNode din = new DataInstanceNode(to, "",
				pin, provData);
		provData.addDIN(din);
		pin.addOutput(bindingChildToString(binding), din);
		paramTable.setOriginal(to, din);
	}
	
	private ProcedureInstanceNode createInterperterPIN(ScopeBinding binding, Mode copyMode) {
		ProcedureInstanceNode pin = new InterpreterBindingNode(bindingToString(binding, copyMode), provData);
		provData.addPIN(pin);

		// TODO:  How do I find the predecessor???
		// addPredSuccLink(predSin, pin);
		
		return pin;
	}

	// This should be in ScopeBinding !
	private String bindingToString(ScopeBinding binding, Mode copyMode) {
		if (copyMode == Mode.COPY_IN) {
			String bindingString = bindingChildToString(binding);
			bindingString = bindingString + " = ";
			bindingString = bindingString + bindingParentToString(binding);
			return bindingString;
		}
		else {
			String bindingString = bindingParentToString(binding);
			bindingString = bindingString + " = ";
			bindingString = bindingString + bindingChildToString(binding);
			return bindingString;

		}
	}

	// TODO:  This should be in the ScopeBinding class instead
	private String bindingParentToString(ScopeBinding binding) {
		String bindingString = binding.getDeclarationInParent().getName();
		for (String part: binding.getFieldAccessorsForDeclarationInParent()) {
			if (part.equals(Binding.COLLECTION_ELEMENT)) {
				bindingString = bindingString + part;
			}
			else {
				bindingString = bindingString + "." + part;
			}
		}
		return bindingString;
	}

	// TODO:  This should be in the ScopeBinding class instead
	private String bindingChildToString(ScopeBinding binding) {
		String bindingString = binding.getDeclarationInChild().getName();
		for (String part: binding.getFieldAccessorsForDeclarationInChild()) {
			if (part.equals(Binding.COLLECTION_ELEMENT)) {
				bindingString = bindingString + part;
			}
			else {
				bindingString = bindingString + "." + part;
			}
		}
		return bindingString;
	}

	// TODO:  This should be in the ScopeBinding class instead
	private boolean isCopyBinding(ScopeBinding scoped) {
		if (scoped.getFieldAccessorsForDeclarationInChild().length == 0
				&& scoped.getFieldAccessorsForDeclarationInParent().length == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Called when an exception handling step is stating
	 * 
	 * @param item
	 *            the agenda item that is starting
	 * @param exception
	 *            the exception being handled
	 */
	@Override
	public void starting(AgendaItem item, Serializable exception) {
		// Nothing to do

	}

	/**
	 * Creates a PIN for a completing non-leaf. Creates a virtual finish node if
	 * the step that is terminated either is a requisite or has a requisite.
	 * 
	 * Sets the exceptions produced as outputs of the step if the step created
	 * them, but not if the step is just propagating them.
	 * 
	 * For a non-leaf, sets the predecessor to be children if the children have
	 * started, its own starting node if it has been created, or its parent if
	 * it terminated when trying to start.
	 * 
	 * @param item the agenda item being terminated
	 * @param exceptions the exceptions being thrown
	 */
	@Override
	public void terminated(AgendaItem item, Set<Serializable> exceptions) {

		try {
			Step step = item.getStep();
			System.out.println(step.getName() + " terminated ");
			StepInstanceNode sin;

			// TODO: Ack!!! What if there are multiple exceptions!
			if (thrownByRequisite(exceptions, step)) {
				return;
			}

			if (!step.isRequisite()) {
				sin = getTerminatedSIN(item, step);

				// Remember this sin in its parent map.
				AgendaItem parentItem = item.getParent();
				childSinMapper.addChild(parentItem,
						agendaItemMapper.getLastPIN(parentItem), sin);

				if (step.hasPrerequisite() || step.hasPostrequisite()) {
					System.out
							.println("Step with a requisite is terminating --- creating a virtual finish node.");
					createVirtualSinOnTerminated(item, sin, parentItem);
				}

			}

			// It is a requisite
			else {
				AgendaItem parent = item.getParent();

				System.out
						.println("Requisite terminated -- Creating virtual finish node");
				sin = agendaItemMapper.getLastPIN(item);
				createVirtualSinOnTerminated(parent, sin, parent.getParent());
			}
			addExceptionsAsOutputs(exceptions, step, sin);

			// For debugging purposes
			if (item.getParent() == null) {
				saveDDG();
			}

		} catch (AMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private boolean thrownByRequisite(Set<Serializable> exceptions,
			Step terminatingStep) {
		Serializable exc = exceptions.iterator().next();
		StepInstanceNode thrower = lastThrowerTable.get(exc);

		if (thrower == null) {
			return false;
		}

		Object throwingObject = thrower.getProcedureDefinition();
		if (!(throwingObject instanceof StepReference)) {
			return false;
		}

		Step throwingStep = ((StepReference) throwingObject).getWrappedStep();
		if (!throwingStep.isRequisite()) {
			return false;
		}

		try {
			if (throwingStep.equals(terminatingStep.getPrerequisite())
				|| throwingStep.equals(terminatingStep.getPostrequisite())) {
				return true;
			}
		} catch (ResolutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return false;
	}

	/**
	 * Create a virtual node on termination. This should only be called if a
	 * step has a requisite or is the root of a requisite.
	 * 
	 * @param item
	 *            the agenda item that is throwing the exception
	 * @param sin
	 *            its step definition
	 * @param parentItem
	 *            the parent item for the step that is throwing the exception
	 * @throws AMSException
	 */
	private void createVirtualSinOnTerminated(AgendaItem item,
			StepInstanceNode sin, AgendaItem parentItem) throws AMSException {
		StepInstanceNode virtualSin = new FinishVirtualNode(item.getStep(),
				null, provData);
		childSinMapper.addChild(parentItem,
				agendaItemMapper.getLastPIN(parentItem), virtualSin);
		provData.addPIN(virtualSin);
		virtualStepMapper.connect(item, virtualSin);
		addPredSuccLink(sin, virtualSin);
		// persistJB.save(virtualSin);
	}

	/**
	 * Gets or creates the SIN to represent the termination of a step
	 * 
	 * @param item
	 *            the agenda item throwing an exception
	 * @param step
	 *            its step definition
	 * @return the SIN representing the termination
	 * @throws AMSException
	 *             if AMS is not responding
	 */
	private StepInstanceNode getTerminatedSIN(AgendaItem item, Step step)
		throws AMSException {
		StepInstanceNode sin;
		if (step.isLeaf()) {
			sin = agendaItemMapper.getLastPIN(item);
		}

		else {
			sin = new FinishStepInstanceNode(step, null, provData);
			provData.addPIN(sin);

			// Remember that this step instance node corresponds to this
			// agenda item.
			StepInstanceNode stepStartPIN = null;
			if (agendaItemMapper.containsAI(item)) {
				stepStartPIN = agendaItemMapper.getLastPIN(item);
			}
			agendaItemMapper.connect(item, sin);
			addTerminatedPredecessorControlFlow(item, step, sin, stepStartPIN);

		}
		// persistJB.save(sin);
		return sin;
	}

	/**
	 * Add control flow from the predecessor of a terminating node to the
	 * terminating node
	 * 
	 * @param item
	 *            the agenda item that is terminated
	 * @param step
	 *            its step definition
	 * @param sin
	 *            the step instance node representing the termination
	 * @param stepStartPIN
	 *            the start node for the agenda item. This will be null if the
	 *            termination occurs when the step is trying to start
	 * @throws AMSException
	 *             the AMS is not responding
	 */
	private void addTerminatedPredecessorControlFlow(AgendaItem item,
			Step step, StepInstanceNode sin, StepInstanceNode stepStartPIN)
		throws AMSException {
		// All the children of a parallel step are predecessors of the
		// parallel step
		if (step.isParallel()) {
			boolean anyChildStarted = false;
			for (AgendaItem child : item.getChildren()) {
				StepInstanceNode childPIN;
				if (virtualStepMapper.containsAI(child)) {
					childPIN = virtualStepMapper.getLastPIN(child);
				} else {
					childPIN = agendaItemMapper.getLastPIN(child);
				}
				if (childPIN != null) {
					addPredSuccLink(childPIN, sin);
					anyChildStarted = true;
				}
			}
			if (!anyChildStarted) {
				if (stepStartPIN != null) {
					assert false;
					addPredSuccLink(stepStartPIN, sin);
				} else {
					// This branch is tested.
					createStartControlFlowEdge(item, step, sin);
				}
			}

		} else {
			StepInstanceNode childPIN = childSinMapper.getLastChildPIN(item);
			if (childPIN != null) {
				// This branch is tested
				// System.out.println("Terminated step:  Adding child " +
				// childPIN.getId() + " as predecessor.");
				// System.out.println(childSinMapper.childPINSToString(item));
				addPredSuccLink(childPIN, sin);
			} else if (stepStartPIN != null) {
				assert false;
				// System.out.println("Terminated step:  2nd branch.");
				addPredSuccLink(stepStartPIN, sin);
			} else {
				// This branch is tested.
				// System.out.println("Terminated step:  3rd branch.");
				createStartControlFlowEdge(item, step, sin);
			}
		}
	}

	/**
	 * For each exception, check if there is an entry in the thrower table for
	 * this exception. If so, it is propagating the exception and we do nothing.
	 * If there is no entry, add an entry. When we see a starting for that
	 * exception, create a control flow link from the thrower to the handler.
	 * 
	 * @param exceptions
	 *            the exceptions being thrown
	 * @param step
	 *            the step definition of the throwing step
	 * @param sin
	 *            the step instance node representing the terminated step
	 */
	private void addExceptionsAsOutputs(Set<Serializable> exceptions,
			Step step, StepInstanceNode sin) {
		assert exceptions.size() > 0;

		for (Serializable exception : exceptions) {
			// Check if the exception is originating at a non-leaf, like a
			// resource exception

			DataInstanceNode originalException = paramTable
					.getOriginal(exception);
			if (originalException == null) {
				String name = exception.getClass().getName(); // "exception" +
																// counter;
				DataInstanceNode din = new ExceptionInstanceNode(exception,
						name, sin, provData);
				System.out.println(step.getName() + " throwing "
						+ din.hashCode());
				provData.addDIN(din);
				sin.addOutput(name, din);
				paramTable.setOriginal(exception, din);
				// persistJB.save(din);
			}

			else {
				System.out.println(step.getName() + " propagating "
						+ originalException.hashCode());
			}

			lastThrowerTable.put(exception, sin);
		}
	}

	/**
	 * @return process root (a PIN)
	 */
	public ProcedureInstanceNode getRoot() {
		return provData.getRoot();
	}

	/**
	 * @return all DINs created thus far
	 */
	public Iterator<laser.ddg.DataInstanceNode> allDinsIter() {
		return provData.dinIter();
	}

	/**
	 * @return pinIter iterator over all PINs created thus far
	 */
	public Iterator<ProcedureInstanceNode> allPinsIter() {
		return provData.pinIter();
	}

	/**
	 * @param agendaItem
	 * @return last PIN associated with current agenda item
	 */
	public ProcedureInstanceNode getLastPIN(StubAgendaItem agendaItem) {
		return agendaItemMapper.getLastPIN(agendaItem);
	}

	/**
	 * Print out textual representation of DDG
	 */
	public void dumpProvData() {
		System.out.println(provData.toString());
	}

	/**
	 * Add an object as a listener to data bindings
	 * 
	 * @param l
	 *            the new listener
	 */
	public void addDataBindingListener(DataBindingListener l) {
		provData.addDataBindingListener(l);
	}

	/**
	 * Remove a data binding listener
	 * 
	 * @param l
	 *            the listener to remove
	 */
	public void removeDataBindingListner(DataBindingListener l) {
		provData.removeDataBindingListner(l);
	}

	/**
	 * Called when a step is started to handle an exception or a message. If
	 * this is a leaf step, it creates a StepInstanceNode. If it is a non-leaf,
	 * it creates a StartStepInstanceNode. Its predecessor is set to the step
	 * instance node corresponding to the throwing of the exception or sending
	 * of the message.
	 * 
	 * If the step declares any local variables, it creates a DataInstanceNode.
	 * 
	 * If this is a leaf step, the DataInstanceNodes that correspond to its
	 * parameters are set as inputs to the step instance node.
	 * 
	 * The exception is also set as an input.
	 * 
	 * @param item
	 *            the agenda item that started
	 * @param trigger
	 *            the exception or message that caused the agenda item to be
	 *            posted
	 */
	@Override
	public void started(AgendaItem item, Serializable trigger) {
		try {
			// Create the step instance node
			Step step = item.getStep();
			System.out.println(step.getName() + " started ");
			StepInstanceNode handlerPred = getHandlerPredecessor(item, step);
			StepInstanceNode sin = createStartStepInstanceNode(item, step);

			addPredSuccLink(handlerPred, sin);
			createDataFlowEdgeForException(sin, trigger);

			createDinsForLocals(item, step, sin);

			if (step.isLeaf()) {
				createDFEdgesForInput(item, step, sin);
			}

			// TODO: Agent connection
		} catch (RootAlreadySetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private DataInstanceNode createDataFlowEdgeForException(
			StepInstanceNode handlerSin, Serializable exception) {
		DataInstanceNode exceptionNode = paramTable.getOriginal(exception);
		handlerSin.addInput("exception", exceptionNode);
		exceptionNode.addUserPIN(handlerSin);
		return exceptionNode;
	}

	/**
	 * Create control flow edges for the predecessor when a step is starting or
	 * is terminated when there is no starting node for that step
	 * 
	 * @param item
	 *            the agenda item being started
	 * @param step
	 *            its step definition
	 * @param sin
	 *            the step instance node being updated
	 * @throws AMSException
	 *             if AMS can't be contacted
	 */
	private StepInstanceNode getHandlerPredecessor(AgendaItem handlerItem,
			Step handlerStep) throws AMSException {

		// Handler is not the root of a requisite
		assert !handlerStep.isRequisite();

		if (handlerStep.hasPrerequisite() || handlerStep.hasPostrequisite()) {
			return virtualStepMapper.getLastPIN(handlerItem);
			// connectVirtualAsPred(handlerItem, sin);
		} else {
			AgendaItem parentItem = handlerItem.getParent();
			// Handler is not the root of the process
			assert parentItem != null;

			// Some child of the parent must have been created for us to
			// get to this handler
			assert parentItem.getChildren().size() >= 1;
			return createParentInterm(parentItem);
		}
	}

	/**
	 * This is called when an exception is handled by a simple handler. We build
	 * a step instance node to represent the simple handler. It consumes the
	 * exception object as input and produces no output. Its predecessor is the
	 * last step to throw the exception. Its successor is a newly-created interm
	 * node for the parent.
	 * 
	 * @param parent the agenda item that the handler is attached to
	 * @param exception the exception being handled
	 */
	@Override
	public void handled(AgendaItem parent, Serializable exception) {
		try {
			System.out.println(exception + " handled with a simple handler");
			StepInstanceNode thrower = lastThrowerTable.get(exception);
			StepInstanceNode handler = new SimpleHandler(provData);
			provData.addPIN(handler);
			handler.addInput("exception", paramTable.getOriginal(exception));

			StepInstanceNode parentNode = childSinMapper.getParentPin(thrower);
			AgendaItem parentItem = agendaItemMapper.getItem(parentNode);

			assert parentItem.equals(parent);
			StepInstanceNode parentInterm = createParentInterm(parent);
			addPredSuccLink(parentInterm, handler);

			childSinMapper.addChild(parentItem, parentNode, handler);
			simpleHandlerMapper.addHandler(parentItem, handler);
		} catch (ParameterAlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
