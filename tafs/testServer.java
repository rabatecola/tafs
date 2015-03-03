/**
 * @author Robert Abatecola
 *
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.InputStream;

public class testServer
{

/**
 * @param args the command line arguments
 */
public static void main(String[] args) throws IOException
{
	ServerSocket			serverSocket = null;
	Socket					socket = null;
	InputStream				is = null;
	int						bufferSize = 0;
	TAFSFile				myTAFSFile = null;

	// Tell spy to use the SunLogger
	Properties systemProperties = System.getProperties();
	systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
	System.setProperties(systemProperties);

	Logger.getLogger("net.spy.memcached").setLevel(Level.FINEST);
//	Logger.getLogger("net.spy.memcached").setLevel(Level.WARNING);

	try
	{
		serverSocket = new ServerSocket(4444);
	}
	catch (IOException ex)
	{
		System.out.println("Can't setup server on this port number.");
	}

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

	byte[] bytes = new byte[bufferSize];

	int count;

	while ((count = is.read(bytes)) > 0)
	{
		myTAFSFile.Write(bytes, count);
	}

	myTAFSFile.Flush();
	myTAFSFile.Close();
	is.close();
	socket.close();
	serverSocket.close();
}
}