/**
 * 
 */
package tafsCacheHandler;

import java.io.IOException;
import java.util.logging.Logger;

import tafs.TAFSCommands;
import tafs.TAFSFile;
import tafs.TAFSGlobalConfig;
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
			new Thread(this).start();
		}

		public void run()
		{
			TAFSFile	theFile;

			log.info("FileCachingThread running");
			theFile = new TAFSFile(myFileName, TAFSGlobalConfig.cacheServers);
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

			//dummyMsg = aMsg.myMsg;
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

		log.info("PrepRecvFile called, file name = '" + fileName + "'");

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
		inRequesterMH.SendMessage(outMsg);
	}

	// Message format:
	//     getfile <filename> [cache|nocache]
	// Responses:
	//	   ok
	//		   - followed by -
	//		   <serialized TAFSMessage object>
	//	   notok <exception message>
	private void GetFile(TAFSMessageHandler inRequesterMH, TAFSMessage inMsg)
	{
		String		fileName;
		TAFSMessage	response;
		TAFSMessage	outMsg = new TAFSMessage();
		TAFSFile	theFile;

		// Find the file in the catalog.
		fileName = inMsg.myArgs.get(0);
		log.info("GetFile called, file name = '" + fileName + "'");

		theFile = new TAFSFile(fileName, TAFSGlobalConfig.cacheServers);
		try
		{
			outMsg.myPayload = theFile.GetFile();
			outMsg.myMsg = TAFSCommands.ok.getCmdStr();
			outMsg.myArgs.add(fileName);
		}
		catch(IOException eIO)
		{
			outMsg.myMsg = TAFSCommands.notok.getCmdStr();
			outMsg.myArgs.add("IOException: " + eIO.getMessage());
		}

		// Notify the requester of the result.
		inRequesterMH.SendMessage(outMsg);
		response = inRequesterMH.ReadMessage();
		log.info("Response from requester: " + response.myMsg);
	}

	private void PrepRecvFile(TAFSMessageHandler inRequesterMH, TAFSMessage inMsg)
	{
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
