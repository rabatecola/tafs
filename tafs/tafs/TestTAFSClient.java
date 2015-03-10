/**
 * 
 */
package tafs;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Robert Abatecola
 *
 */
public class TestTAFSClient
{
	private final static String	className = TestTAFSClient.class.getSimpleName();
	private final static Logger log = Logger.getLogger(className);

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		TAFSClient	aClient = new TAFSClient();
//		String		theFileName = "test.txt";
//		byte[]		fileData = null;

//		fileData = aClient.getFile(theFileName, true);
//		WriteFile(theFileName, fileData);

		aClient.putFile("test.txt", false);
		aClient.putFile("test2.txt", false);
	}

	public static void WriteFile(String inFileName, byte[] inFileData)
	{
		BufferedOutputStream	bos = null;

		try
		{
			bos = new BufferedOutputStream(new FileOutputStream(inFileName));
			bos.write(inFileData, 0, inFileData.length);
		}
		catch (FileNotFoundException eFNF)
		{
			log.severe("Exception while trying to create '" + inFileName + "': " + eFNF.getMessage());
		}
		catch (IOException eIO)
		{
			log.severe("Exception while trying to write '" + inFileName + "': " + eIO.getMessage());
		}
		finally
		{
			try
			{
				if (bos != null)
					bos.close();
			}
			catch (IOException e)
			{
				log.warning("Could not close file.");
			}
		}
	}
}
