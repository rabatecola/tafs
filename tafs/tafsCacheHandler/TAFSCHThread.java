/**
 * 
 */
package tafsCacheHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import tafs.TAFSCommands;
import tafs.TAFSFile;
import tafs.TAFSGlobalConfig;
import tafs.TAFSOptions;
import tafsComm.TAFSCommHandler;
import tafsComm.TAFSMessage;
import tafsComm.TAFSMessageHandler;

/**
 * @author robert
 *
 */
public class TAFSCHThread implements Runnable
{
	TAFSCommHandler	myCH;
	String			myMsg;

	private final static Logger log = Logger.getLogger(TAFSCHThread.class.getName());

	private class FileCachingThread implements Runnable
	{
		String	myFileName;

		public FileCachingThread(String inFileName)
		{
			myFileName = inFileName;

			// Start me up
//			new Thread(this).start();
		}

		public void run()
		{
			TAFSFile	theFile;

			log.info("FileCachingThread running");
			theFile = new TAFSFile(myFileName, TAFSGlobalConfig.getString(TAFSOptions.cacheServers));
			try
			{
				theFile.GetFile();
//				outMsg.myMsg = TAFSCommands.ok.getCmdStr();
			}
			catch(IOException eIO)
			{
//				outMsg.myMsg = TAFSCommands.notok.getCmdStr();
//				outMsg.myArgs.add("IOException: " + eIO.getMessage());
			}
		}
	}

	public TAFSCHThread(TAFSCommHandler inCH, String inMsg)
	{
		myCH = inCH;
		myMsg = inMsg;

		// Start me up
		new Thread(this).start();
	}

