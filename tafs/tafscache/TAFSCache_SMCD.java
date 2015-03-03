/**
 * @author Robert Abatecola
 *
 * An implementation of TAFSCacheInterface that uses spymemcached as the caching mechanism.
 */

package tafscache;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

public class TAFSCache_SMCD implements TAFSCacheInterface
{
	// =============+=============+=============+=============+=============
	// Begin: Cache-related private members
	// Consider moving to separate class that implements a generalized interface.

	// Cache-related member variables
	private MemcachedClient	cache = null;
	private String			cacheServers = "";
	private int				cacheConnectCount = 0;

	public TAFSCache_SMCD(String inCacheServers)
	{
		cacheServers = inCacheServers;
	}

	public void ConnectCache() throws IOException
	{
		if (cache == null)
			cache = new MemcachedClient(AddrUtil.getAddresses(cacheServers));
		cacheConnectCount++;
	}

	public void DisconnectCache()
	{
		if ((cache != null) && (cacheConnectCount == 1))
		{
			cache.shutdown();
			cache = null;
		}

		if (cacheConnectCount > 0)
			cacheConnectCount--;
	}

	public byte[] GetFileFromCache(String inFileName)
	{
		return (byte[])cache.get(inFileName);
	}

	public void PutFileInCache(String inFileName, byte[] inFileBytes)
	{
		try
		{
			Future<Boolean>	myResult = cache.set(inFileName, 3600, inFileBytes);
			try
			{
				if (!myResult.get(10, TimeUnit.SECONDS))
					System.out.println("Cache did not store data.");
			}
			catch(Exception e)
			{
				System.out.println("Cache did not store data: " + e.getMessage());
			}
		}
		catch(IllegalStateException eIS)
		{
			System.out.println("IllegalStateException: " + eIS.getMessage());
		}
	}
	// End: Cache-related private members
	// =============+=============+=============+=============+=============
}