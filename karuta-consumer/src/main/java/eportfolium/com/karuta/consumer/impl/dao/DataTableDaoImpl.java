package eportfolium.com.karuta.consumer.impl.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.DataTableDao;
import eportfolium.com.karuta.model.bean.DataTable;

/**
 * Home object implementation for domain model class DataTable.
 * 
 * @see dao.DataTable
 * @author Hibernate Tools
 */
@Repository
public class DataTableDaoImpl extends AbstractDaoImpl<DataTable> implements DataTableDao {

	@PersistenceContext
	private EntityManager entityManager;

	public DataTableDaoImpl() {
		super();
		setCls(DataTable.class);
	}

}
