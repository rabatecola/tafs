/**
 * 
 */
package tafsCacheCoordinator;

//import java.net.Socket;

import java.util.logging.Logger;

import tafs.TAFSCatalog;
import tafs.TAFSCommands;
import tafs.TAFSGlobalConfig;
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

	private final static Logger log = Logger.getLogger(TAFSCCThread.class.getName());

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

		log.info("\n" + myMsg + " - ");

		try
		{
			aMH = new TAFSMessageHandler(myCH);
			aMsg = aMH.ReadMessage();

			//dummyMsg = aMsg.myMsg;
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
	// 	   useloc <host IP>
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
			useCache = inMsg.myArgs.get(1) != "nocache";
		hostIP = myCatalog.GetFileEntryServerID(fileName);

		// If caching, notify the file's host of the impending request.
		// TO-DO: consider sending this request in a separate thread
		if (useCache)
		{
			outMsg.myMsg = TAFSCommands.prepsendfile.getCmdStr();
			outMsg.myArgs.add(fileName);

			hostCH = new TAFSCommHandler(TAFSGlobalConfig.listenPort);
			hostCH.Open(hostIP);
			hostMH = new TAFSMessageHandler(hostCH);

			hostMH.SendMessage(outMsg);

			response = hostMH.ReadMessage();
			log.info("Response from host: " + response.myMsg);

			hostCH.Close();
		}

		// Notify the requester of the location of the file.
		outMsg.myMsg = TAFSCommands.useloc.getCmdStr();
		outMsg.myArgs.clear();
		outMsg.myArgs.add(hostIP);

		inRequesterMH.SendMessage(outMsg);
		response = inRequesterMH.ReadMessage();
		log.info("Response from requester: " + response.myMsg);
	}

	private void PutFile(TAFSMessageHandler inRequesterMH, TAFSMessage inMsg)
	{
		log.info("PutFile called");
	}

	private void DelFile(TAFSMessageHandler inRequesterMH, TAFSMessage inMsg)
	{
		log.info("DelFile called");
	}
}
