/**
 * 
 */
package tafsCacheCoordinator;

//import java.net.Socket;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import tafs.TAFSCatalog;
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
		TAFSMessageHandler	aMH;
		TAFSMessage			aMsg;
		String				dummyMsg = "GetFilex";
		TAFSCommands		aCmd;

		log.info(myMsg);

		try
		{
			aMH = new TAFSMessageHandler(myCH);
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
	
					default:
						log.warning("Unhandled command: " + dummyMsg);
						break;
				}
			}
			catch(IllegalArgumentException eIA)
			{
				log.warning("Unknown command received: " + dummyMsg);
			}

			Thread.sleep(1350);
		}
		catch (InterruptedException eIE)
		{
		}

		myCH.Close();
	}

	// Message format:
	//     getfile <filename> [cache|nocache]
	// Responses:
	// 	   useloc <host IP> <hostPort>
	//	   notok <exception message>
	private void GetFile(TAFSMessageHandler inRequesterMH, TAFSMessage inMsg)
	{
		String				fileName;
		Boolean				useCache = true;
		String				hostIP;
		TAFSMessage			response;
		TAFSMessage			outMsg = new TAFSMessage();
		TAFSCommHandler		hostCH;
		TAFSMessageHandler	hostMH;

		log.info("GetFile called");

		// Find the file in the catalog.
		fileName = inMsg.myArgs.get(0);
		if (inMsg.myArgs.size() > 1)
			useCache = inMsg.myArgs.get(1).equals(TAFSCommands.cache.getCmdStr());
		synchronized(myCatalog)
		{
			hostIP = myCatalog.GetFileEntryServerID(fileName);
		}

		log.info("File name: '" + fileName + "'");

		// If caching, notify the file's host of the impending request.
		// TODO consider sending this request in a separate thread
		if (useCache)
		{
			outMsg.myMsg = TAFSCommands.prepsendfile.getCmdStr();
			outMsg.myArgs.add(fileName);

			// Connect on cache handler's port
			log.info("Connecting to cache handler");
			hostCH = new TAFSCommHandler(TAFSGlobalConfig.getInteger(TAFSOptions.chListenPort));
			hostCH.Open(hostIP);
			hostMH = new TAFSMessageHandler(hostCH);

			hostMH.SendMessage(outMsg);

			response = hostMH.ReadMessage();
			log.info("Response from host: " + response.myMsg);

			hostCH.Close();
		}

		// Notify the requester of the location of the file.
		log.info("Notifying requester of file location " + hostIP);
		outMsg.myMsg = TAFSCommands.useloc.getCmdStr();
		outMsg.myArgs.clear();
		outMsg.myArgs.add(hostIP);
		outMsg.myArgs.add(TAFSGlobalConfig.getString(TAFSOptions.chListenPort));

		inRequesterMH.SendMessage(outMsg);
		response = inRequesterMH.ReadMessage();
		log.info("Response from requester: " + response.myMsg);
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

		log.info("PutFile called");

		// Find the file in the catalog.
		fileName = inMsg.myArgs.get(0);
		log.info("File name: '" + fileName + "'");

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
			log.info("File already present on " + hostIP);

		// Notify the requester of the location of the file.
		log.info("Notifying requester of file location " + hostIP);
		outMsg.myMsg = TAFSCommands.useloc.getCmdStr();
		outMsg.myArgs.clear();
		outMsg.myArgs.add(hostIP);
		outMsg.myArgs.add(TAFSGlobalConfig.getString(TAFSOptions.chListenPort));

		inRequesterMH.SendMessage(outMsg);
		response = inRequesterMH.ReadMessage();
		log.info("Response from requester: " + response.myMsg);
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

		log.info("AddToCat called");

		fileName = inMsg.myArgs.get(0);
		if (inMsg.myArgs.size() > 1)
			hostIP = inMsg.myArgs.get(1);
		else
		{
			hostIP = inRequesterMH.GetRemoteIP();
		}

		log.info("File name: '" + fileName + "', host IP: " + hostIP);

		synchronized(myCatalog)
		{
			myCatalog.SetFileEntry(fileName, hostIP);
			if (log.isLoggable(Level.FINEST))
				myCatalog.DisplayEntries();
		}

		// Notify the requester of the location of the file.
		log.info("Notifying requester of result");
		outMsg.myMsg = TAFSCommands.ok.getCmdStr();

		inRequesterMH.SendMessage(outMsg);
	}

	private void DelFile(TAFSMessageHandler inRequesterMH, TAFSMessage inMsg)
	{
		log.info("DelFile called");
	}
}
