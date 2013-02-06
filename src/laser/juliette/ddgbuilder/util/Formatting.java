package laser.juliette.ddgbuilder.util;

/**
 * Keeps track of indentation information, used for the textual representation
 * of a DDG (currently not in use b/c the DDG is not being "drawn" as a tree and
 * there are no changes in the indentation).
 * 
 * @author Sophia
 * 
 */
public class Formatting {
	private int numSpaces;
	private static final int INDENT = 5;

	/**
	 * Construct a default object
	 */
	public Formatting() {
		numSpaces = 0;
	}

	/**
	 * Indent to the right
	 */
	public void add1Level() {
		numSpaces += INDENT;
	}

	/**
	 * Can be used to give a constant indentation of DINs if they are to be
	 * drawn in the DDG in addition to the SINs
	 */
	public void dinLevel() {
		numSpaces = 5 * INDENT;
	}

	/**
	 * Indent to the left
	 */
	public void sub1Level() {
		numSpaces -= INDENT;
	}

	/**
	 * Print out the indentation
	 * 
	 * @param text
	 *            text to be printed using the indentaion information in the
	 *            formatting class
	 */
	public void output(String text) {
		for (int i = 0; i < numSpaces; i++) {
			System.out.print(" ");
		}
		System.out.print(text);
	}

}
