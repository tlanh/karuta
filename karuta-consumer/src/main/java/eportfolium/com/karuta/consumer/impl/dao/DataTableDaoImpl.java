package eportfolium.com.karuta.consumer.impl.dao;

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

	public DataTableDaoImpl() {
		super();
		setCls(DataTable.class);
	}

}
