/**
 * Waits for incoming communication (via TAFSCommHandler) and starts up
 * TAFSCCThread threads to handle each connection
 */
package tafsCacheCoordinator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import tafs.TAFSGlobalConfig;
import tafs.TAFSOptions;
import tafsComm.TAFSCommHandler;

/**
 * @author Robert Abatecola
 *
 */
public class TAFSCacheCoordinator
{
	private final static String	className = TAFSCacheCoordinator.class.getSimpleName();
	private static Logger log = Logger.getLogger(className);

	private TAFSCatalog		myCat = new TAFSCatalog();

	private static Integer	threadLimit = 10;	// Default
	public static Semaphore	activeThreads = null;
	public static Semaphore	maxThreads = null;

	public TAFSCacheCoordinator() throws FileNotFoundException, IOException, InterruptedException
	{
		this.RunCC();
	}

	// main declaration to enable directly calling this class
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException
	{
		TAFSGlobalConfig.LoadConfigFromFile();
//		TAFSGlobalConfig.SetLoggingLevel(className);
		TAFSGlobalConfig.SetLoggingLevel(TAFSGlobalConfig.getLevel(TAFSOptions.ccLogLevel));
//		log.setLevel(TAFSGlobalConfig.getLevel(TAFSOptions.ccLogLevel));
		new TAFSCacheCoordinator();
	}

	public void RunCC() throws FileNotFoundException, IOException, InterruptedException
	{
		long			tempCounter = 0;
		long			maxThreadCount = 0;
		TAFSCommHandler	aCommHandler = new TAFSCommHandler(TAFSGlobalConfig.getString(TAFSOptions.ccBindAddr), TAFSGlobalConfig.getInteger(TAFSOptions.ccListenPort));
		TAFSCommHandler	threadCH;

		log.info("Entered " + className);

		threadLimit = TAFSGlobalConfig.getInteger(TAFSOptions.ccThreadLimit);
		maxThreads = new Semaphore(threadLimit);
		activeThreads = new Semaphore(0);

		// Load the catalog from disk.
		String	catFile = TAFSGlobalConfig.getString(TAFSOptions.catalogFile);
		synchronized(myCat)
		{
			myCat.LoadEntriesFromFile(catFile);
			if (log.isLoggable(Level.FINEST))
				myCat.DisplayEntries();
		}
		log.info("Loaded from catalog file '" + catFile + "'");

		// Start up thread to periodically write the catalog to a file.
		// This will be bad for large catalogs.
		log.info("Starting CatalogWriterThread");
		new CatalogWriterThread();

		while (true)
		{
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
			new TAFSCCThread(threadCH, myCat, "Thread #" + tempCounter);

			// Pause for a second before continuing
//			Thread.sleep(1000);

			tempCounter++;
//			if (tempCounter >= 10)
//				break;
		}

//		log.info("Exited " + className);
	}

	private class CatalogWriterThread implements Runnable
	{
		public CatalogWriterThread()
		{
			// Start me up
			new Thread(this).start();
		}

		public void run()
		{
			String	catFile;
			Integer	sleepTime;

			while (true)
			{
				try
				{
					// Refresh sleepTime every time in case prefs have changed while running.
					sleepTime = TAFSGlobalConfig.getInteger(TAFSOptions.catSaveInterval);
					log.info("Thread.sleep(" + sleepTime + ")");
					Thread.sleep(sleepTime);
					// Refresh catFile every time in case prefs have changed while running.
					catFile = TAFSGlobalConfig.getString(TAFSOptions.catalogFile);

					synchronized (myCat)
					{
						if (!myCat.isDirty())
							continue;
						log.info("Writing catalog to file...");
						myCat.WriteEntriesToFile(catFile);
					}
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
//					e.printStackTrace();
					log.warning("Thread interrupted: " + e.getMessage());
				}
			}
		}
	}
}
