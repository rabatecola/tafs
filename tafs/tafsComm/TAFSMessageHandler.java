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
//TODO RA: Rework to use exceptions properly
public class TAFSMessageHandler
{
	private TAFSCommHandler	myCH;

	public TAFSMessageHandler(TAFSCommHandler inCH)
	{
		//Constructor takes a CommHandler, which is assumed to have an open connection
		myCH = inCH;
	}

	public void Close()
	{
		myCH.Close();
	}

	public TAFSMessage ReadMessage() throws IOException, ClassNotFoundException
	{
		TAFSMessage	aMsg = null;

		// Reads a message object from CommHandler.
//		try {
			aMsg = (TAFSMessage)myCH.ReadObject();
//		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		return aMsg;
	}

	public void SendMessage(TAFSMessage inMsg) throws IOException
	{
		// Writes a message object to CommHandler.
//		try {
			myCH.WriteObject(inMsg);
			myCH.Flush();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public void SendMessage(String inMsg, ArrayList<String> inArgs, byte[] inPayload) throws IOException
	{
		TAFSMessage	cMessage = new TAFSMessage();

	  cMessage.myMsg = inMsg;
		cMessage.myArgs = inArgs;
		cMessage.myPayload = inPayload;

		SendMessage(cMessage);
	}

	public String GetRemoteIP()
	{
		return myCH != null ? myCH.GetRemoteIP() : "";
	}
}
