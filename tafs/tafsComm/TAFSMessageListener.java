/**
 * 
 */
package tafsComm;

import java.net.Socket;

/**
 * @author Robert Abatecola
 *
 */
public class TAFSMessageListener
{
	private Socket	mySocket;

	public void TAFSMessageListener(Socket inSocket)
	{
		mySocket = inSocket;
	}

	/**
	 * Reads a serialized TAFSMessage from mySocket.
	 *
	 * @return			The TAFSMessage object that was read
	 * @see				TAFSMessage
	 */
	public TAFSMessage ReadMessage()
	{
		return null;
	}

	/**
	 * Serializes a TAFSMessage and writes it to mySocket.
	 *
	 * @return			The TAFSMessage object that was read
	 * @see				TAFSMessage
	 */
	public void SendMessage(TAFSMessage inMsg)
	{
	}

	/**
	 * Creates a TAFSMessage from arguments and writes it to mySocket.
	 *
	 * @param  inMsg		a string message to send
	 * @param  inArgs		a string array containing arguments for the message
	 * @param  inPayload	a byte array containing data to send
	 */
	public void SendMessage(String inMsg, String[] inArgs, byte[] inPayload)
	{
		TAFSMessage	aMessage = new TAFSMessage();

		aMessage.myMsg = inMsg;
		aMessage.myArgs = inArgs;
		aMessage.myPayload = inPayload;

		SendMessage(aMessage);
	}
}
