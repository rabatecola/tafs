/**
 * 
 */
package tafsComm;

import java.io.Serializable;
import java.net.Socket;

/**
 * @author robert
 *
 */
public class TAFSCommHandler
{
	Integer	myPortNumber;
	Socket	mySocket;

	public TAFSCommHandler(Integer inPortNumber)
	{
		myPortNumber = inPortNumber;
	}

	public Socket Listen()
	{
		// Use ServerSocket to listen for incoming connection
		return null;
	}

	public Socket Open(String inIPAddr)
	{
		// Use Socket to create an outgoing connection
		return null;
	}

	public void Close()
	{
	}

	public Serializable ReadObject()
	{
		// Read a serialized object from the open socket
		return null;
	}

	public void WriteObject(Serializable inObject)
	{
		// Write a serializable object to the open socket
	}
}
