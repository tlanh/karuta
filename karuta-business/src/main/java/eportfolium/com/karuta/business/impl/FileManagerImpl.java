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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.business.contract.FileManager;

@Service
@Transactional
public class FileManagerImpl implements FileManager {

	@Override
	public boolean rewriteFile(String sessionid, String backend, String user, UUID id, String lang, File file)
			throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();

		try {
			String url = backend + "/resources/resource/file/" + id.toString() + "?lang=" + lang;
			HttpPut put = new HttpPut(url);
			put.setHeader("Cookie", "JSESSIONID=" + sessionid); // So that the receiving servlet allow us

			/// Remove import language tag
			String filename = file.getName(); /// NOTE: Since it's used with zip import, specific code.
			int langindex = filename.lastIndexOf("_");
			filename = filename.substring(0, langindex) + filename.substring(langindex + 3);

			FileBody bin = new FileBody(file, ContentType.DEFAULT_BINARY, filename); // File from import

			/// Form info
			HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("uploadfile", bin).build();
			put.setEntity(reqEntity);

			httpclient.execute(put);
		} finally {
			httpclient.close();
		}

		return true;
	}

	public String[] findFiles(String directoryPath, String id) {
		// ========================================================================
		if (id == null)
			id = "";
		// Current folder
		File directory = new File(directoryPath);
		File[] subfiles = directory.listFiles();
		ArrayList<String> results = new ArrayList<String>();

		// Under this, try to find necessary files
		for (int i = 0; i < subfiles.length; i++) {
			File fileOrDir = subfiles[i];
			String name = fileOrDir.getName();

			if ("__MACOSX".equals(name)) /// Could be a better filtering
				continue;

			// One folder level under this one
			if (fileOrDir.isDirectory()) {
				File subdir = new File(directoryPath + name);
				File[] subsubfiles = subdir.listFiles();
				for (int j = 0; j < subsubfiles.length; ++j) {
					File subsubfile = subsubfiles[j];
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

	public String unzip(String zipFile, String destinationFolder) throws FileNotFoundException, IOException {
		String folder = "";
		File zipfile = new File(zipFile);
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipfile)));

		ZipEntry ze = null;
		try {
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
		} finally {
			zis.close();
		}
		return folder;
	}
}
