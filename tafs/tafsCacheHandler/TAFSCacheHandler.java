/**
 * Waits for incoming communication (via TAFSCommHandler) and starts up
 * TAFSCHThread threads to handle each connection
 */
package tafsCacheHandler;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import tafs.TAFSGlobalConfig;
import tafs.TAFSOptions;
import tafsComm.TAFSCommHandler;
import tafscache.TAFSCache_SMCD;

/**
 * @author robert
 *
 */
public class TAFSCacheHandler
{
	private final static String	className = TAFSCacheHandler.class.getSimpleName();
	private static Logger log = Logger.getLogger(className);

	private static Integer	threadLimit = 10;	// Default
	public static Semaphore	activeThreads = null;
	public static Semaphore	maxThreads = null;

	public TAFSCacheHandler() throws InterruptedException, IOException
	{
		this.RunCH();
	}

	// main declaration to enable directly calling this class
	public static void main(String[] args) throws InterruptedException, IOException
	{
		TAFSGlobalConfig.LoadConfigFromFile();
//		TAFSGlobalConfig.SetLoggingLevel(className);
		TAFSGlobalConfig.SetLoggingLevel(TAFSGlobalConfig.getLevel(TAFSOptions.chLogLevel));
//		log.setLevel(TAFSGlobalConfig.getLevel(TAFSOptions.chLogLevel));
		new TAFSCacheHandler();
	}

	public void RunCH() throws InterruptedException, IOException
	{
		long			tempCounter = 0;
		long			maxThreadCount = 0;
		TAFSCommHandler	aCommHandler = new TAFSCommHandler(TAFSGlobalConfig.getString(TAFSOptions.chBindAddr), TAFSGlobalConfig.getInteger(TAFSOptions.chListenPort));
		TAFSCommHandler	threadCH;

		// Tell spy to use the SunLogger
		Properties systemProperties = System.getProperties();
		systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
		System.setProperties(systemProperties);

		Logger.getLogger("net.spy.memcached").setLevel(TAFSGlobalConfig.getLevel(TAFSOptions.memcachedlogLevel));

		TAFSCache_SMCD.CreateCacheClient(TAFSGlobalConfig.getString(TAFSOptions.cacheServers));

		threadLimit = TAFSGlobalConfig.getInteger(TAFSOptions.chThreadLimit);
		maxThreads = new Semaphore(threadLimit);
		activeThreads = new Semaphore(0);

		log.info("Entered " + className + ".  Thread limit = " + threadLimit);

		try
		{
			while (true)
			{
				// Listen for message
				// Listen for message
				if (log.isLoggable(Level.INFO))
				{
					String	logMsg = className + "(" + tempCounter + "): ";
	//						logMsg += "Host is " + InetAddress.getLocalHost().getHostAddress();
							logMsg += "Host is " + aCommHandler.GetLocalIP();
							logMsg += ":" + aCommHandler.GetPort() + ".  Waiting for message...\n\tThreads active, max = " + activeThreads.availablePermits() + ", " + maxThreadCount;

					log.info(logMsg);
				}
				threadCH = aCommHandler.Listen();

				// Spin off thread to handle message
				log.info(className + ": Received message, executing thread.");

				// Increment activeThread count
				activeThreads.release();
	//			maxThreads.acquire();
				if (activeThreads.availablePermits() > maxThreadCount)
					maxThreadCount = activeThreads.availablePermits();
				new TAFSCHThread(threadCH, "Thread #" + tempCounter);

				// Pause for a second before continuing
//				Thread.sleep(1000);

				tempCounter++;
//				if (tempCounter >= 10)
//					break;
			}
		}
		finally
		{
			TAFSCache_SMCD.DestroyCacheClient();
		}

//		log.info("Exited " + className);
	}
}