	public void run()
	{
		TAFSMessageHandler	aMH;
		TAFSMessage			aMsg;
		String				dummyMsg = "GetFile";
		TAFSCommands		aCmd;

		log.info("\n" + myMsg + " - ");

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
					case preprecvfile:
						PrepRecvFile(aMH, aMsg);
						break;
	
					case prepsendfile:
						PrepSendFile(aMH, aMsg);
						break;
	
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
	//     prepsendfile <filename> [cache|nocache]
	// Responses:
	// 	   ok [nocache]
	//	   notok <exception message>
	private void PrepSendFile(TAFSMessageHandler inRequesterMH, TAFSMessage inMsg)
	{
		// Get the file name and find it in the cache
		String		fileName = inMsg.myArgs.get(0);
		Boolean		useCache = true;
		TAFSMessage	outMsg = new TAFSMessage();

		log.info("PrepSendFile called, file name = '" + fileName + "'");

		if (inMsg.myArgs.size() > 1)
			useCache = inMsg.myArgs.get(1) != "nocache";

		// If not using cache, nothing left to do.
		if (useCache)
		{
			// Start thread to handle reading and caching of the file
			new FileCachingThread(fileName);
		}
		else
			outMsg.myArgs.add("nocache");

		outMsg.myMsg = TAFSCommands.ok.getCmdStr();

		// Notify the requester of the outcome.
		log.info("Notifying requester of result");
		inRequesterMH.SendMessage(outMsg);
	}

	// Message format:
	//     getfile <filename> [cache|nocache]
	// Responses:
	//	   ok with serialized TAFSMessage object
	//		   ////NO - followed by -
	//		   ////NO <serialized TAFSMessage object>
	//	   notok <exception message>
	private void GetFile(TAFSMessageHandler inRequesterMH, TAFSMessage inMsg)
	{
		String		fileName;
		TAFSMessage	response;
		TAFSMessage	outMsg = new TAFSMessage();
		TAFSFile	theFile;
		Boolean		useCache = true;

		// Find the file in the catalog.
		fileName = inMsg.myArgs.get(0);
		log.info("GetFile called, file name = '" + fileName + "'");

		if (inMsg.myArgs.size() > 1)
			useCache = inMsg.myArgs.get(1).equals(TAFSCommands.cache.getCmdStr());

		theFile = new TAFSFile(fileName, TAFSGlobalConfig.getString(TAFSOptions.cacheServers));
		theFile.SetCacheReads(useCache);
		try
		{
			outMsg.myPayload = theFile.GetFile();
			outMsg.myMsg = TAFSCommands.ok.getCmdStr();
			outMsg.myArgs.add(fileName);
			log.fine(fileName + " read");
		}
		catch(IOException eIO)
		{
			outMsg.myMsg = TAFSCommands.notok.getCmdStr();
			outMsg.myArgs.add("IOException: " + eIO.getMessage());
			log.severe("IOException: " + eIO.getMessage() + ", " + fileName + "not read");
		}

		// Notify the requester of the result.
		log.info("Notifying requester of result");
		inRequesterMH.SendMessage(outMsg);
		response = inRequesterMH.ReadMessage();
		log.info("Response from requester: " + response.myMsg);
	}

	// Message format:
	//     preprecvfile <filename> <size> [cache|nocache]
	// Responses:
	// 	   ok [nocache]
	//	   notok <exception message>
	// TODO: Use size parameter to check if enough space is available to store the file
	private void PrepRecvFile(TAFSMessageHandler inRequesterMH, TAFSMessage inMsg)
	{
		// Get the file name and find it in the cache
		String		fileName = inMsg.myArgs.get(0);
		Boolean		useCache = true;
		TAFSMessage	outMsg = new TAFSMessage();

		log.info("PrepRecvFile called, file name = '" + fileName + "'");

		if (inMsg.myArgs.size() > 1)
			useCache = inMsg.myArgs.get(1) != "nocache";

		// If not using cache, nothing left to do.
		if (useCache)
		{
			// Start thread to handle reading and caching of the file
//			new FileCachingThread(fileName);
		}
		else
			outMsg.myArgs.add("nocache");

		outMsg.myMsg = TAFSCommands.ok.getCmdStr();

		// Notify the requester of the outcome.
		log.info("Notifying requester of result");
		inRequesterMH.SendMessage(outMsg);
	}

	// Message format:
	//     putfile <filename> [cache|nocache]
	//	   Message payload contains file data
	// Responses:
	//	   ok
	//	   notok <exception message>
	//
	// Notification to Cache coordinator:
	//	   addtocat <filename> <hostIP>
	private void PutFile(TAFSMessageHandler inRequesterMH, TAFSMessage inMsg)
	{
		String		fileName;
		TAFSMessage	response;
		TAFSMessage	outMsg = new TAFSMessage();
		TAFSFile	theFile;
		Boolean		useCache = true;
		Boolean		writeSuccess = false;

		// Find the file in the catalog.
		fileName = inMsg.myArgs.get(0);
		log.info("PutFile called, file name = '" + fileName + "'");

		if (inMsg.myArgs.size() > 1)
			useCache = inMsg.myArgs.get(1).equals(TAFSCommands.cache.getCmdStr());

		// Write the file to storage
//		WriteFile(fileName, inMsg.myPayload);

		theFile = new TAFSFile(fileName, TAFSGlobalConfig.getString(TAFSOptions.cacheServers));
		theFile.SetCacheWrites(useCache);

		try
		{
			theFile.WriteFile(inMsg.myPayload);
			writeSuccess = true;

			outMsg.myMsg = TAFSCommands.ok.getCmdStr();
			outMsg.myArgs.add(fileName);
		}
		catch(IOException eIO)
		{
			outMsg.myMsg = TAFSCommands.notok.getCmdStr();
			outMsg.myArgs.add("IOException: " + eIO.getMessage());
		}

		// Notify the requester of the result.
		log.info("Notifying requester of result");
		inRequesterMH.SendMessage(outMsg);
		response = inRequesterMH.ReadMessage();
		log.info("Response from requester: " + response.myMsg);

		// Notify the Cache Coordinator of the newly received file.
		if (writeSuccess)
		{
			TAFSCommHandler		ccCH = new TAFSCommHandler(TAFSGlobalConfig.getInteger(TAFSOptions.ccListenPort));
			TAFSMessageHandler	ccMH;

			log.info("Notifying coordinator of newly received file");

			// TODO Rework to use exceptions properly
			ccCH = new TAFSCommHandler(TAFSGlobalConfig.getInteger(TAFSOptions.ccListenPort));
			ccCH.Open(TAFSGlobalConfig.getString(TAFSOptions.ccIP));
			ccMH = new TAFSMessageHandler(ccCH);

			outMsg = new TAFSMessage();
			outMsg.myMsg = TAFSCommands.addtocat.getCmdStr();
			outMsg.myArgs.add(fileName);
			try
			{
				outMsg.myArgs.add(InetAddress.getLocalHost().getHostAddress());
			}
			catch(UnknownHostException eUH)
			{
				// If this happens, Cache Coordinator will use IP address on the connection
				log.warning("Could not get my IP address: " + eUH.getMessage());
			}

			ccMH.SendMessage(outMsg);
			response = ccMH.ReadMessage();
			log.info("Response from coordinator: " + response.myMsg);

			ccCH.Close();
		}
	}

	private void DelFile(TAFSMessageHandler inRequesterMH, TAFSMessage inMsg)
	{
		log.info("DelFile called");
	}
}
