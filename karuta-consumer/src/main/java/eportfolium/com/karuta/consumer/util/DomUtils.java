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

package eportfolium.com.karuta.consumer.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;

//  ==================================================================================
//					Affichage DOM
//  ==================================================================================

public class DomUtils {
	final static Logger logger = LoggerFactory.getLogger(DomUtils.class);

//  ==================================================================================
//  ============================= Manage DOM =============================================
//  ==================================================================================

//  ---------------------------------------------------
	public static void createAndSetAttribute(Node node, String tagAttributeName, String tagAttributeValue)
			throws Exception {
//  ---------------------------------------------------
		((Document) node).createAttribute(tagAttributeName);
		((Document) node).getDocumentElement().setAttribute(tagAttributeName, tagAttributeValue);

	}

//  ---------------------------------------------------
	public static void SetAttribute(Node node, String tagAttributeName, String tagAttributeValue) throws Exception {
//  ---------------------------------------------------
		((Document) node).getDocumentElement().setAttribute(tagAttributeName, tagAttributeValue);

	}

//  ---------------------------------------------------
	public static String getRootuuid(Node node) throws Exception {
//  ---------------------------------------------------
		String result = null;
		NodeList liste = ((Document) node).getElementsByTagName("asmRoot");
		if (liste.getLength() != 0) {
			Element elt = (Element) liste.item(0);
			result = elt.getAttribute("uuid");
		}
		return result;
	}

//  ==================================================================================
//  ============================= Transformation  ===========================================
//  ==================================================================================

//  =======================================
	public static void processXSLT(Source xml, String xsltName, Result result, StringBuffer outTrace, boolean trace)
			throws Exception {
//  =======================================
		outTrace.append("<br>processXSLT... " + xsltName);
		TransformerFactory tFactory = TransformerFactory.newInstance();
		StreamSource stylesource = new StreamSource(xsltName);
		Transformer transformer = tFactory.newTransformer(stylesource);

		try {
			transformer.transform(xml, result);
		} catch (TransformerConfigurationException tce) {
			throw new TransformerException(tce.getMessageAndLocation());
		} catch (TransformerException te) {
			throw new TransformerException(te.getMessageAndLocation());
		}
		if (trace)
			outTrace.append(" ... ok");
	}

//  =======================================
	public static void processXSLT(Document xml, String xsltName, Document result, StringBuffer outTrace, boolean trace)
			throws Exception {
//  =======================================
		outTrace.append("a.");
		processXSLT(new DOMSource(xml), xsltName, new DOMResult(result), outTrace, trace);
		outTrace.append("b.");
	}

//  =======================================
	public static void processXSLT(Document xml, String xsltName, Writer result, StringBuffer outTrace, boolean trace)
			throws Exception {
//  =======================================
		outTrace.append("c.");
		processXSLT(new DOMSource(xml), xsltName, new StreamResult(result), outTrace, trace);
		outTrace.append("d.");
	}

//  =======================================
	public static String processXSLTfile2String(Document xml, String xslFile, String param[], String paramVal[],
			StringBuffer outTrace) throws Exception {
//  =======================================
		logger.debug("<br>-->processXSLTfile2String-" + xslFile);
		outTrace.append("<br>-->processXSLTfile2String-" + xslFile);
		Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new File(xslFile)));
		outTrace.append(".1");
		StreamResult result = new StreamResult(new StringWriter());
		outTrace.append(".2");
		DOMSource source = new DOMSource(xml);
		outTrace.append(".3");
		for (int i = 0; i < param.length; i++) {
			outTrace.append("<br>setParemater - " + param[i] + ":" + paramVal[i] + "...");
			logger.debug("<br>setParemater - " + param[i] + ":" + paramVal[i] + "...");
			transformer.setParameter(param[i], paramVal[i]);
			outTrace.append("ok");
			logger.debug("ok");
		}
		outTrace.append(".4");
		transformer.transform(source, result);
		outTrace.append("<br><--processXSLTfile2String-" + xslFile);
		logger.debug("<br><--processXSLTfile2String-" + xslFile);
		return result.getWriter().toString();
	}

