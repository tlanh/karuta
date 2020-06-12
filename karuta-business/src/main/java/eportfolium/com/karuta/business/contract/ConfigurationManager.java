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

import java.util.List;
import java.util.Map;

public interface ConfigurationManager {

	/**
	 * Load all configuration data in memory.
	 */
	void loadConfiguration();

	/**
	 * Get a single configuration value.
	 *
	 * @param key - Key wanted.
	 */
	String get(String key);

	/**
	 * Get several configuration values.
	 *
	 * @param keys - Keys wanted.
	 */
	Map<String, String> getMultiple(List<String> keys);


	/**
	 * Get the full URL to the website, honoring the SSL settings.
	 */
	String getKarutaURL();

	/**
	 * Clear the inner cache.
	 */
	void clear();
}
