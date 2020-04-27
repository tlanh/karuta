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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.business.contract.ConfigurationManager;
import eportfolium.com.karuta.consumer.contract.dao.ConfigurationDao;
import eportfolium.com.karuta.model.bean.Configuration;

@Service
@Transactional
public class ConfigurationManagerImpl implements ConfigurationManager {

	@Autowired
	private ConfigurationDao configurationDao;

	public void loadConfiguration() {
		configurationDao.loadConfiguration();
	}

	public String get(String key, Integer id_lang) {
		return configurationDao.get(key, id_lang);
	}

	public String get(String key) {
		return configurationDao.get(key);
	}

	public Map<String, String> getMultiple(List<String> keys) {
		return configurationDao.getMultiple(keys);
	}

	public Map<String, String> getMultiple(List<String> keys, Integer langID) {
		return configurationDao.getMultiple(keys, langID);
	}

	public boolean updateValue(String key, Map<Integer, String> values, boolean html) {
		return configurationDao.updateValue(key, values, html);
	}

	public void set(String key, Map<Integer, String> values) {
		configurationDao.set(key, values);
	}

	public Long getIdByName(String key) {
		return configurationDao.getIdByName(key);
	}

	public String getValueByName(String key) {
		return configurationDao.getValueByName(key);
	}

	public String getKarutaURL(Boolean ssl) {
		boolean ssl_enabled = BooleanUtils.toBoolean(Integer.parseInt(configurationDao.get("PS_SSL_ENABLED")));
		if (ssl == null) {
			String sslEverywhere = configurationDao.get("PS_SSL_ENABLED_EVERYWHERE");
			if (sslEverywhere != null) {
				ssl = (ssl_enabled && BooleanUtils.toBoolean(Integer.parseInt(sslEverywhere)));
			}
		}

		String base = ((ssl != null && ssl && ssl_enabled) ? "https://" + configurationDao.getDomainSsl()
				: "http://" + configurationDao.getDomain());
		return base;
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public void transferConfigurationTable(Connection con) {

		ResultSet res = configurationDao.findAll("configuration", con);
		Configuration cf = null;
		try {
			while (res.next()) {
				cf = new Configuration();
				cf.setId(res.getLong("id_configuration"));
				cf.setName(res.getString("name"));
				cf.setValue(res.getString("value"));
				cf.setModifDate(res.getDate("modifDate"));
				cf = configurationDao.merge(cf);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public void removeConfigurations(Connection con) {
		configurationDao.removeAll();
	}

}
