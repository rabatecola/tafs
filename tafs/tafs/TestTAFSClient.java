/**
 * 
 */
package tafs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.acl.Acl;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import tafsComm.TAFSCommHandler;
import tafscache.TAFSCache_SMCD;

/**
 * @author Robert Abatecola
 *
 */
public class TestTAFSClient
{
	private final static String	className = TestTAFSClient.class.getSimpleName();
	private final static Logger log = Logger.getLogger(className);

	// Control how many threads are used when reading files.
	private static Integer		threadLimit = 10;	// Default
	private static Semaphore	threadControl = null;
	private static Semaphore	threadCompletion = null;

	private static String		parentDir = "./testOutput/";
	private static String		precisionStr = "%05d";
	private static String		timingFormat = "%1$5d";
	private static String		fieldFormat = "%1$-5s";

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException
	{
		String		theFileName = "test.txt";
		String		newFileName = "";
		Integer		fileCount = 10;		// Default
		Integer		testLoopCount = 1;	// Default
		byte[]		fileData = null;
//		Boolean		useCache = false;
		TAFSClient	aClient = new TAFSClient();

//		testJunk();
		// Tell spy to use the SunLogger
		Properties systemProperties = System.getProperties();
		systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
		System.setProperties(systemProperties);

		Logger.getLogger("net.spy.memcached").setLevel(TAFSGlobalConfig.getLevel(TAFSOptions.memcachedlogLevel));

		// No need to load the config manually.  Creation of a TAFSClient object does this.
//		TAFSGlobalConfig.LoadConfigFromFile();
//		TAFSGlobalConfig.SetLoggingLevel(className);
		TAFSGlobalConfig.SetLoggingLevel(TAFSGlobalConfig.getLevel(TAFSOptions.clientLogLevel));
//		log.setLevel(TAFSGlobalConfig.getLevel(TAFSOptions.clientLogLevel));

		fileCount = TAFSGlobalConfig.getInteger(TAFSOptions.clientFileCount);
		testLoopCount = TAFSGlobalConfig.getInteger(TAFSOptions.clientLoopCount);

		threadLimit = TAFSGlobalConfig.getInteger(TAFSOptions.clientThreadLimit);
		threadControl = new Semaphore(threadLimit, true);
		threadCompletion = new Semaphore(0);

		TAFSCache_SMCD.CreateCacheClient(TAFSGlobalConfig.getString(TAFSOptions.cacheServers));

//		fileData = aClient.getFile(theFileName, true);
		fileData = ReadFile(theFileName);
		WriteFile(theFileName, fileData);

		if (TAFSGlobalConfig.getBoolean(TAFSOptions.clientGenerateTestFiles))
		{
			System.out.println("Generating test files");
			for (int x = 0; x < fileCount; x++)
			{
				newFileName = "temp" + String.format(precisionStr, x) + ".dat";
				log.fine(parentDir + newFileName);
				WriteFile(parentDir + newFileName, fileData);
			}
		}

		if (TAFSGlobalConfig.getBoolean(TAFSOptions.clientRunWriteTests))
		{
			RunWriteTests(fileCount, testLoopCount);
			System.out.println();
		}
		else
			System.out.println("Skipping write tests");

		if (TAFSGlobalConfig.getBoolean(TAFSOptions.clientRunReadTests))
			RunReadTests(fileCount, testLoopCount);
		else
			System.out.println("Skipping read tests");

//		aClient.putFile("test.txt", false);
//		aClient.putFile("test2.txt", false);

		TAFSCache_SMCD.DestroyCacheClient();
	}

	private static void WriteResultTitle(Boolean inIsGet)
	{
		System.out.println("=================================");
		if (inIsGet)
			System.out.println("            Read Tests");
		else
			System.out.println("            Write Tests");
		System.out.println("=================================");
	}
	
	private static void WriteResultHeader()
	{
		System.out.println("threads | cache |  loop | elapsed");
	}

	private static void WriteResultSeparator()
	{
		System.out.println("--------|-------|-------|--------");
	}

	private static void WriteResultLine(Boolean inUseThreads, Boolean inUseCache, long inLoopTime, long inElapsedTime)
	{
		System.out.print("   " + String.format(fieldFormat, inUseThreads ? "on" : "off") + "|  " + String.format(fieldFormat, inUseCache ? "on" : "off") + "| "); 
		System.out.println(String.format(timingFormat, inLoopTime) + " |   " + String.format(timingFormat, inElapsedTime));
	}

