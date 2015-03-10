/**
 * 
 */
package tafs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import tafsComm.TAFSCommHandler;
import tafsComm.TAFSMessage;
import tafsComm.TAFSMessageHandler;

/**
 * @author robert
 *
 */
public class TAFSClient
{
	private final static String	className = TAFSClient.class.getSimpleName();
	private final static Logger log = Logger.getLogger(className);

	public TAFSClient() throws FileNotFoundException, IOException
	{
		TAFSGlobalConfig.LoadConfigFromFile();
	}

	public byte[] getFile(String inFileName, Boolean inUseCache)
	{
		return getFileFromHost(inFileName, TAFSGlobalConfig.getString(TAFSOptions.ccIP), inUseCache);
	}

	public byte[] getFile(String inFileName)
	{
		return getFile(inFileName, true);
	}

	public void putFile(String inFileName, Boolean inUseCache) throws IOException
	{
		putFileToHost(inFileName, TAFSGlobalConfig.getString(TAFSOptions.ccIP), inUseCache);
	}

	public void putFile(String inFileName) throws IOException
	{
		putFile(inFileName, true);
	}

	private byte[] getFileFromHost(String inFileName, String inHostIP, Boolean inUseCache)
	{
		return getFileFromHost(inFileName, inHostIP, TAFSGlobalConfig.getInteger(TAFSOptions.ccListenPort), inUseCache);
	}

	private byte[] getFileFromHost(String inFileName, String inHostIP, Integer inHostPort, Boolean inUseCache)
	{
		Integer				listenPort = inHostPort;
		TAFSCommHandler		theCH = new TAFSCommHandler(listenPort);
		TAFSMessageHandler	theMH;
		TAFSMessage			responseMsg;
		ArrayList<String>	theArgs = new ArrayList<String>();
		byte[]				resultBytes = null;
		String				fileHostIP = "";
		Integer				fileHostPort = listenPort;

		log.info("Sending get request to " + inHostIP + " for file '" + inFileName + "' with" + (inUseCache ? "" : "out") + " cache");

		log.fine("Opening connection to " + inHostIP + ":" + listenPort);
		theCH.Open(inHostIP);
		theMH = new TAFSMessageHandler(theCH);

		theArgs.add(inFileName);
		theArgs.add(inUseCache ? TAFSCommands.cache.getCmdStr() : TAFSCommands.nocache.getCmdStr());
		log.fine("Sending message to " + inHostIP);
		theMH.SendMessage(TAFSCommands.getfile.getCmdStr(), theArgs, null);
		log.fine("Reading response from " + inHostIP);
		responseMsg = theMH.ReadMessage();

		if (responseMsg.myArgs.size() > 0)
			fileHostIP = responseMsg.myArgs.get(0);
		if (responseMsg.myArgs.size() > 1)
			fileHostPort = Integer.parseInt(responseMsg.myArgs.get(1));
		log.info("Response from " + inHostIP + ": " + responseMsg.myMsg + " " + fileHostIP + ":" + fileHostPort);

		theArgs.clear();
		theMH.SendMessage(TAFSCommands.ok.getCmdStr(), theArgs, null);

		log.fine("Closing connection to " + inHostIP);
		theCH.Close();

		// If useloc response is received, re-send request to the IP address in the response
		if (responseMsg.myMsg.equals(TAFSCommands.useloc.getCmdStr()))
		{
			if (fileHostIP.isEmpty())
				log.severe("Empty host IP received from " + inHostIP);
			else
				resultBytes = getFileFromHost(inFileName, fileHostIP, fileHostPort, inUseCache);
		}
		else
		// ok response means that payload is attached to the message
		if (responseMsg.myMsg.equals(TAFSCommands.ok.getCmdStr()))
		{
			resultBytes = responseMsg.myPayload;
			log.info("Payload received from " + inHostIP + ": " + resultBytes.length + " byte" + (resultBytes.length != 1 ? "s" : ""));
		}
		else
		{
			String	logMsg;

			logMsg = "Response from " + inHostIP + ": " + responseMsg.myMsg;
			if (responseMsg.myArgs.size() > 0)
				logMsg += ", " + responseMsg.myArgs.get(0);

			log.warning(logMsg);
		}

		return resultBytes;
	}

