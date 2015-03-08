/**
 * 
 */
package tafs;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author robert
 *
 */
public class TAFSGlobalConfig
{
	public static final	Integer	listenPort = 48611;	// 48611 is the 5000th prime number.  Oh joy.
	public static final Level	logLevel = Level.INFO;
	public static final Level	memcachedlogLevel = Level.FINEST;
	public static final String	cacheServers = "127.0.0.1:11211,127.0.0.1:11212";
	public static final String	catalogFile = "tafsCatalog.dat";

	public static void SetLoggingLevel(String inLoggerName)
	{
		Logger theLogger = Logger.getLogger(inLoggerName);

		theLogger.setLevel(logLevel);
		theLogger.getHandlers();
	    for (Handler handler : theLogger.getHandlers())
	    {
            // Set level in the console handler
	        if (handler instanceof ConsoleHandler)
	        {
	        	((ConsoleHandler)handler).setLevel(logLevel);
	            break;
	        }
	    }
	}
}
