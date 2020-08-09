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

package eportfolium.com.karuta.business.impl;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import eportfolium.com.karuta.business.contract.ConfigurationManager;
import eportfolium.com.karuta.document.ResourceDocument;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.business.contract.FileManager;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class FileManagerImpl implements FileManager {
	@Autowired
	private ConfigurationManager configurationManager;

	public String[] findFiles(String directoryPath, String id) {
		// ========================================================================
		if (id == null)
			id = "";
		// Current folder
		File directory = new File(directoryPath);
		File[] subfiles = directory.listFiles();
		ArrayList<String> results = new ArrayList<>();

		// Under this, try to find necessary files
		for (File fileOrDir : subfiles) {
			String name = fileOrDir.getName();

			if ("__MACOSX".equals(name)) /// Could be a better filtering
				continue;

			// One folder level under this one
			if (fileOrDir.isDirectory()) {
				File subdir = new File(directoryPath + name);
				File[] subsubfiles = subdir.listFiles();
				for (File subsubfile : subsubfiles) {
					String subname = subsubfile.getName();

					if (subname.endsWith(id) || "".equals(id)) {
						String completename = directoryPath + name + File.separatorChar + subname;
						results.add(completename);
					}
				}
			} else if (fileOrDir.isFile()) {
				String subname = fileOrDir.getName();
				if (name.contains(id) || id.equals("")) {
					String completename = directoryPath + subname;
					results.add(completename);
				}
			}
		}

		String[] result = new String[results.size()];
		results.toArray(result);

		return result;
	}

	@Override
	public String unzip(String zipFile, String destinationFolder) throws IOException {
		String folder = "";
		File zipfile = new File(zipFile);

		try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipfile)))) {
			ZipEntry ze = null;
			while ((ze = zis.getNextEntry()) != null) {
				folder = destinationFolder;
				File f = new File(folder, ze.getName());

				if (ze.isDirectory()) {
					f.mkdirs();
					continue;
				}

				f.getParentFile().mkdirs();
				OutputStream fos = new BufferedOutputStream(new FileOutputStream(f));
				try {
					try {
						final byte[] buf = new byte[8192];
						int bytesRead;
						while (-1 != (bytesRead = zis.read(buf)))
							fos.write(buf, 0, bytesRead);
					} finally {
						fos.close();
					}
				} catch (final IOException ioe) {
					f.delete();
					throw ioe;
				}
			}
		}

		return folder;
	}

	@Override
	public String updateResource(ResourceDocument resource,
			InputStream content,
								  String lang,
								  boolean thumbnail,
								  String contextPath) {
		String url = urlFor(resource, thumbnail, lang);

		HttpPut request = new HttpPut(url);
		request.addHeader("app", contextPath);
		try
		{
			request.setEntity(new InputStreamEntity(content));
			CloseableHttpClient client = createClient();
			CloseableHttpResponse response = client.execute(request);
				
			int statusCode = response.getStatusLine().getStatusCode();
			String retval = null;
			StringBuilder sb = new StringBuilder();
			if( statusCode == 200 )
			{
				sb = new StringBuilder();
				InputStream objReturn = response.getEntity().getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(objReturn, "UTF8"));
				String line;
				
				while((line=br.readLine())!= null){
					sb.append(line.trim());
				}
				retval = sb.toString();
			}
			
			return retval;
		}
		catch( IOException e )
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean fetchResource(ResourceDocument resource,
							  OutputStream output,
							  String lang,
							  boolean thumbnail,
							  String contextPath) {
		String url = urlFor(resource, thumbnail, lang);
		
		HttpGet request = new HttpGet(url);
		request.addHeader("app", contextPath);

		try (CloseableHttpClient client = createClient();
			 CloseableHttpResponse response = client.execute(request)) {
			FileCopyUtils.copy(response.getEntity().getContent(), output);

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public CloseableHttpClient createClient() {
		return HttpClients.createDefault();
	}

	private String urlFor(ResourceDocument resource, boolean thumbnail, String lang) {
		String url = configurationManager.get("fileserver") + "/" + resource.getFileid(lang);

		if (thumbnail)
			url += "/thumb";

		return url;
	}
}