	private void putFileToHost(String inFileName, String inHostIP, Boolean inUseCache) throws IOException
	{
		putFileToHost(inFileName, inHostIP, TAFSGlobalConfig.getInteger(TAFSOptions.ccListenPort), inUseCache, false);
	}

	// First message should contain null for payload.  Response will contain IP address of host
	// to which a second message should be sent, this time with the file data in the payload.
	private void putFileToHost(String inFileName, String inHostIP, Integer inHostPort, Boolean inUseCache, Boolean inWithPayload) throws IOException
	{
		Integer				listenPort = inHostPort;
		TAFSCommHandler		theCH = new TAFSCommHandler(listenPort);
		TAFSMessageHandler	theMH;
		TAFSMessage			responseMsg;
		ArrayList<String>	theArgs = new ArrayList<String>();
		byte[]				resultBytes = null;
		String				fileHostIP = "";
		Integer				fileHostPort = listenPort;

		log.info("Sending put request to " + inHostIP + " for file '" + inFileName + "' with" + (inUseCache ? "" : "out") + " cache");

		log.fine("Opening connection to " + inHostIP + ":" + listenPort);
		theCH.Open(inHostIP);
		theMH = new TAFSMessageHandler(theCH);

		if (inWithPayload)
		{
			// Read the file from storage and attach it to the message.
			TAFSFile	theFile = new TAFSFile(inFileName, "", "");

			log.fine("Reading file from storage");

			theFile.SetCacheReads(false);
			theFile.SetCacheWrites(false);
			resultBytes = theFile.ReadFile();
		}
		else
			resultBytes = null;

		theArgs.add(inFileName);
		theArgs.add(String.valueOf(resultBytes != null ? resultBytes.length : 0));
		theArgs.add(inUseCache ? TAFSCommands.cache.getCmdStr() : TAFSCommands.nocache.getCmdStr());
		log.fine("Sending message to " + inHostIP);

		theMH.SendMessage(TAFSCommands.putfile.getCmdStr(), theArgs, resultBytes);
		log.fine("Reading response from " + inHostIP);
		responseMsg = theMH.ReadMessage();

		if (responseMsg.myArgs.size() > 0)
			fileHostIP = responseMsg.myArgs.get(0);
		if (responseMsg.myArgs.size() > 1)
			fileHostPort = Integer.parseInt(responseMsg.myArgs.get(1));
		log.info("Response from " + inHostIP + ": " + responseMsg.myMsg + " " + fileHostIP + ":" + fileHostPort);

		theArgs.clear();
		theMH.SendMessage(TAFSCommands.ok.getCmdStr(), theArgs, null);

		log.fine("Closing connection to " + inHostIP);
		theCH.Close();

		// If useloc response is received, re-send request to the IP address in the response
		if (responseMsg.myMsg.equals(TAFSCommands.useloc.getCmdStr()))
		{
			if (fileHostIP.isEmpty())
				log.severe("Empty host IP received from " + inHostIP);
			else
				putFileToHost(inFileName, fileHostIP, fileHostPort, inUseCache, true);
		}
		else
		// ok response means that payload was stored successfully on the host
		if (responseMsg.myMsg.equals(TAFSCommands.ok.getCmdStr()))
		{
			log.info("Response received: " + responseMsg.myMsg);
//			resultBytes = responseMsg.myPayload;
//			log.info("Payload received from " + inHostIP + ": " + resultBytes.length + " byte" + (resultBytes.length != 1 ? "s" : ""));
		}
		else
		{
			String	logMsg;

			logMsg = "Response from " + inHostIP + ": " + responseMsg.myMsg;
			if (responseMsg.myArgs.size() > 0)
				logMsg += ", " + responseMsg.myArgs.get(0);

			log.warning(logMsg);
		}
	}
}
