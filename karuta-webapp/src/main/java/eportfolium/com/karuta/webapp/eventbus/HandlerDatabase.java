/* =======================================================
	Copyright 2014 - ePortfolium - Licensed under the
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.util.MimeTypeUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;

public class HandlerDatabase implements KEventHandler {
	HttpServletRequest httpServletRequest;
	HttpSession session;
	long userId;
	long groupId;
	SecurityManager securityManager;
	NodeManager nodeManager;

	public HandlerDatabase(HttpServletRequest request, SecurityManager securityManager, NodeManager nodeManager) {
		httpServletRequest = request;
		this.securityManager = securityManager;
		this.nodeManager = nodeManager;

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
				String[] resultCredential = securityManager.postCredentialFromXml(event.inputData, "", null);
				if (resultCredential != null) {
					String login1 = resultCredential[0];
					String tokenID = resultCredential[1];

					if (tokenID == null)
						throw new RestWebApplicationException(Status.FORBIDDEN,
								"invalid credential or invalid group member");
					else if (tokenID.length() == 0)
						throw new RestWebApplicationException(Status.FORBIDDEN,
								"invalid credential or invalid group member");

					this.userId = Integer.parseInt(resultCredential[3]);
					session.setAttribute("user", login1);
					session.setAttribute("uid", this.userId);

					event.status = 200;
					event.doc = parseString(resultCredential[2]);

					System.out.println("LOGIN event");
				} else
					throw new RestWebApplicationException(Status.FORBIDDEN,
							"invalid credential or invalid group member");

				break;

			case NODE:
				switch (event.requestType) {
				case POST:
					String returnValue = nodeManager.addNode(MimeTypeUtils.TEXT_XML, event.uuid, event.inputData,
							this.userId, this.groupId, true).toString();
					if ("faux".equals(returnValue)) {
						event.message = "Vous n'avez pas les droits d'acces";
						event.status = 403;
					} else {
						event.status = 200;
						event.doc = parseString(returnValue);
					}

					System.out.println("POST NODE event");

					break;

				default:
					break;
				}
				break;

			default:
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
