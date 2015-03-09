/**
 * 
 */
package tafs;

/**
 * @author robert
 *
 */
//listenPort	48611
//logLevel	Level.ALL
//memcachedlogLevel	Level.FINEST
//# cacheServers: memcached server IP addresses
//cacheServers	"127.0.0.1:11211,127.0.0.1:11212"
//catalogFile	tafsCatalog.dat
//# ccIP: Cache Coordinator's IP address.
//ccIP	192.168.97.43
//# chIP: Cache Handler's ip address.  More than one can be specified.
//chIP	192.168.97.47
//chIP	192.168.97.48
public enum TAFSOptions
{
	listenPort("listenPort"),
	logLevel("logLevel"),
	memcachedlogLevel("memcachedlogLevel"),
	cacheServers("cacheServers"),
	catalogFile("catalogFile"),
	ccIP("ccIP"),
	chIP("chIP");

	private final	String optStr;

	private TAFSOptions(String inOpt)
	{
		optStr = inOpt;
	}

    public String getOptStr()
    {
        return this.optStr;
    }

	public static boolean contains(String test)
	{
		for (TAFSOptions option : TAFSOptions.values())
		{
			if (option.name().equals(test))
			{
				return true;
			}
		}
		
		return false;
	}
}
