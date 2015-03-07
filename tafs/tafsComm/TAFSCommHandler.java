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
	private Integer	myPortNumber;
	private Socket	mySocket;

	public TAFSCommHandler(Integer inPortNumber)
	{
		myPortNumber = inPortNumber;
	}

	public void SetSocket(Socket inSocket)
	{
		mySocket = inSocket;
	}

	public TAFSCommHandler Listen()
	{
		// Use ServerSocket to listen for incoming connection.
		// Return a new comm. handler with the open socket.
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
