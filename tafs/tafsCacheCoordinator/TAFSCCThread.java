/**
 * 
 */
package tafsCacheCoordinator;

import java.net.Socket;

import pre_dev.TAFSMessageListener;
import tafs.TAFSCatalog;
import tafs.TAFSCommands;
import tafsComm.TAFSMessage;

/**
 * @author Robert Abatecola
 *
 */
public class TAFSCCThread implements Runnable
{
	Socket		mySocket;
	TAFSCatalog	myCatalog;
	String		myMsg;

	public TAFSCCThread(Socket inSocket, TAFSCatalog inCatalog, String inMsg)
	{
		mySocket = inSocket;
		myCatalog = inCatalog;
		myMsg = inMsg;

		// Start me up
		new Thread(this).start();
	}

	public void run()
	{
		TAFSMessageListener	aML;
		TAFSMessage			aMsg;
		String				dummyMsg = "";
		TAFSCommands		aCmd;

		try
		{
			aML = new TAFSMessageListener(mySocket);
			aMsg = aML.ReadMessage();

			//dummyMsg = aMsg.myMsg;
			aCmd = TAFSCommands.valueOf(dummyMsg.toLowerCase());
			switch (aCmd)
			{
				case getfile:
					break;

				case putfile:
					break;

				case delfile:
					break;

				default:
					break;
			}
			Thread.sleep(1350);
		}
		catch (InterruptedException eIE)
		{
		}

		System.out.println(myMsg);
	}
}
