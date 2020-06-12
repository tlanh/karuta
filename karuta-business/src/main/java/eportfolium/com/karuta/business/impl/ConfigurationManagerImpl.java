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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.business.contract.ConfigurationManager;

@Service
@Transactional
public class ConfigurationManagerImpl implements ConfigurationManager {

	@Autowired
	private ConfigurationRepository configurationRepository;

	protected Map<String, String> cache = new HashMap<>();

	@Override
	public void loadConfiguration() {
		Iterable<Configuration> result = configurationRepository.findAll();

		for (Configuration conf : result) {
			cache.put(conf.getName(), conf.getValue());
		}
	}

	@Override
	public String get(String key) {
		if (cache.isEmpty()) {
			loadConfiguration();
		}

		return cache.get(key);
	}

	/**
	 * Get several configuration values (in one language only)
	 *
	 * @param keys Keys wanted
	 */
	@Override
	public Map<String, String> getMultiple(List<String> keys) {
		Map<String, String> results = new HashMap<>();

		for (String key : keys) {
			String feature = get(key);
			results.put(key, feature == null ? "" : feature);
		}

		return results;
	}

	@Override
	public String getKarutaURL() {
		boolean sslEnabled = Integer.parseInt(get("ssl_enabled")) == 1;

		if (sslEnabled) {
			return "https://" + get("domain");
		} else {
			return "http://" + get("domain");
		}
	}

	@Override
	public void clear() {
		this.cache = new HashMap<>();
	}
}
