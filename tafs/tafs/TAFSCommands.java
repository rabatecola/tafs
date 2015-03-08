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

	notok("notok"),
	ok("ok");

	private final	String cmdStr;

	private TAFSCommands(String inCmd)
	{
		// TODO Auto-generated constructor stub
		cmdStr = inCmd;
	}

    public String getCmdStr()
    {
        return this.cmdStr;
    }
}
