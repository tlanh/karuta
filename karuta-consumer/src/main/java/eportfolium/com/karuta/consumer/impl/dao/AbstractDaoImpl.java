package eportfolium.com.karuta.consumer.impl.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public abstract class AbstractDaoImpl<T extends Serializable> {

	private static final Log log = LogFactory.getLog(AbstractDaoImpl.class);

	private Class<T> cls;

	@PersistenceContext
	private EntityManager entityManager;

	public final void setCls(final Class<T> classToSet) {
		this.cls = classToSet;
	}

	/**
	 * Méthode de recherche des informations
	 * 
	 * @param id
	 * @return T
	 */
	public T findById(Serializable id) throws DoesNotExistException {
		if (id == null) {
			throw new IllegalArgumentException(
					"find(class, id) has been given null id.  Class is " + cls.getName() + ".");
		} else if (id.equals(0)) {
			throw new IllegalArgumentException(
					"find(class, id) has been given zero id.  Class is " + cls.getName() + ".");
		}

		try {
			T obj = entityManager.find(cls, id);

			if (obj == null) {
				throw new DoesNotExistException(cls, id);
			}

			return obj;
		} catch (IllegalArgumentException e) {
			// Invalid id
			throw new IllegalArgumentException(
					"find(class, id) has been given invalid id.  Class is " + cls.getName() + ", id is \"" + id + "\".",
					e);
		} catch (Exception e) {
			// Doesn't exist
			throw new DoesNotExistException(cls, id);
		}
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll() {
		return entityManager.createQuery("from " + cls.getName()).getResultList();
	}

	/**
	 * Méthode de création
	 * 
	 * @param entity
	 * @return boolean
	 */
	public void persist(final T entity) {
		log.debug("persisting " + cls.getName() + " instance");
		try {
			entityManager.persist(entity);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	/**
	 * Méthode de mise à jour<br>
	 * Beware - the caller MUST use the result, eg. invoice = merge(invoice).
	 * 
	 * @param <T> entity
	 * @return
	 * @throws BusinessException
	 */
	public T merge(final T entity) {
		log.debug("merging " + cls.getName() + " instance");
		try {
			T result = entityManager.merge(entity);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	/**
	 * Méthode pour effacer
	 * 
	 * @param entity
	 * @return boolean
	 */
	public void remove(final T entity) {
		log.debug("removing " + cls.getName() + "  instance");
		try {
			entityManager.remove(entity);
			log.debug("remove successful");
		} catch (RuntimeException re) {
			log.error("remove failed", re);
			throw re;
		}
	}

	/**
	 * Méthode de suppr de l'entité.
	 * 
	 * @param id
	 * @return T
	 */
	public void removeById(final Serializable id) throws DoesNotExistException {
		T entity;
		entity = findById(id);
		remove(entity);
	}

}
