/**
 * 
 */
package tafsComm;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

//import tafs.TAFSGlobalConfig;
//import tafs.TAFSOptions;


/**
 * @author Mahitha Thokala
 *
 */
// TODO RA: Rework to use exceptions properly
public class TAFSCommHandler
{
	private final static String	className = TAFSCommHandler.class.getSimpleName();
	private final static Logger log = Logger.getLogger(className);

	private String			myBindAddr = "";
	private Integer			myPortNumber = -1;
	private Socket			mySocket = null;
	private ServerSocket	myServerSocket = null;

	private ObjectInputStream	myOIS = null;
	private ObjectOutputStream	myOOS = null;

	public TAFSCommHandler(String inBindAddr, Integer inPortNumber) throws IOException
	{
		myPortNumber = inPortNumber;
		myBindAddr = inBindAddr;
		if (myServerSocket == null)
		{
			InetSocketAddress anISA;

			if (!myBindAddr.isEmpty())
			{
				anISA = new InetSocketAddress(myBindAddr, myPortNumber);
				myServerSocket = new ServerSocket();
				myServerSocket.bind(anISA);
			}
			else
				myServerSocket = new ServerSocket(myPortNumber);

//			myServerSocket = new ServerSocket();
//			myServerSocket.setPerformancePreferences(3, 2, 1);
			//myServerSocket.setReuseAddress(true);
//			myServerSocket.bind(anISA);
//			myServerSocket.bind(new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), myPortNumber));
		}
	}

	public TAFSCommHandler(Integer inPortNumber)
	{
		myPortNumber = inPortNumber;
	}

	public Integer GetPort()
	{
		return myPortNumber;
	}

	public void SetSocket(Socket inSocket) throws IOException
	{
		mySocket = inSocket;
		MakeStreams();
	}

	public String GetRemoteIP()
	{
		String	resultIP = "";

		if (mySocket != null)
		{
			resultIP = mySocket.getRemoteSocketAddress().toString();
			if (resultIP.startsWith("/"))
			{
				StringBuilder sb = new StringBuilder(resultIP);
				sb.deleteCharAt(0);
				resultIP = sb.toString();
			}
			if (resultIP.contains(":"))
			{
				String[]	ipAddrParts = resultIP.split(":");

				// IPv4
				if (ipAddrParts.length == 2)
					resultIP = ipAddrParts[0];
			}
		}

		return resultIP;
	}

	public Integer GetRemotePort()
	{
		Integer	resultPort = -1;

		if (mySocket != null)
			resultPort = ((InetSocketAddress)mySocket.getRemoteSocketAddress()).getPort();

		return resultPort;
	}

	public String GetLocalIP()
	{
		String	resultIP = "";

		if (mySocket != null)
			resultIP = mySocket.getLocalSocketAddress().toString();
		else
		if (myServerSocket != null)
			resultIP = myServerSocket.getLocalSocketAddress().toString();

		if (resultIP.startsWith("/"))
		{
			StringBuilder sb = new StringBuilder(resultIP);
			sb.deleteCharAt(0);
			resultIP = sb.toString();
		}

		if (resultIP.contains(":"))
		{
			String[]	ipAddrParts = resultIP.split(":");

			// IPv4
			if (ipAddrParts.length == 2)
				resultIP = ipAddrParts[0];
		}

		return resultIP;
	}

	public TAFSCommHandler Listen() throws IOException
	{
		TAFSCommHandler	aCH = null;

		// Use ServerSocket to listen for incoming connection.
		// Return a new comm. handler with the open socket.
//        System.out.println("Waiting for client on port " +
//        serverSocket.getLocalPort() + "...");
	      
//		try {
			Socket	newSocket = myServerSocket.accept();
//			newSocket.setKeepAlive(false);
//			log.warning("Old so_linger = " + newSocket.getSoLinger());
			newSocket.setSoLinger(true, 0);
//			newSocket.setReuseAddress(true);
//			newSocket.setTcpNoDelay(true);
			aCH = new TAFSCommHandler(myPortNumber);
			aCH.SetSocket(newSocket);
			log.info("Remote address is " + aCH.GetRemoteIP() + ":" + aCH.GetRemotePort());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		return aCH;
	}

	public Socket Open(String inIPAddr) throws UnknownHostException, IOException
	{
//		Socket clientSocket=null;
//		System.out.println("Connecting to " + inIPAddr
//                + " on port " + myPortNumber);
		// Use Socket to create an outgoing connection
//		try {
			mySocket = new Socket(inIPAddr, myPortNumber);
			MakeStreams();
	
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	
		return mySocket;
	}

	private void MakeStreams() throws IOException
	{
		// ObjectOutoutStream must be created first in order to prevent ObjectInputStream from blocking.
		myOOS = new ObjectOutputStream(mySocket.getOutputStream());
		myOIS = new ObjectInputStream(mySocket.getInputStream());
	}

	public void Flush() throws IOException
	{
		if (mySocket != null)
			mySocket.getOutputStream().flush();
	}

	public void Close()
	{
       try {
    	   if (mySocket != null)
    		   mySocket.close();
	} catch (IOException e) {
		
		e.printStackTrace();
	}
		mySocket = null;
	}

	
	public  Object ReadObject() throws IOException, ClassNotFoundException
	{

		Object	anObject = null;



//			try{

//		    		ObjectInputStream ois = 
//		                     new ObjectInputStream(mySocket.getInputStream());
		    		anObject = myOIS.readObject();
//                    ois.close();
		    		log.finest("deserialized");

//        			System.out.println("mymessage"+ aMessage.myMsg);
//        			System.out.println("myargs"+ aMessage.myArgs);
//        			System.out.println("mypayload"+ aMessage.myPayload);
				
//		} catch (IOException eIO){ 
//				eIO.printStackTrace();
//	} catch (ClassNotFoundException eCNF){ 
//		eCNF.printStackTrace();
//		}
		return anObject;
		}

	public void WriteObject(Object inObject) throws IOException{
//		TAFSMessage bMessage = new TAFSMessage();	
		
//		try{
//			ObjectOutputStream  oos = new 
//                    ObjectOutputStream(mySocket.getOutputStream());

			
  		       myOOS.writeObject(inObject);
			

//			oos.close();
  		     log.finest("serialized");

//			System.out.println("mymessage"+ bMessage.myMsg);
//			System.out.println("myargs"+ bMessage.myArgs);
//			System.out.println("mypayload"+ bMessage.myPayload);
		
//		} catch (IOException e){ 
//			e.printStackTrace();
//			}

//		return bMessage;
	}
}
