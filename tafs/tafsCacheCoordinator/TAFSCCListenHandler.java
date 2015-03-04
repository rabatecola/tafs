/**
 * 
 */
package tafsCacheCoordinator;

import java.net.Socket;

import tafs.TAFSCatalog;

/**
 * @author Robert Abatecola
 *
 */
public class TAFSCCListenHandler implements Runnable
{
	Socket		mySocket;
	TAFSCatalog	myCatalog;
	String		myMsg;

	public TAFSCCListenHandler(Socket inSocket, TAFSCatalog inCatalog, String inMsg)
	{
		mySocket = inSocket;
		myCatalog = inCatalog;
		myMsg = inMsg;

		// Start me up
		new Thread(this).start();
	}

	public void run()
	{
		try
		{
			Thread.sleep(1350);
		}
		catch (InterruptedException eIE)
		{
		}

		System.out.println(myMsg);
	}
}
