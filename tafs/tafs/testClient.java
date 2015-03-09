package tafs;
/**
 * @author Robert Abatecola
 *
 */

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.BufferedOutputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class testClient
{

/**
 * @param args the command line arguments
 */
public static void main(String[] args) throws FileNotFoundException, IOException
{
	byte[]		fileByteArray = null;
	String		fileName = "spymemcached-2.10.3-javadoc.jar";
	Boolean		haveFileData = false;
	TAFSFile	myTAFSFile = null;

	TAFSGlobalConfig.LoadConfigFromFile();

	if (args.length > 0)
	{
		fileName = args[0];
	}
	System.out.println("Transferring: " + fileName);

	// Tell spy to use the SunLogger
	Properties systemProperties = System.getProperties();
	systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
	System.setProperties(systemProperties);

	Logger.getLogger("net.spy.memcached").setLevel(TAFSGlobalConfig.getLevel(TAFSOptions.memcachedlogLevel));
//	Logger.getLogger("net.spy.memcached").setLevel(Level.WARNING);

	myTAFSFile = new TAFSFile(fileName, TAFSGlobalConfig.getString(TAFSOptions.cacheServers));

	try
	{
		fileByteArray = myTAFSFile.GetFile();
		haveFileData = true;
	}
	catch(FileNotFoundException e)
	{
		System.out.println("File not found, " + fileName + ": " + e.getMessage());
	}
	catch(IOException ioE)
	{
		System.out.println("File not read, " + fileName + ": " + ioE.getMessage());
	}

	if (haveFileData)
	{
		Socket					socket = null;
		String					host = "127.0.0.1";
		BufferedOutputStream	out = null;

		// Send the data over the socket
		System.out.println("Writing file to socket.");

		try
		{
			// Open the socket
			socket = new Socket(host, 4444);

			out = new BufferedOutputStream(socket.getOutputStream());

			ObjectOutputStream	oos = new ObjectOutputStream(out);
//			TAFSMessage			aMsg = new TAFSMessage();
			testClass			aMsg = new testClass("Robert", "Second STRING", 42);
			aMsg.myPayload = fileByteArray;
			oos.writeObject(aMsg);
			oos.flush();
			oos.close();

//			out.write(fileByteArray, 0, fileByteArray.length);

		}
		catch(UnknownHostException eUH)
		{
			System.out.println("UnknownHostException creating socket: " + eUH.getMessage());
		}
		catch(IOException eIO)
		{
			System.out.println("IOException while writing to socket: " + eIO.getMessage());
		}
		finally
		{
			if (socket != null)
			{
				System.out.println("Closing socket.");
				socket.close();
			}
			if (out != null)
			{
				System.out.println("Flushing output.");
				out.flush();
				System.out.println("Closing output.");
				out.close();
			}
		}
	}
}
}