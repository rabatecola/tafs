package tafs;
/**
 * @author Robert Abatecola
 *
 */

import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.BufferedOutputStream;

import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

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

	if (args.length > 0)
	{
		fileName = args[0];
	}
	System.out.println("Transferring: " + fileName);

	// Tell spy to use the SunLogger
	Properties systemProperties = System.getProperties();
	systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
	System.setProperties(systemProperties);

	Logger.getLogger("net.spy.memcached").setLevel(Level.FINEST);
//	Logger.getLogger("net.spy.memcached").setLevel(Level.WARNING);

	myTAFSFile = new TAFSFile(fileName, "127.0.0.1:11211,127.0.0.1:11212");

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
			out.write(fileByteArray, 0, fileByteArray.length);
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