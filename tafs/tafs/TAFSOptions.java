/**
 * 
 */
package tafs;

/**
 * @author robert
 *
 */
public enum TAFSOptions
{
	ccListenPort("ccListenPort"),
	chListenPort("chListenPort"),
	logLevel("logLevel"),
	memcachedlogLevel("memcachedlogLevel"),
	cacheServers("cacheServers"),
	catalogFile("catalogFile"),
	ccIP("ccIP"),
	chIP("chIP"),
	chDataDir("chDataDir");

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
