package eportfolium.com.karuta.business.contract;

import java.sql.Connection;
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
	 * @TODO Fix saving HTML values in Configuration model
	 * @param string  key Key
	 * @param mixed   values values is an array if the configuration is
	 *                multilingual, a single string else.
	 *
	 * @param boolean html Specify if html is authorized in value
	 * @param int     shopGroupID
	 * @param int     shopID
	 * @return boolean Update result
	 */
	boolean updateValue(String key, Map<Integer, String> values, boolean html);

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

	/**
	 * Return ID a configuration key
	 *
	 * @param string key
	 * @return long Configuration key ID
	 */
	Long getIdByName(String key);

	/**
	 * Return Value a configuration value
	 *
	 * @param string key
	 * @return a string, configuration value
	 */
	String getValueByName(String key);

	String getKarutaURL(Boolean ssl);


	/**
	 * Used to delete all configuration data.
	 * 
	 * Permet de supprimer toutes les données de configuration.
	 * 
	 * @param con
	 */
	void removeConfigurations(Connection con);

	
	/*****************************/

	void transferConfigurationTable(Connection con);
}
