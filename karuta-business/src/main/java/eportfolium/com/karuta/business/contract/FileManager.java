package eportfolium.com.karuta.business.contract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface FileManager {

	boolean sendFile(String sessionid, String backend, String user, String uuid, String lang, File file)
			throws Exception;

	boolean rewriteFile(String sessionid, String backend, String user, String uuid, String lang, File file)
			throws Exception;

	boolean updateResource(String sessionid, String backend, String uuid, String lang, String json) throws Exception;

	String[] findFiles(String directoryPath, String id);

	String unzip(String zipFile, String destinationFolder) throws FileNotFoundException, IOException;

}
