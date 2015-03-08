/**
 * Waits for incoming communication (via TAFSCommHandler) and starts up
 * TAFSCHThread threads to handle each connection
 */
package tafsCacheHandler;

import java.util.logging.Logger;

import tafs.TAFSGlobalConfig;
import tafsComm.TAFSCommHandler;

/**
 * @author robert
 *
 */
public class TAFSCacheHandler
{
	private final static String	className = TAFSCacheHandler.class.getSimpleName();
	private final static Logger log = Logger.getLogger(className);

	//	public TAFSCacheHandler()
//	{
//	}

	public static void main(String[] args) throws InterruptedException
	{
		long			tempCounter = 0;
		TAFSCommHandler	aCommHandler = new TAFSCommHandler(TAFSGlobalConfig.listenPort);
		TAFSCommHandler	threadCH;

		log.info("Entered " + className);

		while (true)
		{
			// Listen for message
			log.info(className + "(" + tempCounter + "): Waiting for message...");
			// Temp sleep until comm handler is written
			Thread.sleep(1000);
			threadCH = aCommHandler.Listen();

			// Spin off thread to handle message
			log.info(className + ": Received message, executing thread.");

			new TAFSCHThread(threadCH, "Thread for loop #" + tempCounter);

			tempCounter++;
			if (tempCounter >= 10)
				break;
		}

		log.info("Exited " + className);
	}
}
