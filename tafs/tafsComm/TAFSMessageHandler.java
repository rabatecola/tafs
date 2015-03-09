/**
 * 
 */
package tafsComm;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Mahitha Thokala
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
		TAFSMessage	aMsg = null;

		// Reads a message object from CommHandler.
		try {
			aMsg = (TAFSMessage)myCH.ReadObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return aMsg;
	}

	public void SendMessage(TAFSMessage inMsg)
	{
		// Writes a message object to CommHandler.
		try {
			myCH.WriteObject(inMsg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void SendMessage(String inMsg, ArrayList<String> inArgs, byte[] inPayload)
	{
		TAFSMessage	cMessage = new TAFSMessage();

	  cMessage.myMsg = inMsg;
		cMessage.myArgs = inArgs;
		cMessage.myPayload = inPayload;

		SendMessage(cMessage);
	}
}
