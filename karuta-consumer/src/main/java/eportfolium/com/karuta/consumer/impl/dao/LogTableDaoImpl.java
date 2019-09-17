package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.LogTableDao;
import eportfolium.com.karuta.model.bean.LogTable;

/**
 * Home object implementation for domain model class LogTable.
 * 
 * @see dao.LogTable
 * @author Hibernate Tools
 */
@Repository
public class LogTableDaoImpl extends AbstractDaoImpl<LogTable> implements LogTableDao {

	@PersistenceContext
	private EntityManager entityManager;

	public LogTableDaoImpl() {
		super();
		setCls(LogTable.class);
	}

}
