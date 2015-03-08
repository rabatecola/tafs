package tafs;
/**
 * @author Robert Abatecola
 *
 */

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TAFSCatalog
{
	private Map<String,String>	fileCatalog = new HashMap<String,String>();

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
		}
	}

	public void DeleteFileEntry(String inFileName)
	{
		synchronized(fileCatalog)
		{
			fileCatalog.remove(inFileName);
		}
	}

	// Load the catalog from a tab-delimited file containing one fileName and serverID on each line.
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
			do
			{
				oneLine = myBR.readLine();
				if (oneLine == null)
					break;

				// Treat lines beginning with # or // as comments.
				oneLine = oneLine.trim();
				if (oneLine.startsWith("#") || oneLine.startsWith("//"))
					continue;

				// Ignore lines that do not have two tab-separated strings.
				oneLineParts = oneLine.split("\t");
				if (oneLineParts.length != 2)
					continue;

				fileName = oneLineParts[0];
				serverID = oneLineParts[1];
				SetFileEntry(fileName, serverID);
			} while (true);
		}
		catch(EOFException eEOF)
		{
			// This is normal and means that the file was read to its end.
		}
		catch(IOException eIO)
		{
			System.out.println("IOException: " + eIO.getMessage());
		}
		finally
		{
			myBR.close();
		}
	}

	public void DisplayEntries()
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
