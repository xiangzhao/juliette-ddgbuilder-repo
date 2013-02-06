package laser.juliette.ddg.gui;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import laser.ddg.ProvenanceData;
import laser.ddg.persist.RdfModelFactory;
import laser.ddg.query.Query;
import laser.ddg.visualizer.PrefuseGraphBuilder;
import laser.juliette.ddg.persist.JenaLoader;
import laser.juliette.ddg.query.DerivationQuery;

/**
 * Class with a main program that allows the user to view DDGs previously stored in 
 * a Jena database.  The user selects which execution of which process to see a DDG of.
 * 
 * @author Barbara Lerner
 * @version Jul 25, 2012
 *
 */
public class DDGExplorer extends JPanel {
	// The menu that allows the user to select a DDG based on its execution time
	private JComboBox selectTimestampMenu;
	
	private JComboBox selectQueryMenu;
	
	// An area where messages could be displayed.
	private JTextArea log;
	
	// The process that the user selected.
	private String selectedProcessName;
	
	// The timestamp for the DDG that the user selected.
	private String selectedTimestamp;
	
	// The object that loads the DDG from a Jena database
	private JenaLoader jenaLoader;
	
	//private Query queryObj;

	/**
	 * Creates the contents of the main GUI window.
	 */
	public DDGExplorer() {
		super(new BorderLayout());

		// Create the log first, because the action listeners
		// need to refer to it.
		log = new JTextArea(5, 20);
		log.setMargin(new Insets(5, 5, 5, 5));
		log.setEditable(false);
		
		// DON'T DELETE THIS:  Sample of how to load a query using reflection
//        try {
//			ClassLoader classLoader = getClass().getClassLoader();
//			Class queryClass = classLoader.loadClass("laser.juliette.ddg.gui.QDerivationQuery");
//			queryObj = 
//			        (Query) queryClass.newInstance();
//		} catch (InstantiationException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IllegalAccessException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}


		// Create a menu that contains the names of each process that contains
		// DDGs in the database.
		final JComboBox selectProcessMenu = new JComboBox();
		selectProcessMenu.addItem("Select a Process...");
		jenaLoader = new JenaLoader(RdfModelFactory.JENA_DIRECTORY);
		List<String> processNames = jenaLoader.getAllProcessNames();
		for (String processName : processNames) {
			selectProcessMenu.addItem(processName);
		}
		selectProcessMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				selectedProcessName = selectProcessMenu.getSelectedItem().toString();
				selectedTimestamp = null;
				updateTimestampMenu(selectedProcessName);
				selectTimestampMenu.setEnabled(true);
			}

		});

		selectTimestampMenu = new JComboBox();
		selectTimestampMenu.addItem("Select execution...");
		selectTimestampMenu.setEnabled(false);
		this.selectTimestampMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				selectedTimestamp = selectTimestampMenu.getSelectedItem().toString();
				selectQueryMenu.setEnabled(true);
			}
			
		});
		
		selectQueryMenu = new JComboBox();
		selectTimestampMenu.addItem("Select query...");
		selectQueryMenu.addItem("Show entire DDG");
		//selectQueryMenu.addItem(queryObj.getMenuItem());
		final Query derivationQuery = new DerivationQuery();
		selectQueryMenu.addItem (derivationQuery.getMenuItem());
		selectQueryMenu.setEnabled(false);
		selectQueryMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				String selectedItem = selectQueryMenu.getSelectedItem().toString();
				if (selectedItem.equals("Show entire DDG")) {
					displayDDG();
				}
				else if (selectedItem.equals(derivationQuery.getMenuItem())){
					derivationQuery.performQuery(jenaLoader, selectedProcessName, selectedTimestamp, DDGExplorer.this);
				}
				
			}
			
		});

		// For layout purposes, put the menus in a separate panel
		JPanel menuPanel = new JPanel(); // use FlowLayout
		menuPanel.add(selectProcessMenu);
		menuPanel.add(selectTimestampMenu);
		menuPanel.add(selectQueryMenu);
		add(menuPanel, BorderLayout.PAGE_START);

		JScrollPane logScrollPane = new JScrollPane(log);
		add(logScrollPane, BorderLayout.CENTER);
	}
	
	/**
	 * Load a DDG from a database and display it.
	 */
	private void displayDDG() {
		if (selectedProcessName == null || selectedTimestamp == null) {
			JOptionPane.showMessageDialog(this, "Please select a process and execution timestamp to display.");
			return;
		}
		
		final ProvenanceData provData = new ProvenanceData(selectedProcessName); 
		provData.addProvenanceListener(new PrefuseGraphBuilder());
		new Thread() {
			@Override
			public void run() {
				// Load the database in a separate thread so that it does not tie
				// up the Swing thread.  This allows us to see the DDG being 
				// built incrementally as it is read from the DB.
				jenaLoader.loadDDG(selectedProcessName, selectedTimestamp, provData);				
			}
		}.start();
	}

	/**
	 * Update the entries in the timestamp menu to be consistent with the selected process.
	 * @param selectedProcessName the name of the process the user wants to see DDGs for
	 */
	private void updateTimestampMenu(String selectedProcessName) {
		for (int index = selectTimestampMenu.getItemCount() - 1; index > 0; index--) {
			selectTimestampMenu.removeItemAt(index);
		}
		List<String> timestamps = jenaLoader.getTimestamps(selectedProcessName);
		for (String timestamp : timestamps) {
			selectTimestampMenu.addItem(timestamp);
		}
	}
	

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event dispatch thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("DDG Chooser");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add content to the window.
		frame.add(new DDGExplorer());

		// Display the window.
		frame.setSize(800, 400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		createAndShowGUI();
	}
}

