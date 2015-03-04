/**
 * 
 */
package tafsComm;

import java.io.Serializable;

/**
 * @author Robert Abatecola
 *
 */
public class TAFSMessage implements Serializable
{
	private static final long	serialVersionUID = 543;

	String		myMsg;
	String[]	myArgs;
	byte[]		myPayload;
}
