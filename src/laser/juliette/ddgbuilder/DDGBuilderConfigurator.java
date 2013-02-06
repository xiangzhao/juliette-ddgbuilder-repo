package laser.juliette.ddgbuilder;

import laser.juliette.jul.EntryPointNotFoundException;
import laser.juliette.jul.JulFile;
import laser.juliette.runtime.InitializationError;
import laser.juliette.runtime.RuntimeConfigurator;
import laser.juliette.runtime.RuntimeFactory;

/**
 * @author B. Lerner
 * 
 */
public class DDGBuilderConfigurator implements RuntimeConfigurator {
	/**
	 * Initializes the DDG builder.
	 *  
	 */
	@Override
	public void visit(RuntimeFactory factory, JulFile jul)
		throws InitializationError {
		String ddgPropertyValue = jul.getStringProperty("ddg");
		if (ddgPropertyValue == null || ddgPropertyValue.equals("off")) {
			return;
		}

		String processName;
		try {
			processName = jul.getEntryPoint().getStepName();
		} catch (EntryPointNotFoundException e) {
			processName = "";
		}
		
		factory.setDDGBuilder(new DDGBuilder(processName, jul));
		
	}

}
