/**
 * 
 */
package tafs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Logger;

import tafsComm.TAFSCommHandler;
import tafsComm.TAFSMessage;
import tafsComm.TAFSMessageHandler;

/**
 * @author Robert Abatecola
 *
 */
public class TAFSClient
{
	private final static String	className = TAFSClient.class.getSimpleName();
	private final static Logger	log = Logger.getLogger(className);
	private	static				Boolean configLoaded = false;

	public TAFSClient() throws FileNotFoundException, IOException
	{
		synchronized (configLoaded)
		{
			if (!configLoaded)
			{
				TAFSGlobalConfig.LoadConfigFromFile();
				configLoaded = true;
			}
		}
	}

	public byte[] getFile(String inFileName, Boolean inUseCache)
	{
		return getFileFromHost(inFileName, TAFSGlobalConfig.getString(TAFSOptions.ccIP), inUseCache);
	}

	public byte[] getFile(String inFileName)
	{
		return getFile(inFileName, true);
	}

	public void putFile(String inSrcFileName, String inDstFileName, Boolean inUseCache) throws IOException
	{
		putFileToHost(inSrcFileName, inDstFileName, TAFSGlobalConfig.getString(TAFSOptions.ccIP), inUseCache);
	}

	public void putFile(String inSrcFileName, String inDstFileName) throws IOException
	{
		putFile(inSrcFileName, inDstFileName, true);
	}

	private byte[] getFileFromHost(String inFileName, String inHostIP, Boolean inUseCache)
	{
		return getFileFromHost(inFileName, inHostIP, TAFSGlobalConfig.getInteger(TAFSOptions.ccListenPort), inUseCache);
	}

	private byte[] getFileFromHost(String inFileName, String inHostIP, Integer inHostPort, Boolean inUseCache)
	{
		Integer				listenPort = inHostPort;
		TAFSCommHandler		theCH = new TAFSCommHandler(listenPort);
		TAFSMessageHandler	theMH = null;
		TAFSMessage			responseMsg;
		ArrayList<String>	theArgs = new ArrayList<String>();
		byte[]				resultBytes = null;
		String				fileHostIP = "";
		Integer				fileHostPort = listenPort;

		log.info("Sending get request to " + inHostIP + " for file '" + inFileName + "' with" + (inUseCache ? "" : "out") + " cache");

		log.fine("Opening connection to " + inHostIP + ":" + listenPort);
		try
		{
			theCH.Open(inHostIP);
			theMH = new TAFSMessageHandler(theCH);

			theArgs.add(inFileName);
			theArgs.add(inUseCache ? TAFSCommands.cache.getCmdStr() : TAFSCommands.nocache.getCmdStr());
			log.fine("Sending message to " + inHostIP);
			theMH.SendMessage(TAFSCommands.getfile.getCmdStr(), theArgs, null);
			log.fine("Reading response from " + inHostIP);
			try
			{
				responseMsg = theMH.ReadMessage();

				if (responseMsg.myArgs.size() > 0)
					fileHostIP = responseMsg.myArgs.get(0);
				if (responseMsg.myArgs.size() > 1)
					fileHostPort = Integer.parseInt(responseMsg.myArgs.get(1));
				log.info("Response from " + inHostIP + ": " + responseMsg.myMsg + " " + fileHostIP + ":" + fileHostPort);

				theArgs.clear();
				// TODO This could be a problematic area if the send fails... thread could hang on the other end waiting for a message
				theMH.SendMessage(TAFSCommands.ok.getCmdStr(), theArgs, null);

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
			}
			catch(IOException eIO)
			{
//				log.severe("Could not read message: " + eIO.getMessage());
//				eIO.printStackTrace();
				if (eIO.getMessage().equalsIgnoreCase("Connection reset") || eIO.getMessage().equalsIgnoreCase("Connection reset by peer") || eIO.getMessage().equalsIgnoreCase("Broken pipe"))
				{
					log.warning("Could not read message: " + eIO.getMessage());
				}
				else
				{
					log.severe("Could not read message: " + eIO.getMessage());
					eIO.printStackTrace();
				}
			}
			catch (ClassNotFoundException eCNF)
			{
				log.severe("Could not read message: " + eCNF.getMessage());
				eCNF.printStackTrace();
			}
		}
		catch (UnknownHostException eUH)
		{
			eUH.printStackTrace();
			log.severe("Could not open connection to " + inHostIP + ": " + eUH.getMessage());
		}
		catch (IOException eIO)
		{
			log.severe("Could not open connection to " + inHostIP + ": " + eIO.getMessage());
			eIO.printStackTrace();
		}
		finally
		{
			log.fine("Closing connection to " + inHostIP);
			if (theMH != null)
				theMH.Close();
		}

		return resultBytes;
	}