//  ==================================================================================
//  ================================ Utilitaires  ===========================================
//  ==================================================================================

//  =======================================
	public static Document xmlString2Document(String xmlString, StringBuffer outTrace) throws Exception {
//  =======================================
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document xmldoc = null;
		DocumentBuilder builder = factory.newDocumentBuilder();
		xmldoc = builder.parse(new InputSource(new StringReader(xmlString)));
		return xmldoc;
	}

//  =======================================
	public static String file2String(String fileName, StringBuffer outTrace) throws Exception {
//  =======================================
		String result = "";
		try {
			FileInputStream fichierSrce = new FileInputStream(fileName);
			BufferedReader readerSrce = new BufferedReader(new InputStreamReader(fichierSrce, "UTF-8"));
			String line;
			while ((line = readerSrce.readLine()) != null) {
				result += line;
			}
			readerSrce.close();
			fichierSrce.close();
		} catch (IOException ioe) {
			outTrace.append("<br/>file2String-- Error: " + ioe);
		}
		return result;
	}

//  ---------------------------------------------------
	public static void saveString(String str, String fileName) throws Exception {
//  ---------------------------------------------------
		Writer fwriter = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
		fwriter.write(str);
		fwriter.close();
	}

	public static String getInnerXml(Node node) {
		DOMImplementationLS lsImpl = (DOMImplementationLS) node.getOwnerDocument().getImplementation().getFeature("LS",
				"3.0");
		LSSerializer lsSerializer = lsImpl.createLSSerializer();
		NodeList childNodes = node.getChildNodes();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < childNodes.getLength(); i++) {
			sb.append(lsSerializer.writeToString(childNodes.item(i)));
		}
		if (sb.toString().startsWith("<![CDATA["))
			sb.append("]]>");
		return DomUtils.filtrerInnerXml(sb.toString());
	}

	public static String getXmlAttributeOutput(String attributeName, String attributeValue) {
		if (attributeValue == null)
			attributeValue = "";
		return attributeName + "=\"" + attributeValue + "\"";
	}

	public static String getXmlAttributeOutputInt(String attributeName, Integer attributeValue) {
		if (attributeValue == null)
			attributeValue = 0;
		return attributeName + "=\"" + attributeValue + "\"";
	}

	public static String getXmlElementOutput(String tagName, String value) {
		if (value == null)
			return "<" + tagName + "/>";
		else
			return "<" + tagName + ">" + value + "</" + tagName + ">";
	}

	public static String getNodeAttributesString(Node node) {
		String ret = "";
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Attr attribute = (Attr) attributes.item(i);
			ret += attribute.getName().trim() + "=\"" + StringEscapeUtils.escapeXml11(attribute.getValue().trim())
					+ "\" ";
		}
		return ret;
	}

	public static String filterXmlResource(String xml) throws UnsupportedEncodingException {
		if (xml.startsWith("<?xml")) {
			int posEndXml = xml.indexOf("?>");
			xml = xml.substring(posEndXml);

			return DomUtils.cleanXMLData(xml);

		} else
			return DomUtils.cleanXMLData(xml);

	}

	public static String filtrerInnerXml(String chaine) {
		String motif = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>";
		chaine = chaine.replace(motif, "").trim();
		chaine = chaine.replace("\n\t\t\t\t\n", "\n").trim();
		return chaine;
	}

	public static String cleanXMLData(String data) throws UnsupportedEncodingException {
		Tidy tidy = new Tidy();
		tidy.setInputEncoding("UTF-8");
		tidy.setOutputEncoding("UTF-8");
		tidy.setWraplen(Integer.MAX_VALUE);
		tidy.setXmlOut(true);
		tidy.setXmlTags(true);
		tidy.setSmartIndent(true);
		tidy.setMakeClean(true);
		tidy.setForceOutput(true);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes("UTF-8"));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		tidy.parseDOM(inputStream, outputStream);
		return outputStream.toString("UTF-8");
	}

}
