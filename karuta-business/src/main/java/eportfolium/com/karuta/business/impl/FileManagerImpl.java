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
import java.util.HashMap;
import java.util.Map;
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

@Service
@Transactional
public class FileManagerImpl implements FileManager {
	@Autowired
	private ConfigurationManager configurationManager;

	@Override
	public Map<String, ByteArrayOutputStream> unzip(InputStream inputStream) {
		Map<String, ByteArrayOutputStream> entries = new HashMap<>();

		try (ZipInputStream zis = new ZipInputStream(inputStream)) {
			ZipEntry ze;

			while ((ze = zis.getNextEntry()) != null) {
				if (ze.isDirectory()) {
					continue;
				}

				ByteArrayOutputStream output = new ByteArrayOutputStream();

				byte[] buffer = new byte[4096];
				int n;

				while ((n = zis.read(buffer)) > 0) {
					output.write(buffer, 0, n);
				}

				output.close();
				entries.put(ze.getName(), output);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return entries;
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

			if (statusCode == 200) {
				InputStream objReturn = response.getEntity().getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(objReturn, "UTF8"));
				String line;
				
				while ((line = br.readLine()) != null) {
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
