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

	private Integer			myPortNumber;
	private Socket			mySocket = null;
	private ServerSocket	myServerSocket = null;

	public TAFSCommHandler(Integer inPortNumber)
	{
		myPortNumber = inPortNumber;
	}

	public Integer GetPort()
	{
		return myPortNumber;
	}

	public void SetSocket(Socket inSocket)
	{
		mySocket = inSocket;
	}

	public String GetRemoteIP()
	{
		String	resultIP = "";

		if (mySocket != null)
			resultIP = mySocket.getRemoteSocketAddress().toString();

		return resultIP;
	}

	public TAFSCommHandler Listen() throws IOException
	{
		if (myServerSocket == null)
		{
			myServerSocket = new ServerSocket(myPortNumber);
//			myServerSocket.bind(new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), myPortNumber));
		}

		TAFSCommHandler	aCH = null;

		// Use ServerSocket to listen for incoming connection.
		// Return a new comm. handler with the open socket.
//        System.out.println("Waiting for client on port " +
//        serverSocket.getLocalPort() + "...");
	      
//		try {
			Socket	newSocket = myServerSocket.accept();
			aCH = new TAFSCommHandler(myPortNumber);
			log.info("Remote address is " + ((InetSocketAddress)newSocket.getRemoteSocketAddress()).getAddress() + ":" + ((InetSocketAddress)newSocket.getRemoteSocketAddress()).getPort());
			aCH.SetSocket(newSocket);

//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		return aCH;
	}

	public Socket Open(String inIPAddr)
	{
//		Socket clientSocket=null;
//		System.out.println("Connecting to " + inIPAddr
//                + " on port " + myPortNumber);
		// Use Socket to create an outgoing connection
		try {
			mySocket = new Socket(inIPAddr, myPortNumber);
			
	
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		return mySocket;
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

	
	public  Object ReadObject() throws IOException
	{

		Object	anObject = null;



			try{

		    		ObjectInputStream ois = 
		                     new ObjectInputStream(mySocket.getInputStream());
		    		anObject = ois.readObject();
//                    ois.close();
		    		log.fine("deserialized");

//        			System.out.println("mymessage"+ aMessage.myMsg);
//        			System.out.println("myargs"+ aMessage.myArgs);
//        			System.out.println("mypayload"+ aMessage.myPayload);
				
		} catch (IOException eIO){ 
				eIO.printStackTrace();
	} catch (ClassNotFoundException eCNF){ 
		eCNF.printStackTrace();
		}
		return anObject;
		}

	public void WriteObject(Object inObject) throws IOException{
//		TAFSMessage bMessage = new TAFSMessage();	
		
		try{
			ObjectOutputStream  oos = new 
                    ObjectOutputStream(mySocket.getOutputStream());

			
  		       oos.writeObject(inObject);
			

//			oos.close();
  		     log.fine("serialized");

//			System.out.println("mymessage"+ bMessage.myMsg);
//			System.out.println("myargs"+ bMessage.myArgs);
//			System.out.println("mypayload"+ bMessage.myPayload);
		
		} catch (IOException e){ 
			e.printStackTrace();
			}

//		return bMessage;
	}
}
