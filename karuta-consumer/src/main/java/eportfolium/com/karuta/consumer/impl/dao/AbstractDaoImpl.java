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

package eportfolium.com.karuta.consumer.impl.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
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
	protected EntityManager em;

	public final void setCls(final Class<T> classToSet) {
		this.cls = classToSet;
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll() {
		return em.createQuery("from " + cls.getName()).getResultList();
	}

	public void removeAll() {
		List<T> l = findAll();
		for (Iterator<T> it = l.iterator(); it.hasNext();) {
			em.remove(it.next());
		}
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
			T obj = em.find(cls, id);

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

	/**
	 * Méthode de création
	 * 
	 * @param entity
	 * @return boolean
	 */
	public void persist(final T entity) {
		log.debug("persisting " + cls.getName() + " instance");
		try {
			em.persist(entity);
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
			T result = em.merge(entity);
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
			em.remove(entity);
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

	public ResultSet findAll(String table, Connection con) {
		StringBuffer buf = new StringBuffer();
		ResultSet rs = null;
		try {
			Statement stmt = con.createStatement();
			buf.append("SELECT * FROM " + table);
			stmt.execute(buf.toString());
			rs = stmt.getResultSet();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}

}
