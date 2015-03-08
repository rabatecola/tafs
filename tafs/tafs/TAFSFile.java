package tafs;
/**
 * @author Robert Abatecola
 *
 */

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;

import tafscache.TAFSCache_SMCD;

public class TAFSFile
{
	private final static String	className = TAFSFile.class.getSimpleName();
	private final static Logger log = Logger.getLogger(className);

	private byte[]					writeByteArray = null;
	private String					fileName = "";
	private BufferedOutputStream	bos = null;
	private Boolean					cacheReads = true;
	private Boolean					cacheWrites = false;
	private TAFSCache_SMCD	myCache = null;

	public TAFSFile(String inFileName, String inCacheServers)
	{
		fileName = inFileName;
		myCache = new TAFSCache_SMCD(inCacheServers);
	}

	public Boolean GetCacheReads()
	{
		return this.cacheReads;
	}

	public void SetCacheReads(Boolean inCR)
	{
		this.cacheReads = inCR;
	}

	public Boolean GetCacheWrites()
	{
		return this.cacheWrites;
	}

	public void SetCacheWrites(Boolean inCW)
	{
		this.cacheWrites = inCW;
	}

	// Get a file from the cache or file system.
	public byte[] GetFile() throws IOException, IllegalStateException
	{
		byte[]	fileByteArray = null;

		if (cacheReads)
		{
			myCache.ConnectCache();

			fileByteArray = myCache.GetFileFromCache(fileName);
			if (fileByteArray != null)
				log.info("Cache HIT: file found in cache.");
			else
				log.info("Cache MISS: file not found in cache.  Reading it into memory.");
		}

		if (fileByteArray == null)
		{
			fileByteArray = ReadFile();
			if (cacheReads && (fileByteArray != null))
				myCache.PutFileInCache(fileName, fileByteArray);
		}

		if (cacheReads)
			myCache.DisconnectCache();

		return fileByteArray;
	}

	// Read the file from the file system
	public byte[] ReadFile() throws IOException
	{
		byte[]				fileByteArray = null;
		File				file = null;
		long				length = 0;
		FileInputStream		fis = null;
		BufferedInputStream	bis = null;
		int					count = 0;

		try
		{
			file = new File(fileName);
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
					log.info("\tFile read to end: " + fileName);
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
				if (bis != null)
					bis.close();
			}
			catch(IOException e)
			{
			}
		}

		return fileByteArray;
	}

	// Write inFileData to the file system
	public void WriteFile(byte[] inFileData) throws IOException
	{
		OpenForWriting();
		Write(inFileData, inFileData.length);
		Flush();
		Close();
	}

	public void OpenForWriting() throws FileNotFoundException, IOException
	{
		if (cacheWrites)
		{
			myCache.ConnectCache();
			writeByteArray = new byte[0];
		}

		bos = new BufferedOutputStream(new FileOutputStream(fileName));
	}

	public void Flush() throws IOException
	{
		if (cacheWrites)
		{
			WriteNoCache(writeByteArray, writeByteArray.length);
			myCache.PutFileInCache(fileName, writeByteArray);
		}

		if (bos != null)
			bos.flush();
	}

	public void Close() throws IOException
	{
		if (cacheWrites)
			myCache.DisconnectCache();

		if (bos != null)
			bos.close();
	}

	// TO DO: Try appending to a buffer until complete, then spin off a thread to store the
	// buffer in cache and write it to disk.
	public void Write(byte[] inBytes, int inCount) throws IOException
	{
		if (cacheWrites)
		{
			int		newLength = writeByteArray.length + inCount;
			byte[]	newArray = new byte[newLength];

			System.arraycopy(writeByteArray, 0, newArray, 0, writeByteArray.length);
			System.arraycopy(inBytes, 0, newArray, writeByteArray.length, inCount);
			writeByteArray = newArray;
		}
		else
			WriteNoCache(inBytes, inCount);
	}

	public void WriteNoCache(byte[] inBytes, int inCount) throws IOException
	{
		bos.write(inBytes, 0, inCount);
	}
}
