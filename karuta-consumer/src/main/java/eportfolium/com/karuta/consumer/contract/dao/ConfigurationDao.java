package eportfolium.com.karuta.consumer.contract.dao;
// Generated 21 sept. 2017 22:49:01 by Hibernate Tools 5.2.3.Final

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import eportfolium.com.karuta.model.bean.Configuration;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

/**
 * Home object for domain model class PsConfiguration.
 * 
 * @see dulocaldansnosassiettes.business.domain.prestashop.dataobject.PsConfiguration
 * @author Hibernate Tools
 */
public interface ConfigurationDao {

	void persist(Configuration transientInstance);

	void remove(Configuration persistentInstance);

	Configuration merge(Configuration detachedInstance);

	Configuration findById(Serializable id) throws DoesNotExistException;

	/**
	 * Load all configuration data
	 */
	public void loadConfiguration();

	/**
	 * Get a single configuration value (in one language only)
	 * 
	 * @param string  key Key wanted
	 * @param id_lang Language ID
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
	 *
	 * @param array   keys Keys wanted
	 * @param integer id_lang Language ID
	 * @return array Values
	 */
	public Map<String, String> getMultiple(List<String> keys, Integer id_lang);

	/**
	 * Update configuration key and value into database (automatically insert if key
	 * does not exist)
	 *
	 * Values are inserted/updated directly using SQL, because using (Configuration)
	 * ObjectModel may not insert values correctly (for example, HTML is escaped,
	 * when it should not be).
	 * 
	 * @TODO Fix saving HTML values in Configuration model
	 *
	 * @param string  key Key
	 * @param mixed   values values is an array if the configuration is
	 *                multilingual, a single string else.
	 * @param boolean html Specify if html is authorized in value
	 * @return boolean Update result
	 */
	boolean updateValue(String key, Map<Integer, String> values, boolean html);

	/**
	 * Set TEMPORARY a single configuration value (in one language only)
	 *
	 * @param string key Key wanted
	 * @param mixed  values values is an array if the configuration is multilingual,
	 *               a single string else.
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

	String getDomain();

	String getDomainSsl();

	ResultSet findAll(String table, Connection con);

	List<Configuration> findAll();

	void removeAll();

}
