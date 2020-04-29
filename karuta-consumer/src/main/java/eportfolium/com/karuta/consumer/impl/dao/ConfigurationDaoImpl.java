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

package eportfolium.com.karuta.consumer.impl.dao;
// Generated 21 sept. 2017 22:49:01 by Hibernate Tools 5.2.3.Final

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.ConfigurationDao;
import eportfolium.com.karuta.model.bean.Configuration;

/**
 * Home object implementation for domain model class Configuration.
 * 
 * @author Hibernate Tools
 */
@Repository
public class ConfigurationDaoImpl extends AbstractDaoImpl<Configuration> implements ConfigurationDao {

	/** @var array Configuration cache */
	protected Map<String, Map<Integer, Map<String, Map<Object, Object>>>> _cache = new HashMap<>();

	protected Map<String, String> types = new HashMap<>();

	public ConfigurationDaoImpl() {
		super();
		setCls(Configuration.class);
	}

	/**
	 * Load all configuration data
	 */
	public void loadConfiguration() {
		_cache.put(this.getClass().getSimpleName(), new HashMap<Integer, Map<String, Map<Object, Object>>>());
		List<Configuration> result = findAll();
		Map<String, Map<Object, Object>> langContent = null;
		int lang = 0;
		for (Configuration row : result) {
			types.put(row.getName(), "normal");
			if (!_cache.get(this.getClass().getSimpleName()).containsKey(lang)) {
				langContent = new HashMap<>();
				langContent.put("global", new HashMap<>());
				langContent.put("group", new HashMap<>());
				langContent.put("shop", new HashMap<>());
				_cache.get(this.getClass().getSimpleName()).put(lang, langContent);
			}

			_cache.get(this.getClass().getSimpleName()).get(lang).get("global").put(row.getName(), row.getValue());
		}
	}

	/**
	 * Get a single configuration value (in one language only)
	 * 
	 * @param key Key wanted
	 * @param id_lang Language ID
	 *
	 * @return string Value
	 */

	public String get(String key, Integer id_lang) {
		if (_cache.get(this.getClass().getSimpleName()) == null) {
			loadConfiguration();
			// If conf if not initialized, try manual query
			if (_cache.get(this.getClass().getSimpleName()) == null) {
				return getValueByName(key);
			}
		}

		if (_cache.get(this.getClass().getSimpleName()).get(id_lang) == null)
			id_lang = 0;

		if (hasKey(key, id_lang))
			return (String) _cache.get(this.getClass().getSimpleName()).get(id_lang).get("global").get(key);
		return null;

	}

	public String get(String key) {
		return get(key, null);
	}

	public Map<String, String> getMultiple(List<String> keys) {
		return getMultiple(keys, null);
	}

