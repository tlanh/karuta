package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.AnnotationDao;
import eportfolium.com.karuta.model.bean.Annotation;

/**
 * Home object implementation for domain model class Annotation.
 * 
 * @see dao.Annotation
 * @author Hibernate Tools
 */
@Repository
public class AnnotationDaoImpl extends AbstractDaoImpl<Annotation> implements AnnotationDao {

	public AnnotationDaoImpl() {
		super();
		setCls(Annotation.class);
	}


}
