package eportfolium.com.karuta.business;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.MimeTypeUtils;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import eportfolium.com.karuta.business.contract.ResourceManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
public class TestResourceManager {

	/**
	 * Pretty-prints xml, supplied as a string.
	 * <p/>
	 * eg. <code>
	 * String formattedXml = new XmlFormatter().format("<tag><nested>hello</nested></tag>");
	 * </code>
	 */
	class XmlFormatter {

		public String format(String xml) {

			try {
				final InputSource src = new InputSource(new StringReader(xml));
				final Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src)
						.getDocumentElement();
				final Boolean keepDeclaration = Boolean.valueOf(xml.startsWith("<?xml"));

				// May need this:
				// System.setProperty(DOMImplementationRegistry.PROPERTY,"com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl");

				final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
				final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
				final LSSerializer writer = impl.createLSSerializer();

				writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); // Set this to true if the
																							// output needs to be
																							// beautified.
				writer.getDomConfig().setParameter("xml-declaration", keepDeclaration); // Set this to true if the
																						// declaration is needed to be
																						// outputted.

				return writer.writeToString(document);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Autowired
	private ResourceManager resourceManager;

	private String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
	private final String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";

	@Test
	public void test_creating_everything_one_by_one() throws Exception {
		String resource = resourceManager.getResource(nodeUuid);
		System.out.println(new XmlFormatter().format(resource));

		String resources = resourceManager.getResources(MimeTypeUtils.TEXT_XML, portfolioUuid, 1L, 3L);
		System.out.println(new XmlFormatter().format(resources));

		String aNodeUuid = "8287020e-7955-493a-9ada-ba95a4434145";
		String xmlResource = "<res></res>";
		resourceManager.addResource(MimeTypeUtils.TEXT_XML, aNodeUuid, xmlResource, 1L, 3L);

//		resourceManager.changeResource(MimeTypeUtils.TEXT_XML, aNodeUuid, xmlResource, 1L, 3L);
//
//		String xsiType = "nodeRes";
//		String content = "<lastmodified/><code/><label lang=\"fr\">Nouvelle Sous-section</label><label lang=\"en\">New Subsection</label>";
//		resourceManager.changeResourceByXsiType(aNodeUuid, xsiType, content, 1L);
	}

	@Test
	public void test_removing_resource() throws DoesNotExistException, BusinessException {
//		String resourceUuid = "";
//		resourceManager.deleteResource(resourceUuid, 1L, 3L);
	}

}
