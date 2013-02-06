package laser.juliette.ddg.query;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import laser.ddg.DataInstanceNode;
import laser.ddg.ProcedureInstanceNode;
import laser.ddg.ProvenanceData;
import laser.ddg.persist.JenaLoader;
import laser.ddg.query.Query;
import laser.ddg.visualizer.PrefuseGraphBuilder;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Asks the user which variable and which value of the variable to
 * show the derivation for.  Extracts the partial DDG from the database
 * and displays it.
 * 
 * @author Barbara Lerner
 * @version Aug 2, 2012
 *
 */
public class DerivationQuery implements Query {
	private JenaLoader dbLoader;
	private String processName;
	private String timestamp;
	private JComboBox valueMenu;
	
	private List<Resource> allDinsToShow = new ArrayList<Resource>();
	private List<Resource> allPinsToShow = new ArrayList<Resource>();
	private List<Resource> dinResourceList = new ArrayList<Resource>();

	/**
	 * The value to display in the query menu
	 */
	@Override
	public String getMenuItem() {
		return "Show Value Derivation";
	}

	/**
	 * Performs the query.
	 * @param dbLoader the object that can query the db
	 * @param processName the name of the process whose DDG is searched
	 * @param timestamp the timestamp of the DDG to search
	 */
	@Override
	public void performQuery(JenaLoader dbLoader, String processName,
			String timestamp, Component invokingComponent) {
		this.dbLoader = dbLoader;
		this.processName = processName;
		this.timestamp = timestamp;
		
		SortedSet<String> dinNames = dbLoader.getAllDinNames(processName, timestamp);
		
		Vector<String> names = new Vector<String>();
		for (String dinName : dinNames) {
			names.add(dinName);
		}
		
		final JFrame queryFrame = new JFrame ("Derivation query");
		final JPanel varQueryPanel = new JPanel();
		final JComboBox nameMenu = new JComboBox(names);
		
		nameMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				showValuesOf (nameMenu.getSelectedItem().toString());
				valueMenu.setEnabled(true);
			}
			
		});
		
		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = valueMenu.getSelectedIndex();
				showDerivationOf (dinResourceList.get(index));
				queryFrame.dispose();
			}


		});
		okButton.setEnabled(false);
		
		JButton cancelButton = new JButton ("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				queryFrame.setVisible(false);
			}
			
		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		
		JLabel varTitle = new JLabel("Select a variable...");
		varQueryPanel.setLayout(new BorderLayout());
		varQueryPanel.add(varTitle, BorderLayout.NORTH);
		varQueryPanel.add(nameMenu, BorderLayout.CENTER);
		
		JPanel valueQueryPanel = new JPanel();
		valueQueryPanel.setLayout(new BorderLayout());
		JLabel valueTitle = new JLabel("Select a value...");
		valueQueryPanel.add(valueTitle, BorderLayout.NORTH);
		valueMenu = new JComboBox();
		valueMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				okButton.setEnabled(true);
			}
			
		});
		valueQueryPanel.add(valueMenu, BorderLayout.CENTER);
		valueMenu.setEnabled(false);
		
		queryFrame.getContentPane().add(varQueryPanel, BorderLayout.WEST);
		queryFrame.getContentPane().add(valueQueryPanel, BorderLayout.EAST);
		queryFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		queryFrame.pack();
		queryFrame.setLocationRelativeTo(invokingComponent);
		queryFrame.setVisible(true);
		
	}
	
	private void showValuesOf(String dinName) {
		valueMenu.removeAllItems();
		SortedSet<Resource> dins = dbLoader.getDinsNamed(processName, timestamp, dinName);
		
		for (Resource din : dins) {
			valueMenu.addItem(dbLoader.retrieveDinValue(din));
			dinResourceList.add(din);
		}
		
	}

	private void showDerivationOf(Resource qResource) {
		allDinsToShow.clear();
		allPinsToShow.clear();
		allDinsToShow.add(qResource);
		
		for (int i = 0; i < allDinsToShow.size(); i++) {
			Resource nextDataResource = allDinsToShow.get(i);
			Resource nextProcResource = dbLoader.getProducer(processName, timestamp, nextDataResource);
			allPinsToShow.add(nextProcResource);
			if (!dbLoader.retrieveSinName(nextProcResource).equals("Get Q")) {
				addAllInputs(nextProcResource, allDinsToShow);
			}
		}
		displayDDG();
		
	}
	
	private void addAllInputs(Resource procRes, List<Resource> dataResources) {
		String queryVarName = "in";
		ResultSet inputs = dbLoader.getAllInputs(processName, timestamp, dbLoader.retrieveSinId(procRes), queryVarName);
		while (inputs.hasNext()) {
			QuerySolution inputSolution = inputs.next();
			Resource inputResource = inputSolution.getResource(queryVarName);
			
			if (!dataResources.contains(inputResource)) {
				dataResources.add(inputResource);
			}
		}

		
	}
	
	private void displayDDG() {
		final ProvenanceData provData = new ProvenanceData(processName); 
		provData.addProvenanceListener(new PrefuseGraphBuilder());
		new Thread() {
			@Override
			public void run() {
				// Load the database in a separate thread so that it does not tie
				// up the Swing thread.  This allows us to see the DDG being 
				// built incrementally as it is read from the DB.
				loadQueryResult(provData);				
			}
		}.start();
	}

	private void loadQueryResult(ProvenanceData pd) {
		pd.notifyProcessStarted(processName);
		
		Collections.sort(allPinsToShow, new Comparator<Resource>() {

			@Override
			public int compare(Resource res0, Resource res1) {
				// TODO Auto-generated method stub
				return dbLoader.retrieveSinId(res0) - dbLoader.retrieveSinId(res1);
			}
			
		});
		
		for (Resource res : allPinsToShow) {
			ProcedureInstanceNode pin = dbLoader.addProcResourceToProvenance(res, pd);

			String queryVarName = "in";
			ResultSet inputs = dbLoader.getAllInputs(processName, timestamp, pin.getId(), queryVarName);
			while (inputs.hasNext()) {
				QuerySolution inputSolution = inputs.next();
				Resource inputResource = inputSolution.getResource(queryVarName);
				if (allDinsToShow.contains(inputResource)) {
					DataInstanceNode din = (DataInstanceNode) pd.getNodeForResource(inputResource.getURI());
					pin.addInput(din.getName(), din);
					din.addUserPIN(pin);
				}
			}

			queryVarName = "out";
			ResultSet outputs = dbLoader.getAllOutputs(processName, timestamp, pin.getId(), queryVarName);
			while (outputs.hasNext()) {
				QuerySolution outputSolution = outputs.next();
				Resource outputResource = outputSolution.getResource(queryVarName);
				if (allDinsToShow.contains(outputResource)) {
					DataInstanceNode din = dbLoader.addDataResourceToProvenance(pin, outputResource, pd);
					pin.addOutput(din.getName(), din);
				}
			}
		}
		
		pd.notifyProcessFinished();

	}

}