	/**
	 * Get several configuration values (in one language only)
	 *
	 * @param keys Keys wanted
	 * @param langID Language ID
	 * @return array Values
	 */
	public Map<String, String> getMultiple(List<String> keys, Integer langID) {
		Validate.noNullElements(keys);

		Map<String, String> results = new HashMap<String, String>();
		String feature = null;
		for (String key : keys) {
			feature = get(key, langID);
			results.put(key, feature == null ? "" : feature);
		}
		return results;
	}

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
	 * @param key Key
	 * @param values values is an array if the configuration is
	 *                multilingual, a single string else.
	 * @param html Specify if html is authorized in value
	 * @return boolean Update result
	 */
	public boolean updateValue(String key, Map<Integer, String> values, boolean html) {
		if (!StringUtils.isAlphanumeric(key)) {
			throw new RuntimeException(String.format("[%s] n'est pas une clé de configuration valide", key));
		}

		if (values == null) {
			values = new HashMap<>(0);
		}

		Entry<Integer, String> entry;
		if (html) {
			for (java.util.Iterator<Entry<Integer, String>> it = values.entrySet().iterator(); it.hasNext();) {
				entry = it.next();
				values.put(entry.getKey(), entry.getValue());
			}
		}

		boolean result = true;
		Integer lang;
		String value;
		for (Iterator<Entry<Integer, String>> it = values.entrySet().iterator(); it.hasNext();) {
			entry = it.next();
			lang = entry.getKey();
			value = entry.getValue();

			Object stored_value = get(key, lang);
			// if there isn't a stored_value, we must insert value
			if ((!NumberUtils.isCreatable(value) && value.equals(stored_value))
					|| (NumberUtils.isCreatable(value) && value.equals(stored_value) && hasKey(key, lang))) {
				continue;
			}

			// If key already exists, update value
			if (hasKey(key, lang)) {
				if (lang == null || lang == 0) {
					// Update config not linked to lang
					String sql = "SELECT c FROM Configuration c";
					sql += " WHERE c.name = :key";
					TypedQuery<Configuration> q = em.createQuery(sql, Configuration.class);
					q.setParameter("key", key);
					List<Configuration> row = q.setMaxResults(1).getResultList();
					if (!row.isEmpty()) {
						row.get(0).setValue(value);
						try {
							merge(row.get(0));
							result &= true;
						} catch (Exception e) {
							result &= false;
						}
					}
				}
			}
			// If key does not exists, create it
			else {
				Long configID = getIdByName(key);
				if (configID == null | configID == 0L) {
					Configuration c = new Configuration();
					c.setName(key);
					c.setValue(!(lang == null || lang == 0) ? null : value);
					try {
						persist(c);
						configID = c.getId();
						result &= true;
					} catch (Exception e) {
						result &= false;
					}
				}
			}
		}

		set(key, values);
		return result;

	}

	/**
	 * Set TEMPORARY a single configuration value (in one language only)
	 *
	 * @param key Key wanted
	 * @param values values is an array if the configuration is multilingual,
	 *               a single string else.
	 */
	public void set(String key, Map<Integer, String> values) {
		if (!StringUtils.isAlphanumeric(key)) {
			throw new RuntimeException(String.format("[%s] n'est pas une clé de configuration valide", key));
		}

		Entry<Integer, String> entry;
		Integer lang;
		String value;
		for (Iterator<Entry<Integer, String>> it = values.entrySet().iterator(); it.hasNext();) {
			entry = it.next();
			lang = entry.getKey();
			value = entry.getValue();
			_cache.get(this.getClass().getSimpleName()).get(lang).get("global").put(String.valueOf(key), value);
		}
	}

	/**
	 * Return ID a configuration key
	 *
	 * @param key
	 * @return long Configuration key ID
	 */
	public Long getIdByName(String key) {

		Long result = null;
		String sql = "SELECT id FROM Configuration c";
		sql += " WHERE c.name = :key";
		TypedQuery<Long> q = em.createQuery(sql, Long.class);
		q.setParameter("key", key);
		try {
			result = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return result;
	}

	public String getValueByName(String key) {
		String sql = "SELECT conf.value FROM Configuration conf";
		sql += " WHERE conf.name = :key";

		TypedQuery<String> q = em.createQuery(sql, String.class);
		q.setParameter("key", key);

		try {
			String obj = q.getSingleResult();
			return obj;
		} catch (NoResultException e) {
			// TODO: More explicit message or controler ; might raise otherwise when used
			return null;
		} catch (NonUniqueResultException e) {
			throw new IllegalStateException("Duplicate feature found with name = " + key + ".", e);
		}
	}

	private boolean hasKey(String key, Integer langID) {

		if (!NumberUtils.isCreatable(key) && StringUtils.isBlank(key)) {
			return false;
		}

		langID = (int) langID;

		try {

			return _cache.get(this.getClass().getSimpleName()).get(langID).get("global") != null
					&& (_cache.get(this.getClass().getSimpleName()).get(langID).get("global").get(key) != null
							|| _cache.get(this.getClass().getSimpleName()).get(langID).get("global").containsKey(key));
		} catch (Exception e) {
		}
		return false;
	}

	@Override
	public String getDomain() {
		String domainName = get("PS_SHOP_DOMAIN");
		return StringUtils.defaultIfEmpty(domainName, "");
	}

	@Override
	public String getDomainSsl() {
		String domainName = get("PS_SHOP_DOMAIN_SSL");
		return StringUtils.defaultIfEmpty(domainName, "");
	}

}
