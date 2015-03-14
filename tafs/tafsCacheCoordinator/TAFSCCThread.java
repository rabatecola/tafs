/**
 * 
 */
package tafsCacheCoordinator;

//import java.net.Socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import tafs.TAFSCommands;
import tafs.TAFSGlobalConfig;
import tafs.TAFSOptions;
import tafsComm.TAFSCommHandler;
import tafsComm.TAFSMessage;
import tafsComm.TAFSMessageHandler;

/**
 * @author Robert Abatecola
 *
 */
public class TAFSCCThread implements Runnable
{
	TAFSCommHandler	myCH;
	TAFSCatalog		myCatalog;
	String			myMsg;

	private final static String	className = TAFSCCThread.class.getSimpleName();
	private final static Logger log = Logger.getLogger(className);

	private static Integer	nextHostIndex = 0;

	public TAFSCCThread(TAFSCommHandler inCH, TAFSCatalog inCatalog, String inMsg)
	{
//		log.setLevel(TAFSGlobalConfig.logLevel);

		myCH = inCH;
		myCatalog = inCatalog;
		myMsg = inMsg;

		// Start me up
		new Thread(this).start();
	}

	public void run()
	{
		TAFSMessageHandler	aMH = null;
		TAFSMessage			aMsg;
		String				dummyMsg = "";
		TAFSCommands		aCmd;
		Boolean				keepRunning = true;

		log.fine(myMsg);

		aMH = new TAFSMessageHandler(myCH);

		while (keepRunning)
		{
			try
			{
				aMsg = aMH.ReadMessage();

				dummyMsg = aMsg.myMsg;
				try
				{
					aCmd = TAFSCommands.valueOf(dummyMsg.toLowerCase());
					switch (aCmd)
					{
						case getfile:
							GetFile(aMH, aMsg);
							break;

						case putfile:
							PutFile(aMH, aMsg);
							break;

						case addtocat:
							AddToCat(aMH, aMsg);
							break;

						case delfile:
							DelFile(aMH, aMsg);
							break;

						case bye:
							keepRunning = false;
							break;

						default:
							log.warning("Unhandled command: " + dummyMsg);
							break;
					}
				}
				catch(IllegalArgumentException eIA)
				{
					log.warning("Unknown command received: " + dummyMsg);
				}

//				Thread.sleep(1350);
			}
//			catch (InterruptedException eIE)
//			{
//			}
			catch(IOException eIO)
			{
				
			}
			catch(ClassNotFoundException eCNF)
			{
				
			}
			finally
			{
				// TEMP?
				keepRunning = false;
			}
		}
		// Rely on the client to close the connection
		//aMH.Close();
		// Decrement active thread count.
		try
		{
			TAFSCacheCoordinator.activeThreads.acquire();
			TAFSCacheCoordinator.maxThreads.release();
		}
		catch(InterruptedException eI) { /* Ignore */ }
	}

	// Message format:
	//     getfile <filename> [cache|nocache]
	// Responses:
	// 	   useloc <host IP> <hostPort>
	//	   notok <exception message>
	private void GetFile(TAFSMessageHandler inRequesterMH, TAFSMessage inMsg)
	{
		String				fileName = "";
		Boolean				useCache = true;
		String				hostIP = "";
		TAFSMessage			response = null;
		TAFSMessage			outMsg = new TAFSMessage();
		TAFSCommHandler		hostCH = null;
		TAFSMessageHandler	hostMH = null;

		log.fine("GetFile called");

		// Find the file in the catalog.
		fileName = inMsg.myArgs.get(0);
		if (inMsg.myArgs.size() > 1)
			useCache = inMsg.myArgs.get(1).equals(TAFSCommands.cache.getCmdStr());
		synchronized(myCatalog)
		{
			hostIP = myCatalog.GetFileEntryServerID(fileName);
		}

		log.fine("File name: '" + fileName + "'");

		// If caching, notify the file's host of the impending request.
		// TODO consider sending this request in a separate thread
		if (useCache && hostIP != null)
		{
			outMsg.myMsg = TAFSCommands.prepsendfile.getCmdStr();
			outMsg.myArgs.add(fileName);

			// Connect on cache handler's port
			log.fine("Connecting to cache handler at " + hostIP);
			hostCH = new TAFSCommHandler(TAFSGlobalConfig.getInteger(TAFSOptions.chListenPort));
			try
			{
				hostCH.Open(hostIP);
				hostMH = new TAFSMessageHandler(hostCH);

				hostMH.SendMessage(outMsg);

				response = hostMH.ReadMessage();
				log.fine("Response from host: " + response.myMsg);
			}
			catch(IOException eIO)
			{
				if (eIO.getMessage().equalsIgnoreCase("Connection reset"))
				{
					log.warning("Could not receive message: " + eIO.getMessage());
				}
				else
				{
					log.severe("Could not receive message: " + eIO.getMessage());
					eIO.printStackTrace();
				}
			}
			catch(ClassNotFoundException eCNF)
			{
				log.severe("Could not read or send message: " + eCNF.getMessage());
				eCNF.printStackTrace();
			}
			finally
			{
				if (hostMH != null)
					hostMH.Close();
			}
		}

		// Notify the requester of the location of the file if it was found in the catalog.
		if (hostIP != null)
		{
			log.fine("Notifying requester of file location " + hostIP);
			outMsg.myMsg = TAFSCommands.useloc.getCmdStr();
			outMsg.myArgs.clear();
			outMsg.myArgs.add(hostIP);
			outMsg.myArgs.add(TAFSGlobalConfig.getString(TAFSOptions.chListenPort));
		}
		else
		{
			log.fine("File not found in catalog.  Notifying requester.");
			outMsg.myMsg = TAFSCommands.notok.getCmdStr();
			outMsg.myArgs.clear();
			outMsg.myArgs.add("File not found in catalog");
		}

		try
		{
			inRequesterMH.SendMessage(outMsg);
			response = inRequesterMH.ReadMessage();
			log.fine("Response from requester: " + response.myMsg);
		}
		catch(IOException eIO)
		{
			log.severe("Could not read or send message: " + eIO.getMessage());
			eIO.printStackTrace();
		}
		catch(ClassNotFoundException eCNF)
		{
			log.severe("Could not read or send message: " + eCNF.getMessage());
			eCNF.printStackTrace();
		}
	}

