/**
 * Waits for incoming communication (via TAFSCommHandler) and starts up
 * TAFSCHThread threads to handle each connection
 */
package tafsCacheHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Logger;

import tafs.TAFSGlobalConfig;
import tafs.TAFSOptions;
import tafsComm.TAFSCommHandler;

/**
 * @author robert
 *
 */
public class TAFSCacheHandler
{
	private final static String	className = TAFSCacheHandler.class.getSimpleName();
	private final static Logger log = Logger.getLogger(className);

	public TAFSCacheHandler() throws InterruptedException, IOException
	{
		this.RunCH();
	}

	// main declaration to enable directly calling this class
	public static void main(String[] args) throws InterruptedException, IOException
	{
		TAFSGlobalConfig.LoadConfigFromFile();
		new TAFSCacheHandler();
	}

	public void RunCH() throws InterruptedException, IOException
	{
		long			tempCounter = 0;
		TAFSCommHandler	aCommHandler = new TAFSCommHandler(TAFSGlobalConfig.getInteger(TAFSOptions.chListenPort));
		TAFSCommHandler	threadCH;

		log.info("Entered " + className);

		while (true)
		{
			// Listen for message
			log.info(className + "(" + tempCounter + "): Host address is " + InetAddress.getLocalHost().getHostAddress());
			log.info(className + "(" + tempCounter + "): Waiting for message...");
			threadCH = aCommHandler.Listen();

			// Spin off thread to handle message
			log.info(className + ": Received message, executing thread.");

			new TAFSCHThread(threadCH, "Thread for loop #" + tempCounter);

			// Pause for a second before continuing
			Thread.sleep(1000);

			tempCounter++;
			if (tempCounter >= 10)
				break;
		}

		log.info("Exited " + className);
	}
}
