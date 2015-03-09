/**
 * 
 */
package tafs;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author robert
 *
 * TODO Convert to use java's standard preferences interface
 * TODO Add Apache Commons CLI option parsing
 */
public class TAFSGlobalConfig
{
	private final static String	className = TAFSGlobalConfig.class.getSimpleName();
	private final static Logger log = Logger.getLogger(className);

	private static Map<String,String>	optionList = new HashMap<String,String>();

	private static final String		defaultConfigFile = "tafs.conf";
//	private static final Integer	listenPort = 48611;	// 48611 is the 5000th prime number.  Oh joy.
//	private static final Level		logLevel = Level.ALL;
//	private static final Level		memcachedlogLevel = Level.FINEST;
//	private static final String		cacheServers = "127.0.0.1:11211,127.0.0.1:11212";
//	private static final String		catalogFile = "tafsCatalog.dat";
//	private static final String		ccIP = "192.168.97.43";
//	private static final String[]	chIP = { "192.168.97.47", "192.168.97.48" };

	public static void SetLoggingLevel(String inLoggerName)
	{
		Logger theLogger = Logger.getLogger(inLoggerName);

		theLogger.setLevel(getLevel(TAFSOptions.logLevel));
		theLogger.getHandlers();
	    for (Handler handler : theLogger.getHandlers())
	    {
            // Set level in the console handler
	        if (handler instanceof ConsoleHandler)
	        {
	        	((ConsoleHandler)handler).setLevel(getLevel(TAFSOptions.logLevel));
	            break;
	        }
	    }
	}

	public static String getString(TAFSOptions inOption)
	{
		return optionList.get(inOption.getOptStr());
	}

	public static ArrayList<String> getArrayListString(TAFSOptions inOption)
	{
		String				optString = optionList.get(inOption.getOptStr());
		ArrayList<String>	theAL = new ArrayList<String>();

		theAL.addAll(Arrays.asList(optString.split("\\s*,\\s*")));

		return theAL;
	}

	public static Integer getInteger(TAFSOptions inOption)
	{
		return Integer.parseInt(getString(inOption));
	}

	public static Level getLevel(TAFSOptions inOption)
	{
		return Level.parse(getString(inOption));
	}

	public static void LoadConfigFromFile() throws FileNotFoundException, IOException
	{
		LoadConfigFromFile(defaultConfigFile);
	}

	public static void LoadConfigFromFile(String inConfigFileName) throws FileNotFoundException, IOException
	{
		File			file = null;
		FileReader		myFR = null;
		BufferedReader	myBR = null;
		String			oneLine = "";
		String[]		oneLineParts;
		String			optionName = "";
		String			optionValue = "";

		file = new File(inConfigFileName);
		myFR = new FileReader(file);
		myBR = new BufferedReader(myFR);

		try
		{
			do
			{
				oneLine = myBR.readLine();
				if (oneLine == null)
					break;

				// Treat lines beginning with # or // as comments.
				oneLine = oneLine.trim();
				if (oneLine.startsWith("#") || oneLine.startsWith("//"))
					continue;

				// Ignore lines that do not have two strings separated by white space.
				oneLineParts = oneLine.split("\\s+");
				if (oneLineParts.length != 2)
					continue;

				optionName = oneLineParts[0];
				optionValue = oneLineParts[1];
				if (TAFSOptions.contains(optionName))
					optionList.put(optionName, optionValue);
				else
					log.warning("Invalid option specified: " + optionName);
			} while (true);
		}
		catch(EOFException eEOF)
		{
			// This is normal and means that the file was read to its end.
		}
		catch(IOException eIO)
		{
			log.warning("IOException: " + eIO.getMessage());
		}
		finally
		{
			myBR.close();
		}

		SetLoggingLevel("");
		if (log.isLoggable(Level.FINE))
		{
			System.out.println("Options read from file:");
			DisplayEntries();
		}
	}

	public static void DisplayEntries()
	{
		for (String key : optionList.keySet())
		{
			// Get the String value that goes with the key
			String value = optionList.get(key);

			// Print the key and value
//			System.out.println("\t" + key + ": " + value);
			System.out.printf("    %-20.20s  %-40.40s%n", key, value);
		}
	}
}
