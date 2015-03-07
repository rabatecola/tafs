/**
 * 
 */
package tafsCacheCoordinator;

import java.net.Socket;

import pre_dev.TAFSMessageListener;
import tafs.TAFSCatalog;
import tafs.TAFSCommands;
import tafsComm.TAFSCommHandler;
import tafsComm.TAFSMessage;
import tafsComm.TAFSMessageHandler;

/**
 * @author Robert Abatecola
 *
 */
public class TAFSCCThread implements Runnable
{
	TAFSCommHandler	myCH;
	TAFSCatalog		myCatalog;
	String			myMsg;

	public TAFSCCThread(TAFSCommHandler inCH, TAFSCatalog inCatalog, String inMsg)
	{
		myCH = inCH;
		myCatalog = inCatalog;
		myMsg = inMsg;

		// Start me up
		new Thread(this).start();
	}

	public void run()
	{
		TAFSMessageHandler	aMH;
		TAFSMessage			aMsg;
		String				dummyMsg = "";
		TAFSCommands		aCmd;

		try
		{
			aMH = new TAFSMessageHandler(myCH);
			aMsg = aMH.ReadMessage();

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
