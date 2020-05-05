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
	 * Load all configuration data
	 */
	public void loadConfiguration();

	/**
	 * Get a single configuration value (in one language only)
	 * @param string key Key wanted
	 * @param langID Language ID
	 *
	 * @return string Value
	 */

	String get(String key, Integer id_lang);

	/**
	 * Get a single configuration value (in one language only)
	 *
	 * @param string key Key wanted
	 * @return string Value
	 */
	String get(String key);

	Map<String, String> getMultiple(List<String> keys);

	/**
	 * Get several configuration values (in one language only)
	 * @param array   keys Keys wanted
	 * @param integer id_lang Language ID
	 *
	 * @return array Values
	 */
	public Map<String, String> getMultiple(List<String> keys, Integer langID);

	/**
	 * Update configuration key and value into database (automatically insert if key
	 * does not exist)
	 *
	 * Values are inserted/updated directly using SQL, because using (Configuration)
	 * ObjectModel may not insert values correctly (for example, HTML is escaped,
	 * when it should not be).
	 *
	 * @return boolean Update result
	 */
	boolean updateValue(String key, Map<Integer, String> values);

	/**
	 * Set TEMPORARY a single configuration value (in one language only)
	 * @param string key Key wanted
	 * @param mixed  values values is an array if the configuration is multilingual,
	 *               a single string else.
	 *
	 * @param int    id_shop_group
	 * @param int    id_shop
	 */
	void set(String key, Map<Integer, String> values);

	String getKarutaURL(Boolean ssl);
}
