package laser.juliette.ddgbuilder;

/**
 * * No virtual node exception
 * 
 * @author S. Taskova
 */
public class NoVirtualNodeException extends IllegalArgumentException {
	/**
	 * used for serializable objects in order to verify that the same version of
	 * the class definition is used to serialize the object as to unserialize it
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param s
	 */
	public NoVirtualNodeException(String s) {
		super(s);
	}
}
