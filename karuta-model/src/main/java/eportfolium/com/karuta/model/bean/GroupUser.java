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

package eportfolium.com.karuta.model.bean;
// Generated 13 juin 2019 19:14:13 by Hibernate Tools 5.2.10.Final

import java.io.Serializable;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author mlengagne
 *
 *         Karuta enables you to give your users certain privileges, by
 *         assigning them to groups. You can create as many users groups as
 *         needed, and assign a user to as many groups as you like.
 */
@Entity
@Table(name = "group_user")
@AssociationOverrides({
		@AssociationOverride(name = "id.groupInfo", joinColumns = @JoinColumn(name = "gid", nullable = false)),
		@AssociationOverride(name = "id.credential", joinColumns = @JoinColumn(name = "userid", nullable = false)) })
public class GroupUser implements Serializable {

	private static final long serialVersionUID = -1516598438094285942L;

	private GroupUserId id;

	public GroupUser() {
	}

	public GroupUser(Long groupId, Long userId) {
		this.id = new GroupUserId(new GroupInfo(groupId), new Credential(userId));
	}

	public GroupUser(GroupUserId id) {
		this.id = id;
	}

	@EmbeddedId
	public GroupUserId getId() {
		return this.id;
	}

	public void setId(GroupUserId id) {
		this.id = id;
	}

	@Transient
	public GroupInfo getGroupInfo() {
		return getId().getGroupInfo();
	}

	public void setGroupInfo(GroupInfo groupInfo) {
		getId().setGroupInfo(groupInfo);
	}

	@Transient
	public Credential getCredential() {
		return getId().getCredential();
	}

	public void setCredential(Credential credential) {
		getId().setCredential(credential);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		GroupUser that = (GroupUser) o;

		if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (getId() != null ? getId().hashCode() : 0);
	}

}
