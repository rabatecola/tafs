/**
 * @author Robert Abatecola
 *
 * An implementation of TAFSCacheInterface that uses spymemcached as the caching mechanism.
 */

package tafscache;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import tafs.TAFSGlobalConfig;
import tafs.TAFSOptions;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

public final class TAFSCache_SMCD implements TAFSCacheInterface
{
	// =============+=============+=============+=============+=============
	// Begin: Cache-related private members
	// Consider moving to separate class that implements a generalized interface.

	private final static String	className = TAFSCache_SMCD.class.getSimpleName();
	private final static Logger log = Logger.getLogger(className);

	// Cache-related member variables
	private static MemcachedClient	cache = null;
//	private static String			cacheServers = "";
	private static int				cacheConnectCount = 0;

//	public static void SetCacheServers(String inCacheServers)
//	{
//		cacheServers = inCacheServers;
//	}

	/**
	 * Call CreateCacheClient only one time in a program that uses this class.
	 * 
	 * @throws IOException
	 */
	public static void CreateCacheClient(String inCacheServers) throws IOException
	{
		if (cache == null)
			cache = new MemcachedClient(AddrUtil.getAddresses(inCacheServers));
	}

	public static void DestroyCacheClient()
	{
		if (cache != null)
			cache.shutdown();
	}

	/**
	 * 
	 * @throws IOException
	 */
	public static void ConnectCache() throws IOException
	{
		cacheConnectCount++;
	}

	public static void DisconnectCache()
	{
//		if ((cache != null) && (cacheConnectCount == 1))
//		{
//			cache.shutdown();
//			cache = null;
//		}

		if (cacheConnectCount > 0)
			cacheConnectCount--;
	}

	public static byte[] GetFileFromCache(String inFileName)
	{
		return cache != null ? (byte[])cache.get(inFileName) : null;
	}

	public static void PutFileInCache(String inFileName, byte[] inFileBytes)
	{
		if (cache == null)
			return;

		try
		{
			Future<Boolean>	myResult = cache.set(inFileName, TAFSGlobalConfig.getInteger(TAFSOptions.memcachedTTL), inFileBytes);
			try
			{
				if (myResult.get(10, TimeUnit.SECONDS))
					log.info("Data stored to cache.");
				else
					log.warning("Cache did not store data.");
			}
			catch(Exception e)
			{
				log.warning("Cache did not store data: " + e.getMessage());
			}
		}
		catch(IllegalStateException eIS)
		{
			log.severe("IllegalStateException: " + eIS.getMessage());
		}
	}
	// End: Cache-related private members
	// =============+=============+=============+=============+=============
}