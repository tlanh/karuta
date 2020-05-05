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

import java.time.LocalDateTime;
import java.util.Date;

import eportfolium.com.karuta.consumer.repositories.LogTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.business.contract.LogManager;
import eportfolium.com.karuta.model.bean.LogTable;
import eportfolium.com.karuta.util.JavaTimeUtil;

@Service
@Transactional
public class LogManagerImpl implements LogManager {
	@Autowired
	private LogTableRepository logTableRepository;

	public boolean addLog(String url, String method, String headers, String inBody, String outBody, int code) {
		final Date now = JavaTimeUtil.toJavaDate(LocalDateTime.now(JavaTimeUtil.date_default_timezone));
		LogTable logTable = new LogTable(null, now, url, method, headers, inBody, outBody, code);
		boolean result = false;
		try {
			logTableRepository.save(logTable);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}

}
