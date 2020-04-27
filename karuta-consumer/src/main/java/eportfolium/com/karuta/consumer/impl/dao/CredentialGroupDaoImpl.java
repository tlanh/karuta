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
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.CredentialGroupDao;
import eportfolium.com.karuta.model.bean.CredentialGroup;

/**
 * Home object implementation for domain model class CredentialGroup.
 * 
 * @see dao.CredentialGroup
 * @author Hibernate Tools
 */
@Repository
public class CredentialGroupDaoImpl extends AbstractDaoImpl<CredentialGroup> implements CredentialGroupDao {

	private static final Log log = LogFactory.getLog(CredentialGroupDaoImpl.class);

	public CredentialGroupDaoImpl() {
		super();
		setCls(CredentialGroup.class);
	}

	public CredentialGroup getByName(String name) {
		CredentialGroup cr = null;
		String sql = "SELECT cg FROM CredentialGroup cg";
		sql += " WHERE cg.label = :label";
		TypedQuery<CredentialGroup> q = em.createQuery(sql, CredentialGroup.class);
		q.setParameter("label", name);
		try {
			cr = q.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return cr;
	}

	public Boolean rename(Long credentialGroupId, String newName) {
		boolean isOK = true;

		String sql = "SELECT cg FROM CredentialGroup cg WHERE cg.id = :groupId";
		TypedQuery<CredentialGroup> q = em.createQuery(sql, CredentialGroup.class);
		q.setParameter("groupId", credentialGroupId);
		try {
			CredentialGroup cg = q.getSingleResult();
			cg.setLabel(newName);
			merge(cg);

		} catch (Exception e) {
			e.printStackTrace();
			isOK = false;
		}

		return isOK;
	}

	public Long add(String name) throws Exception {
		try {
			CredentialGroup cg = new CredentialGroup();
			cg.setLabel(name);
			cg = merge(cg);
			return cg.getId();
		} catch (Exception re) {
			log.error("createUserGroup failed", re);
			throw re;
		}
	}

}
