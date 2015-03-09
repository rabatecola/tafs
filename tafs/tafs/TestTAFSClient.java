/**
 * 
 */
package tafs;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author robert
 *
 */
public class TestTAFSClient
{

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		TAFSClient	aClient = new TAFSClient();

		aClient.getFile("test.txt", true);
	}
}