	private void putFileToHost(String inSrcFileName, String inDstFileName, String inHostIP, Boolean inUseCache) throws IOException
	{
		putFileToHost(inSrcFileName, inDstFileName, inHostIP, TAFSGlobalConfig.getInteger(TAFSOptions.ccListenPort), inUseCache, false);
	}

	// First message should contain null for payload.  Response will contain IP address of host
	// to which a second message should be sent, this time with the file data in the payload.
	private void putFileToHost(String inSrcFileName, String inDstFileName, String inHostIP, Integer inHostPort, Boolean inUseCache, Boolean inWithPayload) throws IOException
	{
		Integer				listenPort = inHostPort;
		TAFSCommHandler		theCH = new TAFSCommHandler(listenPort);
		TAFSMessageHandler	theMH = null;
		TAFSMessage			responseMsg;
		ArrayList<String>	theArgs = new ArrayList<String>();
		byte[]				resultBytes = null;
		String				fileHostIP = "";
		Integer				fileHostPort = listenPort;

		log.info("Sending put request to " + inHostIP + " for file '" + inSrcFileName + "' with" + (inUseCache ? "" : "out") + " cache");

		log.fine("Opening connection to " + inHostIP + ":" + listenPort);
		theCH.Open(inHostIP);
		theMH = new TAFSMessageHandler(theCH);

		if (inWithPayload)
		{
			// Read the file from storage and attach it to the message.
			TAFSFile	theFile = new TAFSFile(inSrcFileName, "");

			log.fine("Reading file from storage");

			theFile.SetCacheReads(false);
			theFile.SetCacheWrites(false);
			try
			{
				resultBytes = theFile.ReadFile();
			}
			catch(IOException eIO)
			{
				log.severe("Could not read file");
				theMH.Close();
				throw(eIO);
			}
		}
		else
			resultBytes = null;

		theArgs.add(inDstFileName);
		theArgs.add(String.valueOf(resultBytes != null ? resultBytes.length : 0));
		theArgs.add(inUseCache ? TAFSCommands.cache.getCmdStr() : TAFSCommands.nocache.getCmdStr());
		log.fine("Sending message to " + inHostIP);

		try
		{
			theMH.SendMessage(TAFSCommands.putfile.getCmdStr(), theArgs, resultBytes);
		}
		catch(IOException eIO)
		{
			log.severe("Could not send message to server");
			theMH.Close();
			throw(eIO);
		}

		log.fine("Reading response from " + inHostIP);
		try
		{
			responseMsg = theMH.ReadMessage();

			if (responseMsg.myArgs.size() > 0)
				fileHostIP = responseMsg.myArgs.get(0);
			if (responseMsg.myArgs.size() > 1)
				fileHostPort = Integer.parseInt(responseMsg.myArgs.get(1));
			log.info("Response from " + inHostIP + ": " + responseMsg.myMsg + " " + fileHostIP + ":" + fileHostPort);

			// TODO This could be a problematic area if the send fails... thread could hang on the other end waiting for a message
			theArgs.clear();
			theMH.SendMessage(TAFSCommands.ok.getCmdStr(), theArgs, null);

			// If useloc response is received, re-send request to the IP address in the response
			if (responseMsg.myMsg.equals(TAFSCommands.useloc.getCmdStr()))
			{
				if (fileHostIP.isEmpty())
					log.severe("Empty host IP received from " + inHostIP);
				else
					putFileToHost(inSrcFileName, inDstFileName, fileHostIP, fileHostPort, inUseCache, true);
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
		catch(IOException eIO)
		{
			log.severe("Could not read or send message: " + eIO.getMessage());
		}
		catch (ClassNotFoundException eCNF)
		{
			log.severe("Could not read or send message: " + eCNF.getMessage());
		}
		finally
		{
			log.fine("Closing connection to " + inHostIP);
			theMH.Close();
		}
	}
}
