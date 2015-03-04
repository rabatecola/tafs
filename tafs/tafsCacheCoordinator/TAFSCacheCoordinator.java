/**
 * 
 */
package tafsCacheCoordinator;

//import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

import tafs.TAFSCatalog;

/**
 * @author Robert Abatecola
 *
 */
public class TAFSCacheCoordinator
{
	public TAFSCacheCoordinator()
	{
//		myCat = new TAFSCatalog();
	}

	public static void main(String[] args) throws InterruptedException
	{
		TAFSCatalog			myCat = new TAFSCatalog();
		ServerSocket		myServerSocket;
		Socket				mySocket = new Socket();
		long				tempCounter = 0;
//		TAFSCCMsgHandler	aMsgHandler;

		System.out.println("Entered " + TAFSCacheCoordinator.class.getSimpleName());

		while (true)
		{
			// Wait for message... (TCP Listen?)
			System.out.print(TAFSCacheCoordinator.class.getSimpleName() + "(" + tempCounter + "): Waiting for message...");
			Thread.sleep(1000);
			System.out.println();

			// Spin off thread to handle message
			System.out.println(TAFSCacheCoordinator.class.getSimpleName() + ": Received message, executing thread.");

			/*aMsgHandler = */new TAFSCCListenHandler(mySocket, myCat, "Thread for loop #" + tempCounter);

			tempCounter++;
			if (tempCounter >= 10)
				break;
		}

		System.out.println("Exited " + TAFSCacheCoordinator.class.getSimpleName());
	}

//	public void PutFile(String inFileName, byte[] inBytes)
//	{
//		myCat.SetFileEntry(inFileName, "127.0.0.1");
//	}

//	public String GetFileLocation(String inFileName)
//	{
////		String	theCacheHandlerAddr = 
//
////		NotifyCacheHandler(theCacheHandlerAddr, inFileName);
//
//		return myCat.GetFileEntryServerID(inFileName);
//	}

//	private void NotifyCacheHandler(String inCHAddr, String inClientAddr, String inFileName)
//	{
//		//SendMessageToCH
//	}
}
