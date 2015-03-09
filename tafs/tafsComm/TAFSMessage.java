/**
 * 
 */
package tafsComm;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Robert Abatecola
 *
 */
public class TAFSMessage implements Serializable
{
	private static final long	serialVersionUID = 543;

	public String				myMsg;
	public ArrayList<String>	myArgs = new ArrayList<String>();
	public byte[]				myPayload = null;
}
