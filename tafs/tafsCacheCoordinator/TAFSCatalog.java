package tafsCacheCoordinator;
/**
 * @author Robert Abatecola
 *
 */

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class TAFSCatalog
{
	private final static String	className = TAFSCatalog.class.getSimpleName();
	private final static Logger log = Logger.getLogger(className);

	private Map<String,String>	fileCatalog = new HashMap<String,String>();
	private Boolean				catDirty = false;

//	public static void main(String[] args) throws IOException
//	{
//		TAFSCatalog	myCC = new TAFSCatalog();
//
//		myCC.LoadEntriesFromFile("tafsCatalog.dat");
//
//		for (String key : myCC.fileCatalog.keySet())
//		{
//			// Get the String value that goes with the key
//			String value = myCC.fileCatalog.get(key);
//
//			// Print the key and value
//			System.out.println(key + "\t" + value);
//		}
//
//		myCC.SetFileEntry("junk.xlsx", "192.168.10.12");
//
//		System.out.println("Entry for /test.txt is " + myCC.GetFileEntryServerID("/test.txt"));
//		System.out.println("Entry for junk.xlsx is " + myCC.GetFileEntryServerID("junk.xlsx"));
//	}

	public Boolean isDirty()
	{
		synchronized (catDirty)
		{
			return catDirty;
		}
	}

	public String GetFileEntryServerID(String inFileName)
	{
		synchronized(fileCatalog)
		{
			return fileCatalog.get(inFileName);
		}
	}

	public void SetFileEntry(String inFileName, String inServerID)
	{
		synchronized(fileCatalog)
		{
	    	fileCatalog.put(inFileName, inServerID);
			synchronized (catDirty)
			{
				catDirty = true;
			}
		}
	}

	public void DeleteFileEntry(String inFileName)
	{
		synchronized(fileCatalog)
		{
			fileCatalog.remove(inFileName);
			synchronized (catDirty)
			{
				catDirty = true;
			}
		}
	}

	// Load the catalog from a file containing one fileName and serverID on each line.
	// The file name and server ID should be separated by white space.
	public void LoadEntriesFromFile(String inCatFileName) throws FileNotFoundException, IOException
	{
		File			file = null;
		FileReader		myFR = null;
		BufferedReader	myBR = null;
		String			oneLine = "";
		String[]		oneLineParts;
		String			fileName = "";
		String			serverID = "";

		file = new File(inCatFileName);
		myFR = new FileReader(file);
		myBR = new BufferedReader(myFR);

		try
		{
			synchronized(fileCatalog)
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

					fileName = oneLineParts[0];
					serverID = oneLineParts[1];
					SetFileEntry(fileName, serverID);

				} while (true);
				synchronized (catDirty)
				{
					catDirty = false;
				}
			}
		}
		catch(EOFException eEOF)
		{
			// This is normal and means that the file was read to its end.
		}
		catch(IOException eIO)
		{
			log.severe("IOException: " + eIO.getMessage());
		}
		finally
		{
			myBR.close();
		}
	}

	public void WriteEntriesToFile(String inFileName)
	{
		PrintWriter	aPW = null;

		try
		{
			synchronized (fileCatalog)
			{
				aPW = new PrintWriter(new FileOutputStream(inFileName));
				for (String key : fileCatalog.keySet())
				{
					// Get the String value that goes with the key
					String value = fileCatalog.get(key);

					// Print the key and value
					aPW.println(key + "\t" + value);
				}
				synchronized (catDirty)
				{
					catDirty = false;
				}
			}
		}
		catch (FileNotFoundException eFNF)
		{
			log.severe("Exception while trying to create '" + inFileName + "': " + eFNF.getMessage());
		}
		finally
		{
			if (aPW != null)
				aPW.close();
		}
	}

	public void DisplayEntries()
	{
		synchronized (fileCatalog)
		{
			for (String key : fileCatalog.keySet())
			{
				// Get the String value that goes with the key
				String value = fileCatalog.get(key);

				// Print the key and value
				System.out.println(key + "\t" + value);
			}
		}
	}
}
