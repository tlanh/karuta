package eportfolium.com.karuta.consumer;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.consumer.contract.dao.ResourceTableDao;
import eportfolium.com.karuta.model.bean.ResourceTable;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestResourceTableDao {

	private EasyRandom easyRandom;

	public TestResourceTableDao() {
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plus(1, ChronoUnit.DAYS);
		final LocalTime minTime = LocalTime.of(5, 0);
		final LocalTime maxTime = LocalTime.of(9, 0);
		EasyRandomParameters parameters = new EasyRandomParameters().seed(123L).objectPoolSize(100)
				.randomizationDepth(3).charset(Charset.forName("UTF-8")).timeRange(minTime, maxTime)
				.dateRange(today, tomorrow).stringLengthRange(5, 50).collectionSizeRange(1, 10)
				.scanClasspathForConcreteTypes(true).overrideDefaultInitialization(false)
				.ignoreRandomizationErrors(true);
		parameters.excludeField(
				org.jeasy.random.FieldPredicates.named("id").and(org.jeasy.random.FieldPredicates.ofType(Long.class)));

		easyRandom = new EasyRandom(parameters);
	}

	@Autowired
	private ResourceTableDao resourceTableDao;

	private String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";
	private String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
	private String childNodeUuid = "4a00b5ed-432c-4c67-adef-696b6693d0ae";
	private String resUuid = "00210046-3083-4658-a131-37224d321c39";

	@Test
	public void testGetResource() throws Exception {
		ResourceTable result = resourceTableDao.getResource(resUuid);
		Assert.assertEquals(result.getId().toString(), resUuid);
	}

	@Test
	public void testGetResourceByXsiType() {
		String xsiType = "nodeRes";
		ResourceTable res = resourceTableDao.getResourceByXsiType(resUuid, xsiType);
		Assert.assertEquals(resUuid, res.getId().toString());
	}

	@Test
	public void testGetResourceOfResourceByNodeUuid() throws DoesNotExistException {
		ResourceTable res = resourceTableDao.getResourceOfResourceByNodeUuid(nodeUuid);
		Assert.assertNotNull(res);
	}

	@Test
	public void testGetResourceByNodeUuid() {
		String aNodeUuid = "8287020e-7955-493a-9ada-ba95a4434145";
		ResourceTable res = resourceTableDao.getResourceByNodeUuid(aNodeUuid);
		Assert.assertEquals("ed3dc207-1a38-403f-af91-480939da6ccd", res.getId().toString());
	}

	@Test
	public void testGetResNodeContentByNodeUuid() {
		String nodeContent = resourceTableDao.getResNodeContentByNodeUuid(childNodeUuid);
		Assert.assertEquals(nodeContent, "");
	}

	@Test
	public void testGetResourceNodeUuidByParentNodeUuid() throws DoesNotExistException {
		String aNodeUuid = "8287020e-7955-493a-9ada-ba95a4434145";
		UUID resUuid = resourceTableDao.getResourceUuidByParentNodeUuid(aNodeUuid);
		Assert.assertEquals("ed3dc207-1a38-403f-af91-480939da6ccd", resUuid.toString());
	}

	@Test
	public void testGetResourcesOfResourceByPortfolioUUID() throws DoesNotExistException {
		List<ResourceTable> resourcesOfResource = resourceTableDao.getResourcesOfResourceByPortfolioUUID(portfolioUuid);
		Assert.assertNotNull(resourcesOfResource);
		Assert.assertEquals(43, resourcesOfResource.size());
	}

	@Test
	public void testGetContextResourcesByPortfolioUUID() throws DoesNotExistException {
		List<ResourceTable> contextResources = resourceTableDao.getContextResourcesByPortfolioUUID(portfolioUuid);
		Assert.assertNotNull(contextResources);
		Assert.assertEquals(43, contextResources.size());
	}

	@Test
	public void testGetResourcesByPortfolioUUID() throws DoesNotExistException {
		List<ResourceTable> resources = resourceTableDao.getResourcesByPortfolioUUID(portfolioUuid);
		Assert.assertEquals(resources.size(), 28);
	}

	@Test
	public void testGetResourceByNodeParentUuid() throws DoesNotExistException {
		String aNodeUuid = "8287020e-7955-493a-9ada-ba95a4434145";
		ResourceTable result = resourceTableDao.getResourceByParentNodeUuid(aNodeUuid);
		Assert.assertEquals("ed3dc207-1a38-403f-af91-480939da6ccd", result.getId().toString());
	}

	@Test
	@Transactional
	@Rollback
	public void testCreate() throws DoesNotExistException {
		ResourceTable node = easyRandom.nextObject(ResourceTable.class);
		resourceTableDao.persist(node);
	}

	@Test
	@Transactional
	@Rollback
	public void testUpdateResource() {
		String aNodeUuid = "8287020e-7955-493a-9ada-ba95a4434145";
		int result = resourceTableDao.updateResource(aNodeUuid, "ceci est un test", 1L);
		Assert.assertEquals(0, result);
	}

	@Test
	@Transactional
	@Rollback
	public void testUpdateResResource() {
		int result = resourceTableDao.updateResResource(nodeUuid, "ceci est un test", 1L);
		Assert.assertEquals(0, result);
	}

	@Test
	@Transactional
	@Rollback
	public void testUpdateContextResource() {
		int result = resourceTableDao.updateContextResource(UUID.fromString(nodeUuid), "", 1L);
		Assert.assertEquals(0, result);
	}

	/*************************************************************************************************/

}
