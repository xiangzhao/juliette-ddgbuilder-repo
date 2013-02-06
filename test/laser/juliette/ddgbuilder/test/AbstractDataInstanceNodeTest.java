package laser.juliette.ddgbuilder.test;

import java.util.Set;

import laser.ddg.AgentConfiguration;
import laser.ddg.ProcedureInstanceNode;
import laser.ddg.ProvenanceData;
import laser.juliette.ddgbuilder.DataInstanceNode;
import laser.juliette.ddgbuilder.StepInstanceNode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AbstractDataInstanceNodeTest {

	private ProvenanceData provData;
	private ProcedureInstanceNode pin1;
	private ProcedureInstanceNode pin2;
	private ProcedureInstanceNode pin3;
	private DataInstanceNode din1;
	private DataInstanceNode din2;
	private DataInstanceNode din3;
	private DataInstanceNode din4;
	private DataInstanceNode din5;

	@Before
	public void setUp() throws Exception {
		provData = new ProvenanceData("test");
		AgentConfiguration agent = new AgentConfiguration("agent name", "agent version");
		provData.addAgent(agent);
		
		// Create 2 PINs
		pin1 = new StepInstanceNode(new StubStep("pin1",false), agent, provData);
		pin2 = new StepInstanceNode(new StubStep("pin2",false), agent, provData);
		pin3 = new StepInstanceNode(new StubStep("pin3",false), agent, provData);
		provData.addPIN(pin1);
		provData.addPIN(pin2);
		provData.addPIN(pin3);
		
		// Create  DINs
		din1 = new DataInstanceNode("din1", "din1", pin1, provData);
		din2 = new DataInstanceNode("din2", "din2", pin2, provData);
		din3 = new DataInstanceNode("din3", "din3", pin2, provData);
		din4 = new DataInstanceNode("din4", "din4", pin3, provData);
		din5 = new DataInstanceNode("din5", "din5", pin1, provData);
		provData.addDIN(din1);
		provData.addDIN(din2);
		provData.addDIN(din3);
		provData.addDIN(din4);
		provData.addDIN(din5);
		
		// Pass din1 from pin1 to pin2
		pin1.addOutput("d1", din1);
		pin2.addInput("d1", din1);
		din1.addUserPIN(pin2);
		
		// Pass din5 from pin1 to pin2
		pin1.addOutput("d5", din5);
		pin2.addInput("d5", din5);
		din5.addUserPIN(pin2);
		
		// Pass din1 from pin1 to pin3
		pin3.addInput("d1", din1);
		din1.addUserPIN(pin3);
		
		// Make din2 and din3 an output of pin2
		pin2.addOutput("d2", din2);
		pin2.addOutput("d3", din3);
		
		// Make din4 an output of pin3
		pin3.addOutput("d4", din4);

		// Make din1 & din5 a process input and din2 & din4 a process output
		provData.addInputDIN(din1);
		provData.addInputDIN(din5);
		provData.addOutputDIN(din2);
		provData.addOutputDIN(din4);
	}

	@Test
	public void testGetProcessOutputsDerived() {
		Set<laser.ddg.DataInstanceNode> derivedOutputs = din1.getProcessOutputsDerived();
		Assert.assertTrue(derivedOutputs.size() == 2);
		Assert.assertTrue(derivedOutputs.contains(din2));
		Assert.assertTrue(derivedOutputs.contains(din4));
	}

	@Test
	public void testGetProcessInputsDerived() {
		Set<laser.ddg.DataInstanceNode> inputsDerivedFrom = din2.getProcessInputsDerived();
		Assert.assertTrue(inputsDerivedFrom.size() == 2);
		Assert.assertTrue(inputsDerivedFrom.contains(din1));
		Assert.assertTrue(inputsDerivedFrom.contains(din5));
	}

}