	// Message format:
	//     putfile <filename> <size> [cache|nocache]
	//	   Message payload should be empty
	// Responses:
	// 	   useloc <host IP> <hostPort>
	//	   notok <exception message>
	private void PutFile(TAFSMessageHandler inRequesterMH, TAFSMessage inMsg)
	{
		String				fileName;
//		String				fileSizeStr;
//		Boolean				useCache = true;
		String				hostIP;
		TAFSMessage			response;
		TAFSMessage			outMsg = new TAFSMessage();
//		TAFSCommHandler		hostCH;
//		TAFSMessageHandler	hostMH;

		log.fine("PutFile called");

		// Find the file in the catalog.
		fileName = inMsg.myArgs.get(0);
		log.fine("File name: '" + fileName + "'");

//		if (inMsg.myArgs.size() > 1)
//		{
//			fileSizeStr = inMsg.myArgs.get(1);
//			if (inMsg.myArgs.size() > 2)
//				useCache = inMsg.myArgs.get(2).equals(TAFSCommands.cache.getCmdStr());
//		}
		synchronized(myCatalog)
		{
			hostIP = myCatalog.GetFileEntryServerID(fileName);
		}

		if (hostIP == null || hostIP.isEmpty())
		{
			ArrayList<String>	chHostIPs = TAFSGlobalConfig.getArrayListString(TAFSOptions.chIP);

			synchronized (nextHostIndex)
			{
				if (nextHostIndex >= chHostIPs.size())
					nextHostIndex = 0;

				hostIP = chHostIPs.get(nextHostIndex);
				nextHostIndex++;
			}
		}
		else
			log.fine("File already present on " + hostIP);

		// Notify the requester of the location of the file.
		log.fine("Notifying requester of file location " + hostIP);
		outMsg.myMsg = TAFSCommands.useloc.getCmdStr();
		outMsg.myArgs.clear();
		outMsg.myArgs.add(hostIP);
		outMsg.myArgs.add(TAFSGlobalConfig.getString(TAFSOptions.chListenPort));

		try
		{
			inRequesterMH.SendMessage(outMsg);
			response = inRequesterMH.ReadMessage();
			log.fine("Response from requester: " + response.myMsg);
		}
		catch(IOException eIO)
		{
			log.severe("Could not read or send message: " + eIO.getMessage());
			eIO.printStackTrace();
		}
		catch(ClassNotFoundException eCNF)
		{
			log.severe("Could not read or send message: " + eCNF.getMessage());
			eCNF.printStackTrace();
		}
	}

	// Message format:
	//     addtocat <filename> <hostIP>
	//	   If hostIP is missing, use IP address of connection
	// Responses:
	// 	   ok
	//	   notok <exception message>
	private void AddToCat(TAFSMessageHandler inRequesterMH, TAFSMessage inMsg)
	{
		String				fileName;
		String				hostIP = "<error in host IP>";
		TAFSMessage			outMsg = new TAFSMessage();

		log.fine("AddToCat called");

		fileName = inMsg.myArgs.get(0);
		if (inMsg.myArgs.size() > 1)
			hostIP = inMsg.myArgs.get(1);
		else
		{
			hostIP = inRequesterMH.GetRemoteIP();
		}

		log.fine("File name: '" + fileName + "', host IP: " + hostIP);

		synchronized(myCatalog)
		{
			myCatalog.SetFileEntry(fileName, hostIP);
			if (log.isLoggable(Level.FINEST))
			{
				log.finest("Current catalog content:");
				myCatalog.DisplayEntries();
			}
		}

		// Notify the requester of the location of the file.
		log.fine("Notifying requester of result");
		outMsg.myMsg = TAFSCommands.ok.getCmdStr();

		try
		{
			inRequesterMH.SendMessage(outMsg);
		}
		catch(IOException eIO)
		{
			log.severe("Could not send message: " + eIO.getMessage());
			eIO.printStackTrace();
		}
	}

	private void DelFile(TAFSMessageHandler inRequesterMH, TAFSMessage inMsg)
	{
		log.fine("DelFile called");
	}
}
