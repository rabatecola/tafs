/**
 * Waits for incoming communication (via TAFSCommHandler) and starts up
 * TAFSCCThread threads to handle each connection
 */
package tafsCacheCoordinator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.logging.Logger;

import tafs.TAFSCatalog;
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
	private final static Logger log = Logger.getLogger(className);

	public TAFSCacheCoordinator() throws FileNotFoundException, IOException, InterruptedException
	{
		this.RunCC();
	}

	// main declaration to enable directly calling this class
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException
	{
		TAFSGlobalConfig.LoadConfigFromFile();
		new TAFSCacheCoordinator();
	}

	public void RunCC() throws FileNotFoundException, IOException, InterruptedException
	{
		TAFSCatalog		myCat = new TAFSCatalog();
		long			tempCounter = 0;
		TAFSCommHandler	aCommHandler = new TAFSCommHandler(TAFSGlobalConfig.getInteger(TAFSOptions.ccListenPort));
		TAFSCommHandler	threadCH;

		String path = TAFSCacheCoordinator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = URLDecoder.decode(path, "UTF-8");
		log.info("Starting " + decodedPath);
		CodeSource codeSource = TAFSCacheCoordinator.class.getProtectionDomain().getCodeSource();
		File jarFile;
//		try
//		{
			jarFile = new File(codeSource.getLocation().getPath());
			String jarDir = jarFile.getPath();
			log.info("jarDir = " + URLDecoder.decode(jarDir, "UTF-8"));
			log.info("jarFile = " + URLDecoder.decode(jarFile.getName(), "UTF-8"));
//		}
//		catch (URISyntaxException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		log.info("Entered " + className);

		// Load the catalog from disk.
		String	catFile = TAFSGlobalConfig.getString(TAFSOptions.catalogFile);
		myCat.LoadEntriesFromFile(catFile);
		myCat.DisplayEntries();
		log.info("Loaded from catalog file '" + catFile + "'");

		while (true)
		{
			// Listen for message
			log.info(className + "(" + tempCounter + "): Host address is " + InetAddress.getLocalHost().getHostAddress());
			log.info(className + "(" + tempCounter + "): Waiting for message...");

			threadCH = aCommHandler.Listen();

			// Spin off thread to handle message
			log.info(className + ": Received message, executing thread.");

			new TAFSCCThread(threadCH, myCat, "Thread for loop #" + tempCounter);

			// Pause for a second before continuing
			Thread.sleep(1000);

			tempCounter++;
			if (tempCounter >= 10)
				break;
		}

		log.info("Exited " + className);
	}
}
