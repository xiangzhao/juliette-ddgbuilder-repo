package laser.juliette.ddgbuilder;

/**
 * * Agenda item is a leaf exception
 * 
 * @author S. Taskova
 */
public class AiIsLeafException extends IllegalArgumentException {
	/**
	 * used for serializable objects in order to verify that the same version of
	 * the class definition is used to serialize the object as to unserialize it
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param s
	 */
	public AiIsLeafException(String s) {
		super(s);
	}
}
