/**
 * @author Robert Abatecola
 *
 */

package tafscache;
import java.io.IOException;

// Abstract interface to enable use of various caching mechanisms such as memcached and redis.

public interface TAFSCacheInterface
{
	abstract void ConnectCache() throws IOException;
	abstract void DisconnectCache();
	abstract byte[] GetFileFromCache(String inFileName);
	abstract void PutFileInCache(String inFileName, byte[] inFileBytes);
}
