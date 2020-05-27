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

import java.util.*;

import eportfolium.com.karuta.consumer.repositories.ConfigurationRepository;
import eportfolium.com.karuta.model.bean.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.business.contract.ConfigurationManager;

@Service
@Transactional
public class ConfigurationManagerImpl implements ConfigurationManager {

	@Autowired
	private ConfigurationRepository configurationRepository;

	protected Map<Integer, Map<String, Map<Object, Object>>> cache = new HashMap<>();
	protected Map<String, String> types = new HashMap<>();

	public void loadConfiguration() {
		Iterable<Configuration> result = configurationRepository.findAll();

		int lang = 0;

		for (Configuration conf : result) {
			types.put(conf.getName(), "normal");

			if (!cache.containsKey(lang)) {
				Map<String, Map<Object, Object>> map = new HashMap() {{
					put("global", new HashMap<>());
					put("group", new HashMap<>());
					put("shop", new HashMap<>());
				}};

				cache.put(lang, map);
			}

			cache.get(lang).get("global").put(conf.getName(), conf.getValue());
		}
	}

	public String get(String key, Integer id_lang) {
		if (cache.isEmpty()) {
			loadConfiguration();
		}

		if (cache.get(id_lang) == null)
			id_lang = 0;

		if (hasKey(key, id_lang))
			return (String) cache.get(id_lang).get("global").get(key);
		else
			return null;
	}

	public String get(String key) {
		return get(key, null);
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

		Map<String, String> results = new HashMap<>();
		String feature = null;

		for (String key : keys) {
			feature = get(key, langID);
			results.put(key, feature == null ? "" : feature);
		}

		return results;
	}

	public void set(String key, Map<Integer, String> values) {
		if (!StringUtils.isAlphanumeric(key)) {
			throw new RuntimeException(String.format("[%s] n'est pas une clé de configuration valide", key));
		}

		values.forEach((lang, value) -> {
			cache.get(lang).get("global").put(key, value);
		});
	}

	private boolean hasKey(String key, Integer langID) {
		if (!NumberUtils.isCreatable(key) && StringUtils.isBlank(key)) {
			return false;
		}

		return cache.get(langID).get("global") != null
				&& (cache.get(langID).get("global").get(key) != null
					|| cache.get(langID).get("global").containsKey(key));
	}

	public String getKarutaURL(Boolean ssl) {
		boolean ssl_enabled = Integer.parseInt(get("PS_SSL_ENABLED")) == 1;

		if (ssl == null) {
			String sslEverywhere = get("PS_SSL_ENABLED_EVERYWHERE");

			if (sslEverywhere != null) {
				ssl = (ssl_enabled && Integer.parseInt(sslEverywhere) == 1);
			}
		}

		if (ssl && ssl_enabled) {
			return "https://" + get("PS_SHOP_DOMAIN_SSL");
		} else {
			return "http://" + get("PS_SHOP_DOMAIN");
		}
	}

}
