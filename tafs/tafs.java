import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.logging.Logger;

import tafsCacheCoordinator.TAFSCacheCoordinator;
import tafsCacheHandler.TAFSCacheHandler;
import tafs.TAFSGlobalConfig;

/**
 * Main entry point to facilitate easy launching of components.
 * Launches either cache coordinator or cache handler depending on the name of the
 * jar file.
 * 
 * The following will launch the cache coordinator:
 * java -cp [/path/to/]tafscc tafs
 * 
 * This will launch the cache handler:
 * java -cp [/path/to/]tafsch tafs
 */

/**
 * @author Robert Abatecola
 *
 */
public class tafs
{
	private final static String	className = tafs.class.getSimpleName();
	private final static Logger log = Logger.getLogger(className);

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException
	{
		CodeSource	codeSource = tafs.class.getProtectionDomain().getCodeSource();
		String		path = codeSource.getLocation().getPath();
		File		jarFile = new File(path);
		String		jarDir = jarFile.getPath();
		String		jarAbsPath = jarFile.getAbsolutePath();
//		String		jarFileName = URLDecoder.decode(jarFile.getName(), "UTF-8");
		String		jarFileName = jarFile.getName();
		String		decodedPath = URLDecoder.decode(path, "UTF-8");

		TAFSGlobalConfig.LoadConfigFromFile();

		log.info("Starting " + decodedPath);
		log.fine("jarDir = " + URLDecoder.decode(jarDir, "UTF-8"));
		log.fine("jarFileName = " + jarFileName);
		log.fine("jarAbsPath = " + URLDecoder.decode(jarAbsPath, "UTF-8"));

		if (jarFileName.equals("tafscc"))
			new TAFSCacheCoordinator();

		if (jarFileName.equals("tafsch"))
			new TAFSCacheHandler();
	}
}
