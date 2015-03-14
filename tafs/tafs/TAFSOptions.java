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
	memcachedTTL("memcachedTTL"),
	cacheServers("cacheServers"),
	catalogFile("catalogFile"),
	ccIP("ccIP"),
	chIP("chIP"),
	chDataDir("chDataDir"),
	catSaveInterval("catSaveInterval"),
	ccThreadLimit("ccThreadLimit"),
	chThreadLimit("chThreadLimit"),
	ccLogLevel("ccLogLevel"),
	chLogLevel("chLogLevel"),
	ccBindAddr("ccBindAddr"),
	chBindAddr("chBindAddr"),
	clientLogLevel("clientLogLevel"),
	clientThreadLimit("clientThreadLimit"),
	clientFileCount("clientFileCount"),
	clientLoopCount("clientLoopCount"),
	clientGenerateTestFiles("clientGenerateTestFiles"),
	clientRunReadTests("clientRunReadTests"),
	clientRunWriteTests("clientRunWriteTests");

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