	private static void RunReadTests(Integer inFileCount, Integer inLoopCount) throws IOException, InterruptedException
	{
		RunTests(true, inFileCount, inLoopCount);
	}

	private static void RunWriteTests(Integer inFileCount, Integer inLoopCount) throws IOException, InterruptedException
	{
		RunTests(false, inFileCount, inLoopCount);
	}
	
	private static void RunTests(Boolean inIsGet, Integer inFileCount, Integer inLoopCount) throws IOException, InterruptedException
	{
		long		startTime;
		long		endTime;
		long		elapsedTime;

		WriteResultTitle(inIsGet);
		WriteResultHeader();

		for (int x = 0; x < inLoopCount; x++)
		{
			WriteResultSeparator();

			// Multiple threads for puts...
			// Cache off
			startTime = System.currentTimeMillis();
			elapsedTime = RunTest(inIsGet, inFileCount, true, false);
			endTime = System.currentTimeMillis();
			elapsedTime /= 1000000;
			WriteResultLine(true, false, endTime - startTime, elapsedTime);

			// Cache on
			startTime = System.currentTimeMillis();
			elapsedTime = RunTest(inIsGet, inFileCount, true, true);
			endTime = System.currentTimeMillis();
			elapsedTime /= 1000000;
			WriteResultLine(true, true, endTime - startTime, elapsedTime);

			// Send each put in sequence, no threading
			// Cache off
			startTime = System.currentTimeMillis();
			elapsedTime = RunTest(inIsGet, inFileCount, false, false);
			endTime = System.currentTimeMillis();
			elapsedTime /= 1000000;
			WriteResultLine(false, false, endTime - startTime, elapsedTime);

			// Cache on
			startTime = System.currentTimeMillis();
			elapsedTime = RunTest(inIsGet, inFileCount, false, true);
			endTime = System.currentTimeMillis();
			elapsedTime /= 1000000;
			WriteResultLine(false, true, endTime - startTime, elapsedTime);
		}
	}

	private static long RunTest(Boolean inIsGet, Integer inFileCount, Boolean inWithThreads, Boolean inUseCache) throws IOException, InterruptedException
	{
		long	elapsedTime;

		if (inWithThreads)
			elapsedTime = GetOrPutFilesWithThreads(inIsGet, inFileCount, inUseCache);
		else
			elapsedTime = GetOrPutFilesWithoutThreads(inIsGet, inFileCount, inUseCache);

		return elapsedTime;
	}

	private static void testJunk()
	{
		TAFSCommHandler	aCH = new TAFSCommHandler(48611);
		long			socketCount = 0;

		while(true)
		{
			try
			{
				aCH.Open("127.0.0.1");
				socketCount++;
			}
			catch (UnknownHostException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				System.out.println("Socket count: " + socketCount);
				e.printStackTrace();
			}
		}
	}

	private static long GetOrPutFilesWithoutThreads(Boolean inIsGet, Integer inFileCount, Boolean inUseCache) throws IOException
	{
		return GetOrPutFilesWithoutThreads(inIsGet, inFileCount, 0, inUseCache);
	}

	private static long GetOrPutFilesWithoutThreads(Boolean inIsGet, Integer inFileCount, Integer inStartFile, Boolean inUseCache) throws IOException
	{
		String		newFileName = "";
		long		startTime;
		long		endTime;
		long		elapsedTime = 0;
		Integer		endFile = inFileCount + inStartFile - 1;
		TAFSClient	aClient = new TAFSClient();

		for (int x = inStartFile; x <= endFile; x++)
		{
			newFileName = "temp" + String.format(precisionStr, x) + ".dat";
			log.info((inIsGet ? "Reading from" : "Writing to") + " TAFS: " + newFileName);
			startTime = System.nanoTime();
			if (inIsGet)
				aClient.getFile(newFileName, inUseCache);
			else
				aClient.putFile(parentDir + newFileName, newFileName, inUseCache);
			endTime = System.nanoTime();
			elapsedTime += endTime - startTime;
		}

		return elapsedTime;
	}

