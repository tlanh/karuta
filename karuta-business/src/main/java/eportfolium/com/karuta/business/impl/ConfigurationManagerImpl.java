package eportfolium.com.karuta.business.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eportfolium.com.karuta.business.contract.ConfigurationManager;
import eportfolium.com.karuta.consumer.contract.dao.ConfigurationDao;

@Service
public class ConfigurationManagerImpl implements ConfigurationManager {

	@Autowired
	private ConfigurationDao configurationDao;

	public void loadConfiguration() {
		configurationDao.loadConfiguration();
	}

	public String get(String key, Integer id_lang, Integer id_shop_group, Object id_shop) {
		return configurationDao.get(key, id_lang, id_shop_group, id_shop);
	}

	public String get(String key) {
		return configurationDao.get(key);
	}

	public Map<String, String> getMultiple(List<String> keys) {
		return configurationDao.getMultiple(keys);
	}

	public Map<String, String> getMultiple(List<String> keys, Integer langID, Integer shopGroupID, Integer shopID) {
		return configurationDao.getMultiple(keys, langID, shopGroupID, shopID);
	}

	public boolean updateValue(String key, Map<Integer, String> values, boolean html, Integer shopGroupID,
			Integer shopID) {
		return configurationDao.updateValue(key, values, html, shopGroupID, shopID);
	}

	public void set(String key, Map<Integer, String> values, Integer id_shop_group, Integer id_shop) {
		configurationDao.set(key, values, id_shop_group, id_shop);
	}

	public Integer getIdByName(String key) {
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

}
