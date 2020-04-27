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

package eportfolium.com.karuta.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eportfolium.com.karuta.model.exception.UtilRuntimeException;


public class ResourceUtil {
	static private final Logger LOGGER = LoggerFactory.getLogger(ResourceUtil.class);
	static private ResourceUtil myClass = null;

	private ResourceUtil() {
	}

	static public Properties getAsProperties(String resourceName) throws UtilRuntimeException {
		return getAsProperties(resourceName, getContextClassLoader());
	}

	static public Properties getAsProperties(String resourceName, ClassLoader classLoader) throws UtilRuntimeException {

		Properties p;

		if (resourceName == null) {
			throw new IllegalArgumentException("resourceName is null");
		}
		if (myClass == null) {
			myClass = new ResourceUtil();
		}

		InputStream resourceStream = null;
		try {
			// Try local

			resourceStream = classLoader.getResourceAsStream("/" + resourceName);

			// If not found, try classpath

			if (resourceStream == null) {
				resourceStream = classLoader.getResourceAsStream(resourceName);
			}

			// If not found, then get out

			if (resourceStream == null) {
				LOGGER.error("Could not load properties from resource \"" + resourceName + "\".  Check the classpath.");
				System.err.println(
						"Could not load properties from resource \"" + resourceName + "\".  Check the classpath.");
				throw new UtilRuntimeException(
						"Could not find resource \"" + resourceName + "\".  Check the classpath.");
			}

			// Load the properties!

			p = new Properties();
			p.load(resourceStream);
		}
		catch (UtilRuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new UtilRuntimeException(
					"Could not load properties from resource \"" + resourceName + "\".  Check the classpath.", e);
		}
		finally {
			if (resourceStream != null) {
				try {
					resourceStream.close();
				}
				catch (Throwable ignore) {
				}
			}
		}

		return p;

	}

	static public List<String> getResourceFiles(String path) throws IOException {
		List<String> filenames = new ArrayList<>();

		try (InputStream in = getResourceAsStream(path);
				BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String resource;

			while ((resource = br.readLine()) != null) {
				filenames.add(resource);
			}
		}

		return filenames;
	}

	static public InputStream getResourceAsStream(String resource) {
		if (myClass == null) {
			myClass = new ResourceUtil();
		}
		final InputStream in = getContextClassLoader().getResourceAsStream(resource);

		return in == null ? myClass.getClass().getResourceAsStream(resource) : in;
	}

	static private ClassLoader getContextClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	static public String getExtension(String path) {
		String[] strings = path.split("\\.");
		return strings[strings.length - 1];
	}
}
