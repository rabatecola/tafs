/**
 * 
 */
package tafs;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

/**
 * @author Robert Abatecola
 *
 */
public class TestTAFSClient
{
	private final static String	className = TestTAFSClient.class.getSimpleName();
	private final static Logger log = Logger.getLogger(className);

	// Control how many threads are used when reading files.
	private final static Integer	maxThreads = 5;
	private final static Semaphore	threadControl = new Semaphore(maxThreads);

	private static TAFSClient	aClient = null;
	private static String		parentDir = "./testOutput/";
	private static String		precisionStr = "%04d";


	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException
	{
//		String		theFileName = "test.txt";
//		String		newFileName = "";
		Integer		fileCount = 1000;
//		byte[]		fileData = null;
		long		startTime;
		long		endTime;
		long		elapsedTime;
//		Boolean		useCache = false;
		String		timingFormat = "%1$5d";

		aClient = new TAFSClient();

//		fileData = aClient.getFile(theFileName, true);
//		WriteFile(theFileName, fileData);

//		for (int x = 0; x < fileCount; x++)
//		{
//			newFileName = "temp" + String.format(precisionStr, x) + ".dat";
//			System.out.println(parentDir + newFileName);
//			WriteFile(parentDir + newFileName, fileData);
//		}

		System.out.println("threads | cache |  loop | elapsed");
		System.out.println("--------|-------|-------|--------");
//		System.out.println("  on    |  off  | 99999 |   99999");
//		System.out.println("  on    |  off  | " + String.format(timingFormat, endTime - startTime) + " |   " + String.format(timingFormat, elapsedTime / 1000000));

		// Separate thread for each put...
		// Cache off
		startTime = System.currentTimeMillis();
		elapsedTime = PutFilesWithThreads(fileCount, false);
		endTime = System.currentTimeMillis();
		elapsedTime /= 1000000;
		System.out.println("  on    |  off  | " + String.format(timingFormat, endTime - startTime) + " |   " + String.format(timingFormat, elapsedTime));
//		System.out.println("PutFile threads on, cache off (loop, elapsed):  " + String.format(timingFormat, endTime - startTime) + ", " + String.format(timingFormat, elapsedTime) + " milliseconds");
//		System.out.println("PutFile threads on, cache off loop time:     " + (endTime - startTime) + " milliseconds");
//		System.out.println("PutFile threads on, cache off elapsed time:  " + elapsedTime / 1000000 + " milliseconds");

		// Cache on
		startTime = System.currentTimeMillis();
		elapsedTime = PutFilesWithThreads(fileCount, true);
		endTime = System.currentTimeMillis();
		elapsedTime /= 1000000;
		System.out.println("  on    |  on   | " + String.format(timingFormat, endTime - startTime) + " |   " + String.format(timingFormat, elapsedTime));
//		System.out.println("PutFile threads on, cache on (loop, elapsed):   " + String.format(timingFormat, endTime - startTime) + ", " + String.format(timingFormat, elapsedTime) + " milliseconds");
//		System.out.println("PutFile threads on, cache on loop time:      " + (endTime - startTime) + " milliseconds");
//		System.out.println("PutFile threads on, cache on elapsed time:   " + elapsedTime / 1000000 + " milliseconds");

		// Send each put in sequence, no threading
		// Cache off
		startTime = System.currentTimeMillis();
		elapsedTime = PutFilesWithoutThreads(fileCount, false);
		endTime = System.currentTimeMillis();
		elapsedTime /= 1000000;
		System.out.println("  off   |  off  | " + String.format(timingFormat, endTime - startTime) + " |   " + String.format(timingFormat, elapsedTime));
//		System.out.println("PutFile threads off, cache off (loop, elapsed): " + String.format(timingFormat, endTime - startTime) + ", " + String.format(timingFormat, elapsedTime) + " milliseconds");
//		System.out.println("PutFile threads off, cache off loop time:    " + (endTime - startTime) + " milliseconds");
//		System.out.println("PutFile threads off, cache off elapsed time: " + elapsedTime / 1000000 + " milliseconds");

		// Cache on
		startTime = System.currentTimeMillis();
		elapsedTime = PutFilesWithoutThreads(fileCount, true);
		endTime = System.currentTimeMillis();
		elapsedTime /= 1000000;
		System.out.println("  off   |  on   | " + String.format(timingFormat, endTime - startTime) + " |   " + String.format(timingFormat, elapsedTime));
//		System.out.println("PutFile threads off, cache on (loop, elapsed):  " + String.format(timingFormat, endTime - startTime) + ", " + String.format(timingFormat, elapsedTime) + " milliseconds");
//		System.out.println("PutFile threads off, cache on loop time:     " + (endTime - startTime) + " milliseconds");
//		System.out.println("PutFile threads off, cache on elapsed time:  " + elapsedTime / 1000000 + " milliseconds");

		//		aClient.putFile("test.txt", false);
//		aClient.putFile("test2.txt", false);
	}

	private static long PutFilesWithoutThreads(Integer inFileCount, Boolean inUseCache) throws IOException
	{
		String	newFileName = "";
		long	startTime;
		long	endTime;
		long	elapsedTime = 0;

		for (int x = 0; x < 1000; x++)
		{
			newFileName = "temp" + String.format(precisionStr, x) + ".dat";
			log.info("Writing to TAFS: " + newFileName);
			startTime = System.nanoTime();
			aClient.putFile(parentDir + newFileName, newFileName, inUseCache);
			endTime = System.nanoTime();
			elapsedTime += endTime - startTime;
		}

		return elapsedTime;
	}

	private static long PutFilesWithThreads(Integer inFileCount, Boolean inUseCache) throws InterruptedException
	{
		String	newFileName = "";
		long	startTime;
		long	endTime;
		long	elapsedTime = 0;

		for (int x = 0; x < inFileCount; x++)
		{
			newFileName = "temp" + String.format(precisionStr, x) + ".dat";
			startTime = System.nanoTime();
			threadControl.acquire();
			new TestTAFSClient().new PutFile(parentDir + newFileName, newFileName, inUseCache);
			endTime = System.nanoTime();
			elapsedTime += endTime - startTime;
		}

		return elapsedTime;
	}

	private class PutFile implements Runnable
	{
		private String		dstFile = "";
		private String		srcFile = "";
		private Boolean		useCache;
		private TAFSClient	aClient;

		public PutFile(String inSrcFile, String inDstFile, Boolean inUseCache)
		{
			dstFile = inDstFile;
			srcFile = inSrcFile;
			useCache = inUseCache;

			new Thread(this).start();
		}

		@Override
		public void run()
		{
			try
			{
				log.info("TAFS: Put " + srcFile + " to " + dstFile);
				aClient = new TAFSClient();
				aClient.putFile(srcFile, dstFile, useCache);
			}
			catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				threadControl.release();
			}
		}
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
