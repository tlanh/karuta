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

package eportfolium.com.karuta.webapp.eventbus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import eportfolium.com.karuta.business.contract.LogManager;

public class HandlerLogging implements KEventHandler {
	HttpServletRequest httpServletRequest;
	HttpSession session;
	int userId;
	int groupId;
	LogManager logManager;

	public HandlerLogging(HttpServletRequest request, LogManager logManager) {
		httpServletRequest = request;
		this.logManager = logManager;

		this.session = request.getSession(true);
		Integer val = (Integer) session.getAttribute("uid");
		if (val != null)
			this.userId = val;
		val = (Integer) session.getAttribute("gid");
		if (val != null)
			this.groupId = val;
	}

	@Override
	public boolean processEvent(KEvent event) {
		try {
			switch (event.eventType) {
			case LOGIN:
				String httpHeaders = "";

				@SuppressWarnings("unchecked")
				final List<String> headerNames = Collections.<String>list(httpServletRequest.getHeaderNames());
				for (final String header : headerNames) {
					httpHeaders += header + ": " + httpServletRequest.getHeader(header) + "\n";
				}

				String url = httpServletRequest.getRequestURL().toString();
				if (httpServletRequest.getQueryString() != null)
					url += "?" + httpServletRequest.getQueryString().toString();
				logManager.addLog(url, httpServletRequest.getMethod().toString(), httpHeaders, event.inputData,
						event.doc.toString(), event.status);

				//// TODO Devrait aussi écrire une partie dans les fichiers
				System.out.println("LOGIN EVENT");

				break;

			default:
				break;
			}
		} catch (Exception ex) {
		}

		return true;
	}

	Document parseString(String data)
			throws UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document doc = documentBuilder.parse(new ByteArrayInputStream(data.getBytes("UTF-8")));
		doc.setXmlStandalone(true);

		return doc;
	}

}
