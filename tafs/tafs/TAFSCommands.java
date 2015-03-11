/**
 * 
 */
package tafs;

/**
 * @author robert
 *
 */
public enum TAFSCommands
{
	getfile("getfile"),
	putfile("putfile"),
	delfile("delfile"),
	rcvfile("rcvfile"),

	preprecvfile("preprecvfile"),
	prepsendfile("prepsendfile"),
	useloc("useloc"),

	addtocat("addtocat"),

	cache(""),
	nocache("nocache"),

	// Responses
	notok("notok"),
	ok("ok"),

	bye("bye");

	private final	String cmdStr;

	private TAFSCommands(String inCmd)
	{
		cmdStr = inCmd;
	}

    public String getCmdStr()
    {
        return this.cmdStr;
    }
}