	private static long GetOrPutFilesWithThreads(Boolean inIsGet, Integer inFileCount, Boolean inUseCache) throws InterruptedException
	{
//		String	newFileName = "";
		long	startTime;
		long	endTime;
		long	elapsedTime = 0;
		Integer	filesPerThread = inFileCount / threadLimit;
		Integer	finalCount = inFileCount % threadLimit;

		for (int x = 0; x < threadLimit; x++)
		{
//			newFileName = "temp" + String.format(precisionStr, x) + ".dat";
			startTime = System.nanoTime();
			threadControl.acquire();
			// Increment running thread semaphore
			threadCompletion.release();
			new TestTAFSClient().new GetOrPutFile(inIsGet, /*parentDir + newFileName, newFileName,*/ filesPerThread, x * filesPerThread, inUseCache);
			endTime = System.nanoTime();
			elapsedTime += endTime - startTime;
		}
//		System.out.println("Threads still running: " + threadCompletion.availablePermits());
		if (finalCount != 0)
		{
			startTime = System.nanoTime();
			threadControl.acquire();
			// Increment running thread semaphore
			threadCompletion.release();
			new TestTAFSClient().new GetOrPutFile(inIsGet, /*parentDir + newFileName, newFileName,*/ finalCount, threadLimit * filesPerThread, inUseCache);
			endTime = System.nanoTime();
			elapsedTime += endTime - startTime;
		}

		// Wait for all threads to finish
		startTime = System.nanoTime();
//		threadCompletion.acquire();
		threadControl.acquire(threadLimit);
		threadControl.release(threadLimit);
		endTime = System.nanoTime();
		elapsedTime += endTime - startTime;

		return elapsedTime;
	}

	private class GetOrPutFile implements Runnable
	{
		private Boolean	useCache;
		private Integer	fileCount;
		private Integer	startFile;
		private Boolean	isGet;

		public GetOrPutFile(Boolean inIsGet, Integer inFileCount, Integer inStartFile, Boolean inUseCache)
		{
			fileCount = inFileCount;
			startFile = inStartFile;
			useCache = inUseCache;
			isGet = inIsGet;

			new Thread(this).start();
		}

//		public void SetGet(Boolean inGet)
//		{
//			isGet = inGet;
//		}

		@Override
		public void run()
		{
			try
			{
				GetOrPutFilesWithoutThreads(isGet, fileCount, startFile, useCache);
			}
			catch (IOException eIO)
			{
				// TODO Auto-generated catch block
				if (eIO.getMessage().equalsIgnoreCase("Connection reset by peer") || eIO.getMessage().equalsIgnoreCase("Broken pipe"))
				{
					log.warning("Could not receive message: " + eIO.getMessage());
				}
				else
				{
					log.severe("Could not receive message: " + eIO.getMessage());
					eIO.printStackTrace();
				}
			}
			finally
			{
				threadControl.release();
				try
				{
					threadCompletion.acquire();
				}
				catch(InterruptedException eIE) { /* Ignore */ }
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
	
	// Read the file from the file system
	public static byte[] ReadFile(String inFileName) throws IOException
	{
		byte[]				fileByteArray = null;
		File				file = null;
		long				length = 0;
		FileInputStream		fis = null;
		BufferedInputStream	bis = null;
		int					count = 0;

		try
		{
			log.fine("Reading '" + inFileName + "'");
			file = new File(inFileName);
			// Get the size of the file
			length = file.length();
			if (length > Integer.MAX_VALUE)
			{
				log.severe("\tFile is too large.");
			}
			else
			{
				fis = new FileInputStream(file);
				bis = new BufferedInputStream(fis);
				fileByteArray = new byte[(int)length];
	
				count = bis.read(fileByteArray);
				if (count == -1 || count == length)
				{
					log.fine("\tFile read to end: " + inFileName);
				}
			}
		}
//		catch(FileNotFoundException e)
//		{
//			System.out.println("\tFile not found, " + fileName + ": " + e.getMessage());
//		}
//		catch(IOException ioE)
//		{
//			System.out.println("\tFile not read, " + fileName + ": " + ioE.getMessage());
//		}
		finally
		{
			try
			{
				if (fis != null)
					fis.close();
			}
			catch(IOException e)
			{
			}
			finally
			{
				if (bis != null)
					bis.close();
			}
		}

		return fileByteArray;
	}
}
