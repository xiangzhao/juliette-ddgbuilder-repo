package laser.juliette.ddgbuilder.test;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import laser.ddg.DataBindingEvent;
import laser.ddg.DataBindingListener;
import laser.ddg.DataInstanceNode;
import laser.ddg.ProcedureInstanceNode;
import laser.juliette.ams.AMSException;
import laser.juliette.ams.IllegalTransition;
import laser.juliette.ams.UnknownParameter;
import laser.juliette.ddgbuilder.DDGBuilder;
import laser.juliette.ddgbuilder.FinishVirtualNode;
import laser.juliette.ddgbuilder.IntermStepInstanceNode;
import laser.juliette.ddgbuilder.IntermVirtualNode;
import laser.juliette.ddgbuilder.SimpleHandler;
import laser.juliette.ddgbuilder.StartVirtualNode;
import laser.lj.Binding;
import laser.lj.InterfaceDeclaration;
import laser.lj.ScopeBinding;
import laser.lj.ScopeBinding.Mode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DDGBuilderTest {
	private StubAgendaItem rootItem = new StubAgendaItem("root", false, null);
	private StubAgendaItem nonleafItem = new StubAgendaItem ("non-leaf", false, rootItem);
	private StubAgendaItem leafItem = new StubAgendaItem ("leaf", true, nonleafItem);
	private Binding copyBinding;
	
	private DDGBuilder builder;
	private DataBindingListener listener;
	private int listenerCalls;

	@Before
	public void setUp() throws Exception {
		builder = new DDGBuilder("test", null);
		listenerCalls = 0;
		listener = new DataBindingListener () {

			public void bindingCreated(DataBindingEvent e) {
				listenerCalls++;
			}
			
		};
		builder.addDataBindingListener(listener);

		String[] parentFieldAccessors = new String[0]; 
		String[] childFieldAccessors = new String[0]; 
		copyBinding = new ScopeBinding(ScopeBinding.Mode.COPY_IN_AND_OUT, null, parentFieldAccessors, null, childFieldAccessors);

	}
	
	@Test
	public void createRoot() {
		builder.started(rootItem);
		ProcedureInstanceNode rootSin = builder.getRoot();
		Assert.assertNotNull(builder.getLastPIN(rootItem));
		Assert.assertFalse(rootSin.predecessorIter().hasNext());
		Assert.assertFalse(rootSin.successorIter().hasNext());
	}

	@Test
	public void createNonleaf() {
		builder.started(rootItem);
		builder.started(nonleafItem);
		
		ProcedureInstanceNode rootSin = builder.getRoot();
		Assert.assertNotNull(builder.getLastPIN(rootItem));
		
		ProcedureInstanceNode nonleafSin = builder.getLastPIN(nonleafItem);
		Assert.assertTrue(rootSin.successorIter().next() == nonleafSin);

		Assert.assertTrue(nonleafSin.predecessorIter().next() == rootSin);
		Assert.assertFalse(nonleafSin.successorIter().hasNext());
	}

	@Test
	public void createLeaf() {
		builder.started(rootItem);
		builder.started(nonleafItem);
		builder.started(leafItem);
		
		ProcedureInstanceNode rootSin = builder.getRoot();
		Assert.assertNotNull(builder.getLastPIN(rootItem));
		
		ProcedureInstanceNode nonleafSin = builder.getLastPIN(nonleafItem);
		Assert.assertTrue(rootSin.successorIter().next() == nonleafSin);

		Assert.assertTrue(nonleafSin.predecessorIter().next() == rootSin);

		ProcedureInstanceNode leafSin = builder.getLastPIN(leafItem);
		Assert.assertTrue(nonleafSin.successorIter().next() == leafSin);
		Assert.assertTrue(leafSin.predecessorIter().next() == nonleafSin);
		Assert.assertFalse(leafSin.successorIter().hasNext());
		
	}
	
	@Test
	public void completingRoot() {
		builder.started(rootItem);
		ProcedureInstanceNode rootStartSin = builder.getLastPIN(rootItem);
		builder.started(nonleafItem);
		ProcedureInstanceNode nonleafStartSin = builder.getLastPIN(nonleafItem);
		builder.started(leafItem);
		ProcedureInstanceNode leafStartSin = builder.getLastPIN(leafItem);
		
		builder.completing(leafItem);
		builder.completing(nonleafItem);
		builder.completing(rootItem);
		
		ProcedureInstanceNode rootFinishSin = builder.getLastPIN(rootItem);
		ProcedureInstanceNode nonleafFinishSin = builder.getLastPIN(nonleafItem);
		ProcedureInstanceNode leafFinishSin = builder.getLastPIN(leafItem);
		
		Assert.assertSame(leafStartSin, leafFinishSin);
		Assert.assertNotSame(nonleafStartSin, nonleafFinishSin);
		Assert.assertNotSame(rootStartSin, rootFinishSin);
		
		Assert.assertTrue(leafFinishSin.predecessorIter().next() == nonleafStartSin);
		Assert.assertTrue(leafFinishSin.successorIter().next() == nonleafFinishSin);

		Assert.assertTrue(nonleafFinishSin.predecessorIter().next() == leafFinishSin);
		Assert.assertTrue(nonleafFinishSin.successorIter().next() == rootFinishSin);

		Assert.assertTrue(rootFinishSin.predecessorIter().next() == nonleafFinishSin);
		Assert.assertFalse(rootFinishSin.successorIter().hasNext());
	}
	
	@Test
	public void completingParallel() throws AMSException, IllegalTransition {
		StubAgendaItem parallelItem = new StubAgendaItem (true, "parallel", null);
		builder.started(parallelItem);

		StubAgendaItem parChild1 = new StubAgendaItem("parChild1", true, parallelItem);
		builder.started(parChild1);

		StubAgendaItem parChild2 = new StubAgendaItem("parChild2", true, parallelItem);
		builder.started(parChild2);
		
		ProcedureInstanceNode parStartSin = builder.getLastPIN(parallelItem);
		ProcedureInstanceNode parChild1Sin = builder.getLastPIN(parChild1);
		ProcedureInstanceNode parChild2Sin = builder.getLastPIN(parChild2);
		
		Assert.assertTrue(parChild1Sin.predecessorIter().next() == parStartSin);
		Assert.assertTrue(parChild2Sin.predecessorIter().next() == parStartSin);
		
		Iterator<ProcedureInstanceNode> parallelSuccIter = parStartSin.successorIter();
		Assert.assertTrue(parallelSuccIter.next() == parChild1Sin);
		Assert.assertTrue(parallelSuccIter.next() == parChild2Sin);
		Assert.assertFalse(parallelSuccIter.hasNext());
		
		builder.completing(parChild1);
		builder.completed(parChild1);
		parChild1.complete();
		builder.completing(parChild2);
		builder.completed(parChild2);
		parChild2.complete();
		builder.completing(parallelItem);
		
		ProcedureInstanceNode parFinishSin = builder.getLastPIN(parallelItem);
		Assert.assertTrue(parChild1Sin.successorIter().next() == parFinishSin);
		Assert.assertTrue(parChild2Sin.successorIter().next() == parFinishSin);
		
		Iterator<ProcedureInstanceNode> parallelFinPredIter = parFinishSin.predecessorIter();
		ProcedureInstanceNode firstPred = parallelFinPredIter.next();
		if (firstPred == parChild1Sin) {
			Assert.assertTrue(parallelFinPredIter.next() == parChild2Sin);
		}
		else if (firstPred == parChild2Sin){
			Assert.assertTrue(parallelFinPredIter.next() == parChild1Sin);
		}
		else {
			assert false;
		}
		Assert.assertFalse(parallelFinPredIter.hasNext());
	}

	@Test
	public void completingNotParallel() {
		StubAgendaItem notParallelItem = new StubAgendaItem ("not parallel", false, null);
		
		builder.started(notParallelItem);
		ProcedureInstanceNode parentStartSin = builder.getLastPIN(notParallelItem);

		StubAgendaItem child1 = new StubAgendaItem("child1", true, notParallelItem);
		builder.started(child1);
		builder.completing(child1);
		StubAgendaItem child2 = new StubAgendaItem("child2", true, notParallelItem);
		builder.started(child2);
		
		ProcedureInstanceNode parentIntermSin = builder.getLastPIN(notParallelItem);
		ProcedureInstanceNode child1Sin = builder.getLastPIN(child1);
		ProcedureInstanceNode child2Sin = builder.getLastPIN(child2);
		
		Assert.assertTrue(child1Sin.predecessorIter().next() == parentStartSin);
		Assert.assertTrue(child2Sin.predecessorIter().next() == parentIntermSin);
		
		Iterator<ProcedureInstanceNode> parentStartSuccIter = parentStartSin.successorIter();
		Assert.assertTrue(parentStartSuccIter.next() == child1Sin);
		Assert.assertFalse(parentStartSuccIter.hasNext());
		Iterator<ProcedureInstanceNode> parentIntermSuccIter = parentIntermSin.successorIter();
		Assert.assertTrue(parentIntermSuccIter.next() == child2Sin);
		Assert.assertFalse(parentIntermSuccIter.hasNext());
		
		builder.completing(child2);
		builder.completing(notParallelItem);
		
		ProcedureInstanceNode parentFinishSin = builder.getLastPIN(notParallelItem);
		Assert.assertTrue(child2Sin.successorIter().next() == parentFinishSin);
		
		Iterator<ProcedureInstanceNode> parentFinPredIter = parentFinishSin.predecessorIter();
		Assert.assertTrue(parentFinPredIter.next() == child2Sin);
		Assert.assertFalse(parentFinPredIter.hasNext());
	}
	
	@Test
	public void startedPrereqRoot() {
		rootItem = new StubAgendaItem("root", false, null);
		nonleafItem = new StubAgendaItem ("non-leaf", false, rootItem);
		StubAgendaItem prereqItem = new StubAgendaItem ("prereq", true, null, true);
		leafItem = new StubAgendaItem ("leaf", true, nonleafItem, prereqItem);

		builder.started(rootItem);
		builder.started(nonleafItem);
		builder.starting(leafItem);
		builder.started(prereqItem);
		
		ProcedureInstanceNode prereqStartSin = builder.getLastPIN(prereqItem);
		ProcedureInstanceNode virtualStartSin = prereqStartSin.predecessorIter().next();
		Assert.assertNotNull(virtualStartSin);
		Assert.assertEquals(StartVirtualNode.class, virtualStartSin.getClass());
		Assert.assertEquals(virtualStartSin.successorIter().next(), prereqStartSin);
	}
	
	@Test
	public void completedPrereqRoot() {
		rootItem = new StubAgendaItem("root", false, null);
		nonleafItem = new StubAgendaItem ("non-leaf", false, rootItem);
		StubAgendaItem prereqItem = new StubAgendaItem ("prereq", true, null, true);
		leafItem = new StubAgendaItem ("leaf", true, nonleafItem, prereqItem);
	
		builder.started(rootItem);
		builder.started(nonleafItem);
		builder.starting(leafItem);
		builder.started(prereqItem);
		builder.completing(prereqItem);
		builder.completed(prereqItem);
		
		ProcedureInstanceNode prereqFinishSin = builder.getLastPIN(prereqItem);
		ProcedureInstanceNode virtualIntermSin = prereqFinishSin.successorIter().next();
		Assert.assertNotNull(virtualIntermSin);
		Assert.assertEquals(IntermVirtualNode.class, virtualIntermSin.getClass());
		Assert.assertEquals(virtualIntermSin.predecessorIter().next(), prereqFinishSin);
	}
	
	@Test
	public void startedStepWithPrereq() {
		rootItem = new StubAgendaItem("root", false, null);
		nonleafItem = new StubAgendaItem ("non-leaf", false, rootItem);
		StubAgendaItem prereqItem = new StubAgendaItem ("prereq", true, null, true);
		leafItem = new StubAgendaItem ("leaf", true, nonleafItem, prereqItem);
	
		builder.started(rootItem);
		builder.started(nonleafItem);
		builder.starting(leafItem);
		builder.started(prereqItem);
		builder.completing(prereqItem);
		builder.completed(prereqItem);
		builder.started(leafItem);
		
		ProcedureInstanceNode leafStartSin = builder.getLastPIN(leafItem);
		ProcedureInstanceNode virtualIntermSin = leafStartSin.predecessorIter().next();
		Assert.assertNotNull(virtualIntermSin);
		Assert.assertEquals(IntermVirtualNode.class, virtualIntermSin.getClass());
		Assert.assertEquals(virtualIntermSin.successorIter().next(), leafStartSin);
	}

	@Test
	public void completingStepWithPostreq() {
		rootItem = new StubAgendaItem("root", false, null);
		nonleafItem = new StubAgendaItem ("non-leaf", false, rootItem);
		StubAgendaItem postreqItem = new StubAgendaItem ("postreq", true, null, true);
		leafItem = new StubAgendaItem ("leaf", true, nonleafItem, null, postreqItem);
	
		builder.started(rootItem);
		builder.started(nonleafItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		builder.completing(leafItem);
		
		ProcedureInstanceNode leafFinishSin = builder.getLastPIN(leafItem);
		ProcedureInstanceNode virtualIntermSin = leafFinishSin.successorIter().next();
		Assert.assertNotNull(virtualIntermSin);
		Assert.assertEquals(IntermVirtualNode.class, virtualIntermSin.getClass());
		Assert.assertEquals(virtualIntermSin.predecessorIter().next(), leafFinishSin);
	}

	@Test
	public void startedPostreq() {
		rootItem = new StubAgendaItem("root", false, null);
		nonleafItem = new StubAgendaItem ("non-leaf", false, rootItem);
		StubAgendaItem postreqItem = new StubAgendaItem ("postreq", true, null, true);
		leafItem = new StubAgendaItem ("leaf", true, nonleafItem, null, postreqItem);
	
		builder.started(rootItem);
		builder.started(nonleafItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		builder.completing(leafItem);
		builder.starting(postreqItem);
		builder.started(postreqItem);
		
		ProcedureInstanceNode postreqStartSin = builder.getLastPIN(postreqItem);
		ProcedureInstanceNode virtualIntermSin = postreqStartSin.predecessorIter().next();
		Assert.assertNotNull(virtualIntermSin);
		Assert.assertEquals(IntermVirtualNode.class, virtualIntermSin.getClass());
		Assert.assertEquals(virtualIntermSin.successorIter().next(), postreqStartSin);
	}

	@Test
	public void completedPostreq() {
		rootItem = new StubAgendaItem("root", false, null);
		nonleafItem = new StubAgendaItem ("non-leaf", false, rootItem);
		StubAgendaItem postreqItem = new StubAgendaItem ("postreq", true, null, true);
		leafItem = new StubAgendaItem ("leaf", true, nonleafItem, null, postreqItem);
	
		builder.started(rootItem);
		builder.started(nonleafItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		builder.completing(leafItem);
		builder.starting(postreqItem);
		builder.started(postreqItem);
		builder.completing(postreqItem);
		builder.completed(postreqItem);
		
		ProcedureInstanceNode postreqFinishSin = builder.getLastPIN(postreqItem);
		ProcedureInstanceNode virtualFinishSin = postreqFinishSin.successorIter().next();
		Assert.assertNotNull(virtualFinishSin);
		Assert.assertEquals(FinishVirtualNode.class, virtualFinishSin.getClass());
		Assert.assertEquals(virtualFinishSin.predecessorIter().next(), postreqFinishSin);
	}

	@Test
	public void completedVirtual() {
		rootItem = new StubAgendaItem("root", false, null);
		nonleafItem = new StubAgendaItem ("non-leaf", false, rootItem);
		StubAgendaItem postreqItem = new StubAgendaItem ("postreq", true, null, true);
		leafItem = new StubAgendaItem ("leaf", true, nonleafItem, null, postreqItem);
	
		builder.started(rootItem);
		builder.started(nonleafItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		builder.completing(leafItem);
		builder.starting(postreqItem);
		builder.started(postreqItem);
		builder.completing(postreqItem);
		builder.completed(postreqItem);
		builder.completed(leafItem);
		builder.completing(nonleafItem);
		builder.completed(nonleafItem);
		
		ProcedureInstanceNode nonleafFinishSin = builder.getLastPIN(nonleafItem);
		ProcedureInstanceNode virtualFinishSin = nonleafFinishSin.predecessorIter().next();
		Assert.assertNotNull(virtualFinishSin);
		Assert.assertEquals(FinishVirtualNode.class, virtualFinishSin.getClass());
		Assert.assertEquals(virtualFinishSin.successorIter().next(), nonleafFinishSin);
	}
	
	@Test
	public void mimicLJilTest() {
		rootItem = new StubAgendaItem("ReqRoot", false, null);
		StubAgendaItem prereqItem = new StubAgendaItem("Prereq", true, null, true);
		StubAgendaItem postreqItem = new StubAgendaItem("Postreq", true, null, true);
		StubAgendaItem reqLeafItem = new StubAgendaItem("ReqLeaf", true, rootItem, prereqItem, postreqItem);
		
		System.out.println("\n\n\nMimicing LJil test");
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(reqLeafItem);
		builder.starting(prereqItem);
		builder.started(prereqItem);
		builder.completing(prereqItem);
		builder.completed(prereqItem);
		builder.started(reqLeafItem);
		builder.completing(reqLeafItem);
		builder.starting(postreqItem);
		builder.started(postreqItem);
		builder.completing(postreqItem);
		builder.completed(postreqItem);
		builder.completed(reqLeafItem);
		builder.completing(rootItem);
		builder.completed(rootItem);
	}
	
	@Test
	public void localParameterTest() {
		rootItem = new StubAgendaItem ("Root", true, null);
		rootItem.addLocalParameter("p", new Serializable(){});
		
		builder.starting(rootItem);
		builder.started(rootItem);
		
		ProcedureInstanceNode rootNode = builder.getLastPIN(rootItem);
		DataInstanceNode din = rootNode.getOutput("p");
		Assert.assertNotNull(din);
		Assert.assertEquals(din.getProducer(), rootNode);
		Assert.assertEquals(1, listenerCalls);
	}
	
	@Test
	public void leafOutputTest() {
		rootItem = new StubAgendaItem ("Root", false, null);
		leafItem = new StubAgendaItem ("Leaf", true, rootItem);
		leafItem.addOutputParameter("p");
		
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		try {
			leafItem.setParameter("p", new Serializable(){});
		} catch (AMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownParameter e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		builder.completing(leafItem);
		builder.completed(leafItem);
		
		ProcedureInstanceNode leafNode = builder.getLastPIN(leafItem);
		DataInstanceNode din = leafNode.getOutput("p");
		Assert.assertNotNull(din);
		Assert.assertEquals(din.getProducer(), leafNode);
		Assert.assertEquals(1, listenerCalls);
	}
	
	@Test
	public void paramCopyTest() {
		Serializable original = new Serializable(){};
		Serializable copy1 = new Serializable(){};

		rootItem = new StubAgendaItem ("Root", false, null);
		rootItem.addLocalParameter("pRoot", original);
		leafItem = new StubAgendaItem ("Leaf", true, rootItem);
		leafItem.addInputParameter("pLeaf", copy1);
		
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(leafItem);
		
		builder.copied(original, copy1, copyBinding, Mode.COPY_IN);

		builder.started(leafItem);
		
		ProcedureInstanceNode leafNode = builder.getLastPIN(leafItem);
		DataInstanceNode din = leafNode.getInput("pLeaf");
		Assert.assertNotNull(din);
		
		ProcedureInstanceNode rootNode = builder.getLastPIN(rootItem);
		Assert.assertTrue(din.getProducer() == rootNode);
		Assert.assertTrue(usersOfDinIncludePin (din, leafNode));
		
		
		builder.completing(leafItem);
		builder.completed(leafItem);
		builder.completing(rootItem);
		builder.completed(rootItem);
		
		Assert.assertEquals(2, listenerCalls);
	}

	private boolean usersOfDinIncludePin(DataInstanceNode din,
			ProcedureInstanceNode pin) {
		Iterator<ProcedureInstanceNode> users = din.users();
		while (users.hasNext()) {
			ProcedureInstanceNode user = users.next();
			if (user == pin) {
				return true;
			}
		}
		return false;
	}
	
	@Test
	public void paramPassThroughTest() {
		Serializable original = new Serializable(){};
		Serializable copy1 = new Serializable(){};
		Serializable copy2 = new Serializable(){};

		rootItem = new StubAgendaItem ("Root", false, null);
		rootItem.addLocalParameter("pRoot", original);
		nonleafItem = new StubAgendaItem("Nonleaf", false, rootItem);
		nonleafItem.addInputParameter("pNon", copy1);
		leafItem = new StubAgendaItem ("Leaf", true, nonleafItem);
		leafItem.addInputParameter("pLeaf", copy2);
		
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(nonleafItem);
		builder.copied(original, copy1, copyBinding, Mode.COPY_IN);
		builder.started(nonleafItem);
		builder.starting(leafItem);
		
		builder.copied(copy1, copy2, copyBinding, Mode.COPY_IN);

		builder.started(leafItem);
		
		ProcedureInstanceNode leafNode = builder.getLastPIN(leafItem);
		DataInstanceNode din = leafNode.getInput("pLeaf");
		Assert.assertNotNull(din);
		
		ProcedureInstanceNode rootNode = builder.getLastPIN(rootItem);
		Assert.assertTrue(din.getProducer() == rootNode);
		Assert.assertTrue(usersOfDinIncludePin (din, leafNode));
		
		
		builder.completing(leafItem);
		builder.completed(leafItem);
		builder.completing(nonleafItem);
		builder.completed(nonleafItem);
		builder.completing(rootItem);
		builder.completed(rootItem);
		
		Assert.assertEquals(2, listenerCalls);
	}

	@Test
	public void paramPassUpAndDownTest() {
		Serializable original = new Serializable(){};
		Serializable copy1 = new Serializable(){};
		Serializable copy2 = new Serializable(){};
		Serializable copy3 = new Serializable(){};

		rootItem = new StubAgendaItem ("Root", false, null);
		rootItem.addLocalParameter("pRoot", original);
		StubAgendaItem leaf1Item = new StubAgendaItem("Leaf1", true, rootItem);
		leaf1Item.addInOutParameter("pLeaf1", copy1);
		
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(leaf1Item);
		builder.copied(original, copy1, copyBinding, Mode.COPY_IN);
		builder.started(leaf1Item);
		builder.completing(leaf1Item);
		builder.completed(leaf1Item);
		builder.copied(copy1, copy2, copyBinding, Mode.COPY_OUT);

		StubAgendaItem leaf2Item = new StubAgendaItem ("Leaf2", true, rootItem);
		leaf2Item.addInputParameter("pLeaf2", copy2);

		builder.starting(leaf2Item);
		builder.copied(copy2, copy3, copyBinding, Mode.COPY_IN);
		builder.started(leaf2Item);
		
		ProcedureInstanceNode leaf2Node = builder.getLastPIN(leaf2Item);
		DataInstanceNode din = leaf2Node.getInput("pLeaf2");
		Assert.assertNotNull(din);
		
		ProcedureInstanceNode leaf1Node = builder.getLastPIN(leaf1Item);
		Assert.assertTrue(din.getProducer() == leaf1Node);
		Assert.assertTrue(usersOfDinIncludePin (din, leaf2Node));
		
		
		builder.completing(leaf2Item);
		builder.completed(leaf2Item);
		builder.completing(rootItem);
		builder.completed(rootItem);
		
		Assert.assertEquals(4, listenerCalls);
	}
	
	@Test
	public void paramDataModifiedTest() {
		class ParamType implements Serializable {
			private int x = 0;
			
			public void incX() {
				x++;
			}
		}
		ParamType original = new ParamType(){};
		ParamType copy1 = new ParamType(){};
		ParamType copy2 = new ParamType(){};
		ParamType copy3 = new ParamType(){};

		rootItem = new StubAgendaItem ("Root", false, null);
		rootItem.addLocalParameter("pRoot", original);
		StubAgendaItem leaf1Item = new StubAgendaItem("Leaf1", true, rootItem);
		leaf1Item.addInOutParameter("pLeaf1", copy1);
		
		
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(leaf1Item);
		builder.copied(original, copy1, copyBinding, Mode.COPY_IN);
		copy1.incX();
		try {
			leaf1Item.setParameter("pLeaf1", copy1);
		} catch (AMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownParameter e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		builder.started(leaf1Item);
		builder.completing(leaf1Item);
		builder.completed(leaf1Item);
		builder.copied(copy1, copy2, copyBinding, Mode.COPY_OUT);

		StubAgendaItem leaf2Item = new StubAgendaItem ("Leaf2", true, rootItem);
		leaf2Item.addInputParameter("pLeaf2", copy2);

		builder.starting(leaf2Item);
		builder.copied(copy2, copy3, copyBinding, Mode.COPY_IN);
		builder.started(leaf2Item);
		
		ProcedureInstanceNode leaf2Node = builder.getLastPIN(leaf2Item);
		DataInstanceNode din = leaf2Node.getInput("pLeaf2");
		Assert.assertNotNull(din);
		
		ProcedureInstanceNode leaf1Node = builder.getLastPIN(leaf1Item);
		Assert.assertTrue(din.getProducer() == leaf1Node);
		Assert.assertTrue(usersOfDinIncludePin (din, leaf2Node));
		
		
		builder.completing(leaf2Item);
		builder.completed(leaf2Item);
		builder.completing(rootItem);
		builder.completed(rootItem);
		
		Assert.assertEquals(4, listenerCalls);
	}

	@Test
	public void leafThrowException() {
		rootItem = new StubAgendaItem ("Root", false, null);
		leafItem = new StubAgendaItem ("Leaf", true, rootItem);
		
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		
		Set<Serializable> exceptions = createException();
		builder.terminated(leafItem, exceptions);
		
		ProcedureInstanceNode leafNode = builder.getLastPIN(leafItem);
		String exceptionName = getExceptionName(exceptions);
		DataInstanceNode din = leafNode.getOutput(exceptionName);
		Assert.assertNotNull(din);

	}

	private Set<Serializable> createException() {
		Set<Serializable> exceptions = new HashSet<Serializable>();
		Serializable exception = new Serializable(){};
		exceptions.add(exception);
		return exceptions;
	}
	
	@Test
	public void propagateException() {
		System.out.println("\n\n*** Propagate test ***");
		rootItem = new StubAgendaItem ("Root", false, null);
		leafItem = new StubAgendaItem ("Leaf", true, rootItem);
		
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		
		Set<Serializable> exceptions = createException();
		builder.terminated(leafItem, exceptions);
		
		builder.terminated(rootItem, exceptions);
		
		ProcedureInstanceNode leafNode = builder.getLastPIN(leafItem);
		ProcedureInstanceNode rootNode = builder.getLastPIN(rootItem);
		Assert.assertEquals(rootNode, leafNode.successorIter().next());
		Assert.assertEquals(leafNode, rootNode.predecessorIter().next());
	}

	@Test
	public void nonleafThrowExceptionAfterStarting() {
		rootItem = new StubAgendaItem ("Root", false, null);
		nonleafItem = new StubAgendaItem ("Nonleaf", false, rootItem);
		leafItem = new StubAgendaItem ("Leaf", true, nonleafItem);
		
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(nonleafItem);
		
		Set<Serializable> exceptions = createException();
		builder.terminated(nonleafItem, exceptions);
		
		ProcedureInstanceNode nonleafNode = builder.getLastPIN(nonleafItem);
		String exceptionName = getExceptionName(exceptions);
		DataInstanceNode din = nonleafNode.getOutput(exceptionName);
		Assert.assertNotNull(din);

	}

	private String getExceptionName(Set<Serializable> exceptions) {
		Serializable exception = exceptions.iterator().next();
		String exceptionName = exception.getClass().getName();
		return exceptionName;
	}
	
	@Test
	public void nonleafThrowExceptionBeforeStarting() {
		System.out.println("\n\n*** Resource unknown exception");
		rootItem = new StubAgendaItem ("Root", false, null);
		nonleafItem = new StubAgendaItem ("Nonleaf", false, rootItem);
		leafItem = new StubAgendaItem ("Leaf", true, nonleafItem);
		
		builder.starting(rootItem);
		builder.started(rootItem);
		
		Set<Serializable> exceptions = createException();
		builder.terminated(nonleafItem, exceptions);
		
		ProcedureInstanceNode nonleafNode = builder.getLastPIN(nonleafItem);
		String exceptionName = getExceptionName(exceptions);
		DataInstanceNode din = nonleafNode.getOutput(exceptionName);
		Assert.assertNotNull(din);

	}
	
	@Test
	public void nonleafThrowExceptionAfterChildStarted() {
		System.out.println("\n\n*** Nonleaf throwing exception after child has started");
		rootItem = new StubAgendaItem ("Root", false, null);
		nonleafItem = new StubAgendaItem ("Nonleaf", false, rootItem);
		leafItem = new StubAgendaItem ("Leaf", true, nonleafItem);
		
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(nonleafItem);
		builder.started(nonleafItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		
		// Act as if we have nonleaf is a try step and with a continue handler but there
		// are no more steps.
		Set<Serializable> exceptions = createException();
		builder.terminated(leafItem, exceptions);
		
		builder.handled(nonleafItem, exceptions.iterator().next());
		
		Set<Serializable> exceptions2 = createException();
		builder.terminated(nonleafItem, exceptions2);
		
		
		ProcedureInstanceNode nonleafNode = builder.getLastPIN(nonleafItem);
		String exceptionName = getExceptionName(exceptions);
		DataInstanceNode din = nonleafNode.getOutput(exceptionName);
		Assert.assertNotNull(din);

	}
	
	@Test
	// I don't think this sequence can happen.  A parallel step will not
	// throw a new exception after a child has started.
	public void parallelThrowExceptionAfterStarting() {
		rootItem = new StubAgendaItem ("Root", false, null);
		StubAgendaItem parallelItem = new StubAgendaItem (true, "parallel", rootItem);
		leafItem = new StubAgendaItem ("Leaf", true, parallelItem);
		
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(parallelItem);
		
		Set<Serializable> exceptions = createException();
		builder.terminated(parallelItem, exceptions);
		
		ProcedureInstanceNode nonleafNode = builder.getLastPIN(parallelItem);
		String exceptionName = getExceptionName(exceptions);
		DataInstanceNode din = nonleafNode.getOutput(exceptionName);
		Assert.assertNotNull(din);

	}
	
	@Test
	// I don't think this sequence can happen.  A parallel step will not
	// throw a new exception after a child has started.
	public void parallelThrowExceptionAfterChildTerminated() {
		rootItem = new StubAgendaItem ("Root", false, null);
		StubAgendaItem parallelItem = new StubAgendaItem (true, "parallel", rootItem);
		leafItem = new StubAgendaItem ("Leaf", true, parallelItem);
		
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(parallelItem);
		builder.started(parallelItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		
		Set<Serializable> exceptions = createException();
		builder.terminated(leafItem, exceptions);

		Set<Serializable> exceptions2 = createException();
		builder.terminated(parallelItem, exceptions2);
		
		ProcedureInstanceNode nonleafNode = builder.getLastPIN(parallelItem);
		String exceptionName = getExceptionName(exceptions);
		DataInstanceNode din = nonleafNode.getOutput(exceptionName);
		Assert.assertNotNull(din);

	}
	
	@Test
	public void stepWithPrereqThrowingException() {
		System.out.println("\n\n*** Step with prereq throwing exception");
		rootItem = new StubAgendaItem ("Root", false, null);
		StubAgendaItem prereqItem = new StubAgendaItem ("prereq", true, null, true);
		nonleafItem = new StubAgendaItem ("Nonleaf", false, rootItem, prereqItem);
		leafItem = new StubAgendaItem ("Leaf", true, nonleafItem);
		
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(nonleafItem);
		builder.starting(prereqItem);
		builder.started(prereqItem);
		builder.completing(prereqItem);
		builder.completed(prereqItem);
		builder.started(nonleafItem);
		ProcedureInstanceNode nonleafNode = builder.getLastPIN(nonleafItem);

		builder.starting(leafItem);
		builder.started(leafItem);
		
		ProcedureInstanceNode leafNode = builder.getLastPIN(leafItem);
		Assert.assertEquals(nonleafNode, leafNode.predecessorIter().next());
		Assert.assertEquals(leafNode, nonleafNode.successorIter().next());

		// Act as if we have nonleaf is a try step and with a continue handler but there
		// are no more steps.
		Set<Serializable> exceptions = createException();
		builder.terminated(leafItem, exceptions);
		leafNode = builder.getLastPIN(leafItem);
		Assert.assertEquals(nonleafNode, leafNode.predecessorIter().next());
		Assert.assertEquals(leafNode, nonleafNode.successorIter().next());
		
		builder.handled(nonleafItem, exceptions.iterator().next());
		
		Set<Serializable> exceptions2 = createException();
		builder.terminated(nonleafItem, exceptions2);
		
		
		nonleafNode = builder.getLastPIN(nonleafItem);
		String exceptionName = getExceptionName(exceptions);
		DataInstanceNode din = nonleafNode.getOutput(exceptionName);
		Assert.assertNotNull(din);
		
		builder.terminated(rootItem, exceptions2);
	}
	
	@Test
	public void prereqTerminated() {
		System.out.println("\n\n*** Prereq terminated");
		rootItem = new StubAgendaItem ("Root", false, null);
		StubAgendaItem prereqItem = new StubAgendaItem ("prereq", true, null, true);
		nonleafItem = new StubAgendaItem ("Nonleaf", false, rootItem, prereqItem);
		leafItem = new StubAgendaItem ("Leaf", true, nonleafItem);
		
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(nonleafItem);
		builder.starting(prereqItem);
		builder.started(prereqItem);
		
		// Act as if we have nonleaf is a try step and with a continue handler but there
		// are no more steps.
		Set<Serializable> exceptions = createException();
		builder.terminated(prereqItem, exceptions);
		
		ProcedureInstanceNode prereqNode = builder.getLastPIN(prereqItem);
		String exceptionName = getExceptionName(exceptions);
		DataInstanceNode din = prereqNode.getOutput(exceptionName);
		Assert.assertNotNull(din);
		
		builder.terminated(nonleafItem, exceptions);
		builder.terminated(rootItem, exceptions);
		
		ProcedureInstanceNode rootNode = builder.getLastPIN(rootItem);
		ProcedureInstanceNode prereqSucc = prereqNode.successorIter().next();
		ProcedureInstanceNode rootPred = rootNode.predecessorIter().next();
		Assert.assertEquals(prereqSucc, rootPred);
		Assert.assertEquals(rootPred, prereqSucc);
	}

	@Test
	public void handlerStarted() {
		rootItem = new StubAgendaItem ("Root", false, null);
		leafItem = new StubAgendaItem ("Leaf", true, rootItem);

		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		
		Set<Serializable> exceptions = createException();
		builder.terminated(leafItem, exceptions);

		StubAgendaItem handlerItem = new StubAgendaItem ("Handler", true, rootItem);
		builder.starting(handlerItem, exceptions.iterator().next());
		builder.started(handlerItem, exceptions.iterator().next());
		
		ProcedureInstanceNode leafNode = builder.getLastPIN(leafItem);
		String exceptionName = getExceptionName(exceptions);
		DataInstanceNode din = leafNode.getOutput(exceptionName);
		ProcedureInstanceNode handlerNode = builder.getLastPIN(handlerItem);
		
		Assert.assertEquals(din, handlerNode.getInput("exception"));
		Assert.assertTrue(din.getProducer() == leafNode);
		Assert.assertTrue(usersOfDinIncludePin (din, handlerNode));
		
		ProcedureInstanceNode intermParentSin = handlerNode.predecessorIter().next();
		Assert.assertEquals(intermParentSin, leafNode.successorIter().next());
	}

	// TODO
	// Need to test control flow after a handler completes.  Consider all possible continuations
	// and the possibility that the handler terminates.
	
	/* A continuation after the last child has completed should give the same
	 * events from the interpreter.
	 */
	@Test
	public void handlerCompletion() {
		rootItem = new StubAgendaItem ("Root", false, null);
		leafItem = new StubAgendaItem ("Leaf", true, rootItem);

		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		
		Set<Serializable> exceptions = createException();
		builder.terminated(leafItem, exceptions);

		StubAgendaItem handlerItem = new StubAgendaItem ("Handler", true, rootItem);
		builder.starting(handlerItem, exceptions.iterator().next());
		builder.started(handlerItem, exceptions.iterator().next());
		builder.completing(handlerItem);
		builder.completed(handlerItem);
		
		// Assume handler has completion semantics
		builder.completing(rootItem);
		builder.completed(rootItem);
		
		ProcedureInstanceNode handlerNode = builder.getLastPIN(handlerItem);
		ProcedureInstanceNode rootNode = builder.getLastPIN(rootItem);
		Assert.assertEquals(rootNode, handlerNode.successorIter().next());
		Assert.assertEquals(handlerNode, rootNode.predecessorIter().next());
	}
	
	@Test
	public void handlerContinue() {
		rootItem = new StubAgendaItem ("Root", false, null);
		leafItem = new StubAgendaItem ("Leaf", true, rootItem);

		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		
		Set<Serializable> exceptions = createException();
		builder.terminated(leafItem, exceptions);

		StubAgendaItem handlerItem = new StubAgendaItem ("Handler", true, rootItem);
		builder.starting(handlerItem, exceptions.iterator().next());
		builder.started(handlerItem, exceptions.iterator().next());
		builder.completing(handlerItem);
		builder.completed(handlerItem);
		
		// Assume handler has continue semantics
		StubAgendaItem leaf2Item = new StubAgendaItem ("Leaf2", true, rootItem);
		builder.starting(leaf2Item);
		builder.started(leaf2Item);
		
		ProcedureInstanceNode handlerNode = builder.getLastPIN(handlerItem);
		ProcedureInstanceNode rootNode = builder.getLastPIN(rootItem);
		Assert.assertEquals(rootNode, handlerNode.successorIter().next());
		Assert.assertEquals(handlerNode, rootNode.predecessorIter().next());

		ProcedureInstanceNode leaf2Node = builder.getLastPIN(leaf2Item);
		Assert.assertEquals(rootNode, leaf2Node.predecessorIter().next());
		Assert.assertEquals(leaf2Node, rootNode.successorIter().next());
		
	}

	/* We will see the same event stream if the handler creates and throws
	 * a new exception.
	 */
	@Test
	public void handlerRethrow() {
		rootItem = new StubAgendaItem ("Root", false, null);
		leafItem = new StubAgendaItem ("Leaf", true, rootItem);

		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		
		Set<Serializable> exceptions = createException();
		builder.terminated(leafItem, exceptions);

		StubAgendaItem handlerItem = new StubAgendaItem ("Handler", true, rootItem);
		builder.starting(handlerItem, exceptions.iterator().next());
		builder.started(handlerItem, exceptions.iterator().next());
		builder.completing(handlerItem);
		builder.completed(handlerItem);
		
		// Assume handler has rethrow semantics
		builder.terminated(rootItem, exceptions);
		
		ProcedureInstanceNode rootNode = builder.getLastPIN(rootItem);
		ProcedureInstanceNode handlerNode = builder.getLastPIN(handlerItem);
		Assert.assertEquals(rootNode, handlerNode.successorIter().next());
		Assert.assertEquals(handlerNode, rootNode.predecessorIter().next());
		
	}

	/* This test does not currently pass.  We need to manufacture a final node for the 
	 * step that did not complete.  (At least that is Lee's opinion.)
	 */
	@Test
	public void handlerRestart() {
		rootItem = new StubAgendaItem ("Root", false, null);
		leafItem = new StubAgendaItem ("Leaf", true, rootItem);

		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		
		Set<Serializable> exceptions = createException();
		builder.terminated(leafItem, exceptions);

		StubAgendaItem handlerItem = new StubAgendaItem ("Handler", true, rootItem);
		builder.starting(handlerItem, exceptions.iterator().next());
		builder.started(handlerItem, exceptions.iterator().next());
		builder.completing(handlerItem);
		builder.completed(handlerItem);
		
		// Assume handler has restart semantics
		StubAgendaItem rootRestartItem = new StubAgendaItem ("Root", false, null);
		builder.starting(rootRestartItem);
		builder.started(rootRestartItem);
		
		ProcedureInstanceNode rootNode = builder.getLastPIN(rootItem);
		ProcedureInstanceNode handlerNode = builder.getLastPIN(handlerItem);
		Assert.assertEquals(rootNode, handlerNode.successorIter().next());
		Assert.assertEquals(handlerNode, rootNode.predecessorIter().next());
		
		ProcedureInstanceNode rootRestartNode = builder.getLastPIN(rootRestartItem);
		Assert.assertEquals(rootNode, rootRestartNode.predecessorIter().next());
		Assert.assertEquals(rootRestartNode, rootNode.successorIter().next());
		
	}
	
	@Test
	public void simpleHandlerStarted() {
		rootItem = new StubAgendaItem ("Root", false, null);
		leafItem = new StubAgendaItem ("Leaf", true, rootItem);

		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		
		Set<Serializable> exceptions = createException();
		builder.terminated(leafItem, exceptions);

		builder.handled(rootItem, exceptions.iterator().next());
		
		ProcedureInstanceNode leafNode = builder.getLastPIN(leafItem);
		
		ProcedureInstanceNode leafSucc = leafNode.successorIter().next();
		Assert.assertTrue(leafSucc instanceof IntermStepInstanceNode);

		ProcedureInstanceNode handlerNode = leafSucc.successorIter().next();
		Assert.assertTrue(handlerNode instanceof SimpleHandler);
		ProcedureInstanceNode handlerPred = handlerNode.predecessorIter().next(); 
		Assert.assertEquals(leafSucc, handlerPred);
		Assert.assertTrue(!handlerNode.outputParamNames().hasNext());
		
		String exceptionName = getExceptionName(exceptions);
		DataInstanceNode exceptionNode = leafNode.getOutput(exceptionName);
		Assert.assertEquals(exceptionNode, handlerNode.getInput("exception"));
	}

	@Test
	public void simpleHandlerContinue() {
		rootItem = new StubAgendaItem ("Root", false, null);
		leafItem = new StubAgendaItem ("Leaf", true, rootItem);

		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		
		Set<Serializable> exceptions = createException();
		builder.terminated(leafItem, exceptions);

		builder.handled(rootItem, exceptions.iterator().next());
		
		nonleafItem = new StubAgendaItem("Chidl2", false, rootItem);
		builder.starting(nonleafItem);
		builder.started(nonleafItem);
		
		ProcedureInstanceNode leafNode = builder.getLastPIN(leafItem);
		ProcedureInstanceNode rootNode = builder.getLastPIN(rootItem);
		ProcedureInstanceNode leafSucc = leafNode.successorIter().next();
		Assert.assertTrue(leafSucc instanceof IntermStepInstanceNode);

		ProcedureInstanceNode handlerNode = leafSucc.successorIter().next();
		Assert.assertTrue(handlerNode instanceof SimpleHandler);
		ProcedureInstanceNode handlerPred = handlerNode.predecessorIter().next(); 
		Assert.assertEquals(leafSucc, handlerPred);

		Assert.assertEquals(handlerNode, rootNode.predecessorIter().next());
		Assert.assertEquals(rootNode, handlerNode.successorIter().next());
		
		ProcedureInstanceNode child2Node = builder.getLastPIN(nonleafItem);
		Assert.assertEquals(child2Node, rootNode.successorIter().next());
		Assert.assertEquals(rootNode, child2Node.predecessorIter().next());
		
	}

	@Test
	public void simpleHandlerComplete() {
		rootItem = new StubAgendaItem ("Root", false, null);
		leafItem = new StubAgendaItem ("Leaf", true, rootItem);

		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		
		Set<Serializable> exceptions = createException();
		builder.terminated(leafItem, exceptions);

		builder.handled(rootItem, exceptions.iterator().next());
		builder.completing(rootItem);
		builder.completed(rootItem);
		
		ProcedureInstanceNode leafNode = builder.getLastPIN(leafItem);
		ProcedureInstanceNode rootNode = builder.getLastPIN(rootItem);
		ProcedureInstanceNode leafSucc = leafNode.successorIter().next();
		Assert.assertTrue(leafSucc instanceof IntermStepInstanceNode);

		ProcedureInstanceNode handlerNode = leafSucc.successorIter().next();
		Assert.assertTrue(handlerNode instanceof SimpleHandler);
		ProcedureInstanceNode handlerPred = handlerNode.predecessorIter().next(); 
		Assert.assertEquals(leafSucc, handlerPred);

		Assert.assertEquals(handlerNode, rootNode.predecessorIter().next());
		Assert.assertEquals(rootNode, handlerNode.successorIter().next());
	}

	@Test
	public void simpleHandlerRethrow() {
		rootItem = new StubAgendaItem ("Root", false, null);
		leafItem = new StubAgendaItem ("Leaf", true, rootItem);

		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		
		Set<Serializable> exceptions = createException();
		builder.terminated(leafItem, exceptions);

		// Interpreter does not send us this because it can't distinguish
		// between a propagate and a simple handler rethrow
		//builder.handled(rootItem, exceptions.iterator().next());
		builder.terminated(rootItem, exceptions);
		
		ProcedureInstanceNode leafNode = builder.getLastPIN(leafItem);
		ProcedureInstanceNode rootNode = builder.getLastPIN(rootItem);
		ProcedureInstanceNode leafSucc = leafNode.successorIter().next();
		Assert.assertEquals(leafSucc, rootNode);
	}

	@Test
	public void postreqTerminated() {
		System.out.println("\n\n*** Postreq terminated");
		rootItem = new StubAgendaItem ("Root", false, null);
		StubAgendaItem postreqItem = new StubAgendaItem ("postreq", true, null, true);
		nonleafItem = new StubAgendaItem ("Nonleaf", false, rootItem, null, postreqItem);
		leafItem = new StubAgendaItem ("Leaf", true, nonleafItem);
		
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(nonleafItem);
		builder.started(nonleafItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		builder.completing(leafItem);
		builder.completed(leafItem);

		builder.starting(postreqItem);
		builder.started(postreqItem);
		
		// Act as if we have nonleaf is a try step and with a continue handler but there
		// are no more steps.
		Set<Serializable> exceptions = createException();
		builder.terminated(postreqItem, exceptions);
		
		ProcedureInstanceNode postreqNode = builder.getLastPIN(postreqItem);
		String exceptionName = getExceptionName(exceptions);
		DataInstanceNode din = postreqNode.getOutput(exceptionName);
		Assert.assertNotNull(din);
		
		builder.terminated(nonleafItem, exceptions);
		builder.terminated(rootItem, exceptions);
		
		ProcedureInstanceNode rootNode = builder.getLastPIN(rootItem);
		ProcedureInstanceNode postreqSucc = postreqNode.successorIter().next();
		ProcedureInstanceNode rootPred = rootNode.predecessorIter().next();
		Assert.assertEquals(postreqSucc, rootPred);
		Assert.assertEquals(rootPred, postreqSucc);
	}

	@Test
	public void stepWithPostreqThrowingException() {
		System.out.println("\n\n*** Step with postreq throwing exception");
		rootItem = new StubAgendaItem ("Root", false, null);
		StubAgendaItem postreqItem = new StubAgendaItem ("postreq", true, null, true);
		nonleafItem = new StubAgendaItem ("Nonleaf", false, rootItem, null, postreqItem);
		leafItem = new StubAgendaItem ("Leaf", true, nonleafItem);
		
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(nonleafItem);
		builder.started(nonleafItem);
		ProcedureInstanceNode nonleafNode = builder.getLastPIN(nonleafItem);

		builder.starting(leafItem);
		builder.started(leafItem);
		
		ProcedureInstanceNode leafNode = builder.getLastPIN(leafItem);
		Assert.assertEquals(nonleafNode, leafNode.predecessorIter().next());
		Assert.assertEquals(leafNode, nonleafNode.successorIter().next());

		// Act as if we have nonleaf is a try step and with a continue handler but there
		// are no more steps.
		Set<Serializable> exceptions = createException();
		builder.terminated(leafItem, exceptions);
		leafNode = builder.getLastPIN(leafItem);
		Assert.assertEquals(nonleafNode, leafNode.predecessorIter().next());
		Assert.assertEquals(leafNode, nonleafNode.successorIter().next());
		
		builder.handled(nonleafItem, exceptions.iterator().next());
		
		Set<Serializable> exceptions2 = createException();
		builder.terminated(nonleafItem, exceptions2);
		
		
		nonleafNode = builder.getLastPIN(nonleafItem);
		String exceptionName = getExceptionName(exceptions);
		DataInstanceNode din = nonleafNode.getOutput(exceptionName);
		Assert.assertNotNull(din);
		
		builder.terminated(rootItem, exceptions2);
	}
	
	@Test
	public void childOfParallelThrowException() throws AMSException, IllegalTransition {
		rootItem = new StubAgendaItem ("Root", false, null);
		StubAgendaItem parallelItem = new StubAgendaItem (true, "parallel", rootItem);
		leafItem = new StubAgendaItem ("Leaf", true, parallelItem);
		
		builder.starting(rootItem);
		builder.started(rootItem);
		builder.starting(parallelItem);
		builder.started(parallelItem);
		builder.starting(leafItem);
		builder.started(leafItem);
		
		Set<Serializable> exceptions = createException();
		builder.terminated(leafItem, exceptions);
		
		builder.handled(parallelItem, exceptions.iterator().next());
		
		StubAgendaItem leaf2Item = new StubAgendaItem ("Leaf2", true, parallelItem);
		builder.starting(leaf2Item);
		builder.started(leaf2Item);
		builder.completing(leaf2Item);
		builder.completed(leaf2Item);
		leaf2Item.complete();

		builder.completing(parallelItem);
		builder.completed(parallelItem);
		
		ProcedureInstanceNode parallelNode = builder.getLastPIN(parallelItem);
		ProcedureInstanceNode leaf1Node = builder.getLastPIN(leafItem);
		ProcedureInstanceNode leaf2Node = builder.getLastPIN(leaf2Item);
		
		Assert.assertEquals(parallelNode, leaf2Node.successorIter().next());
		Iterator<ProcedureInstanceNode> parallelPredIter = parallelNode.predecessorIter();
		
		// Not surprised that this fails.  One predecessor should be leaf2 and one predecesor
		// should be the simple handler but right now the stub agenda item code doesn't know 
		// about the handler.  Need to see if this works with the interpreter.  No.  It does
		// not find the handler either.
		ProcedureInstanceNode parallelPred1 = parallelPredIter.next();
		ProcedureInstanceNode parallelPred2 = parallelPredIter.next();
		Assert.assertEquals(leaf2Node, parallelPred1);
		Assert.assertEquals(leaf1Node.successorIter().next().successorIter().next(), parallelPred2);
		
		builder.completing(rootItem);
		builder.completed(rootItem);
	}

	

}