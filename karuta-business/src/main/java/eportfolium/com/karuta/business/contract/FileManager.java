/* =======================================================
	Copyright 2020 - ePortfolium - Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
   ======================================================= */

package eportfolium.com.karuta.business.contract;

import eportfolium.com.karuta.document.ResourceDocument;
import eportfolium.com.karuta.model.bean.Resource;

import org.apache.http.client.HttpClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.UUID;

public interface FileManager {

	String[] findFiles(String directoryPath, String id);

	String unzip(String zipFile, String destinationFolder) throws IOException;

	String updateResource(ResourceDocument resource,
						   InputStream content,
						   String lang,
						   boolean thumbnail,
						   String contextPath);

	boolean fetchResource(ResourceDocument resource,
						  OutputStream output,
						  String lang,
						  boolean thumbnail,
						  String contextPath);

	HttpClient createClient();
}
