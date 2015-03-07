package tafs;
/**
 * @author Robert Abatecola
 *
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.InputStream;

import tafsComm.TAFSMessage;

public class testServer
{
	static ServerSocket	serverSocket = null;
	static Socket		socket = null;
	static InputStream	is = null;
	static int			bufferSize = 0;
	static TAFSFile		myTAFSFile = null;
	static Boolean		testSerialization = true;

/**
 * @param args the command line arguments
 * @throws ClassNotFoundException 
 */
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		// Tell spy to use the SunLogger
		Properties systemProperties = System.getProperties();
		systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
		System.setProperties(systemProperties);

		Logger.getLogger("net.spy.memcached").setLevel(Level.FINEST);
//		Logger.getLogger("net.spy.memcached").setLevel(Level.OFF);
//		Logger.getLogger("net.spy.memcached").setLevel(Level.WARNING);

		try
		{
			serverSocket = new ServerSocket(4444);
		}
		catch (IOException ex)
		{
			System.out.println("Can't setup server on this port number.");
		}

		while (true)
			RunLoop();

//		serverSocket.close();
	}

	static void RunLoop() throws IOException, ClassNotFoundException
	{
		try
		{
			socket = serverSocket.accept();
		}
		catch (IOException ex)
		{
			System.out.println("Can't accept client connection.");
		}

		try
		{
			is = socket.getInputStream();
	
			bufferSize = socket.getReceiveBufferSize();
			System.out.println("Buffer size: " + bufferSize);
		}
		catch (IOException ex)
		{
			System.out.println("Can't get socket input stream. ");
		}

		myTAFSFile = new TAFSFile("spymemcached-2.10.3-javadoc.jar_COPIED", "127.0.0.1:11211,127.0.0.1:11212");
		myTAFSFile.SetCacheWrites(true);
		myTAFSFile.OpenForWriting();

		if (testSerialization)
		{
			ObjectInputStream	ois = new ObjectInputStream(is);
			testClass			aMsg = (testClass)ois.readObject();
//			TAFSMessage			aMsg = (TAFSMessage)ois.readObject();

			System.out.println("'" + aMsg.myName + "', '" + aMsg.mySecondString + "', " + aMsg.myInteger);
			myTAFSFile.Write(aMsg.myPayload, aMsg.myPayload.length);
			ois.close();
		}
		else
		{
			byte[] bytes = new byte[bufferSize];
		
			int count;
		
			while ((count = is.read(bytes)) > 0)
			{
				myTAFSFile.Write(bytes, count);
			}
		}

		myTAFSFile.Flush();
		myTAFSFile.Close();
		is.close();
		socket.close();
	}
}