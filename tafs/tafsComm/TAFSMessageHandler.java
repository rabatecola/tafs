/**
 * 
 */
package tafsComm;

/**
 * @author Robert Abatecola
 *
 */
public class TAFSMessageHandler
{
	TAFSCommHandler	myCH;

	public TAFSMessageHandler(TAFSCommHandler inCH)
	{
		//Constructor takes a CommHandler, which is assumed to have an open connection
		myCH = inCH;
	}

	public TAFSMessage ReadMessage()
	{
		// Reads a message object from CommHandler.
		return null;
	}

	public void SendMessage(TAFSMessage inMsg)
	{
		// Writes a message object to CommHandler.
	}

	public void SendMessage(String inMsg, String[] inArgs, byte[] inPayload)
	{
		TAFSMessage	aMessage = new TAFSMessage();

		aMessage.myMsg = inMsg;
		aMessage.myArgs = inArgs;
		aMessage.myPayload = inPayload;

		SendMessage(aMessage);
	}
}
