/**
 * 
 */
package tafs;

import java.util.ArrayList;
import java.util.logging.Logger;

import tafsCacheCoordinator.TAFSCacheCoordinator;
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

	public byte[] getFile(String inFileName, Boolean inUseCache)
	{
		return getFileFromHost(inFileName, TAFSGlobalConfig.getString(TAFSOptions.ccIP), inUseCache);
	}

	public byte[] getFile(String inFileName)
	{
		return getFile(inFileName, true);
	}

	private byte[] getFileFromHost(String inFileName, String inHostIP, Boolean inUseCache)
	{
		Integer				listenPort = TAFSGlobalConfig.getInteger(TAFSOptions.listenPort);
		TAFSCommHandler		theCH = new TAFSCommHandler(listenPort);
		TAFSMessageHandler	theMH;
		TAFSMessage			responseMsg;
		ArrayList<String>	theArgs;
		byte[]				resultBytes = null;

		log.info("Sending request to " + inHostIP + " for file '" + inFileName + "' with" + (inUseCache ? "" : "out") + " cache");

		log.fine("Opening connection to " + inHostIP + ":" + listenPort);
		theCH.Open(inHostIP);
		theMH = new TAFSMessageHandler(theCH);

		theArgs.add(inFileName);
		theArgs.add(inUseCache ? TAFSCommands.cache.getCmdStr() : TAFSCommands.nocache.getCmdStr());
		log.fine("Sending message to " + inHostIP);
		theMH.SendMessage(TAFSCommands.getfile.getCmdStr(), theArgs, null);
		log.fine("Reading response from " + inHostIP);
		responseMsg = theMH.ReadMessage();

		log.fine("Closing connection to " + inHostIP);
		theCH.Close();

		// If useloc response is received, re-send request to the IP address in the response
		if (responseMsg.myMsg.equals(TAFSCommands.useloc.getCmdStr()))
		{
			String	hostIP = responseMsg.myArgs.get(0);

			log.info("Response from " + inHostIP + ": " + responseMsg.myMsg + " " + hostIP);
			resultBytes = getFileFromHost(inFileName, hostIP, inUseCache);
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
}
