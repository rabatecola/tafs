/**
 * Waits for incoming communication (via TAFSCommHandler) and starts up
 * TAFSCCThread threads to handle each connection
 */
package tafsCacheCoordinator;

//import java.io.IOException;

import tafs.TAFSCatalog;
import tafsComm.TAFSCommHandler;

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
		TAFSCatalog		myCat = new TAFSCatalog();
		long			tempCounter = 0;
		TAFSCommHandler	aCommHandler = new TAFSCommHandler(4321);
		TAFSCommHandler	threadCH;

		System.out.println("Entered " + TAFSCacheCoordinator.class.getSimpleName());

		while (true)
		{
			// Listen for message
			System.out.print(TAFSCacheCoordinator.class.getSimpleName() + "(" + tempCounter + "): Waiting for message...");
			// Temp sleep until comm handler is written
			Thread.sleep(1000);
			System.out.println();
			threadCH = aCommHandler.Listen();

			// Spin off thread to handle message
			System.out.println(TAFSCacheCoordinator.class.getSimpleName() + ": Received message, executing thread.");

			/*aMsgHandler = */new TAFSCCThread(threadCH, myCat, "Thread for loop #" + tempCounter);

			tempCounter++;
			if (tempCounter >= 10)
				break;
		}

		System.out.println("Exited " + TAFSCacheCoordinator.class.getSimpleName());
	}
}
