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
//	private String					cacheServers = "";
//	private TAFSCache_SMCD			myCache = null;
	private String					filePath = "";
//	private Boolean					ownCache = true;

	public TAFSFile(String inFileName/*, String inCacheServers*/)
	{
		this(inFileName, TAFSGlobalConfig.getString(TAFSOptions.chDataDir)/*, inCacheServers*/);
	}

	public TAFSFile(String inFileName, String inFilePath/*, String inCacheServers*/)
	{
		fileName = inFileName;
		filePath = inFilePath + fileName;
//		cacheServers = inCacheServers;
//		EnableCache();
	}

	public Boolean GetCacheReads()
	{
		return this.cacheReads;
	}

	public void SetCacheReads(Boolean inCR)
	{
		this.cacheReads = inCR;
//		EnableCache();
	}

	public Boolean GetCacheWrites()
	{
		return this.cacheWrites;
	}

	public void SetCacheWrites(Boolean inCW)
	{
		this.cacheWrites = inCW;
//		EnableCache();
	}

//	public void SetExternalCache(TAFSCache_SMCD inCache)
//	{
//		if (inCache != null)
//		{
//			myCache = inCache;
//			ownCache = false;
//		}
//		else
//			ownCache = true;
//	}

//	private void EnableCache() throws IOException
//	{
//		if (TAFSCache_SMCD != null)
//			return;
//
//		if (cacheReads || cacheWrites)
//			TAFSCache_SMCD = new TAFSCache_SMCD(cacheServers);
//	}
//
	// Get a file from the cache or file system.
	public byte[] GetFile() throws IOException, IllegalStateException
	{
		byte[]	fileByteArray = null;

		if (cacheReads)
		{
//			if (ownCache)
				TAFSCache_SMCD.ConnectCache();

			fileByteArray = TAFSCache_SMCD.GetFileFromCache(fileName);
			if (fileByteArray != null)
				log.fine("Cache HIT: file found in cache.");
			else
				log.fine("Cache MISS: file not found in cache.  Reading it into memory.");
		}

		if (fileByteArray == null)
		{
			fileByteArray = ReadFile();
			if (cacheReads && (fileByteArray != null))
				TAFSCache_SMCD.PutFileInCache(fileName, fileByteArray);
		}

		if (cacheReads /*&& ownCache*/)
			TAFSCache_SMCD.DisconnectCache();

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
			log.fine("Reading '" + filePath + "'");
			file = new File(filePath);
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
					log.fine("\tFile read to end: " + filePath);
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

	// Write inFileData to the file system
	public void WriteFile(byte[] inFileData) throws IOException
	{
		try
		{
			OpenForWriting();
			Write(inFileData, inFileData.length);
			Flush();
		}
		catch(IOException eIO)
		{
			throw eIO;
		}
		finally
		{
			Close();
		}
	}

	public void OpenForWriting() throws FileNotFoundException, IOException
	{
		if (cacheWrites)
		{
//			if (ownCache)
//				TAFSCache_SMCD.ConnectCache();
			writeByteArray = new byte[0];
		}

		bos = new BufferedOutputStream(new FileOutputStream(filePath));
	}

	public void Flush() throws IOException
	{
		if (cacheWrites)
		{
			WriteNoCache(writeByteArray, writeByteArray.length);
			TAFSCache_SMCD.PutFileInCache(fileName, writeByteArray);
		}

		if (bos != null)
			bos.flush();
	}

	public void Close() throws IOException
	{
//		if (cacheWrites && ownCache)
//			TAFSCache_SMCD.DisconnectCache();

		if (bos != null)
			bos.close();
	}

	// TODO: Try appending to a buffer until complete, then spin off a thread to store the
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
