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

import java.io.Serializable;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "credential_group_members")
@AssociationOverrides({
		@AssociationOverride(name = "id.credentialGroup", joinColumns = @JoinColumn(name = "cg", nullable = false)),
		@AssociationOverride(name = "id.credential", joinColumns = @JoinColumn(name = "userid", nullable = false)) })
public class CredentialGroupMembers implements Serializable {

	private static final long serialVersionUID = 2334699588563188342L;

	private CredentialGroupMembersId id;

	public CredentialGroupMembers() {
	}

	public CredentialGroupMembers(CredentialGroupMembersId id) {
		this.id = id;
	}

	@EmbeddedId
	public CredentialGroupMembersId getId() {
		return this.id;
	}

	public void setId(CredentialGroupMembersId id) {
		this.id = id;
	}

	@Transient
	public CredentialGroup getCredentialGroup() {
		return getId().getCredentialGroup();
	}

	public void setCredentialGroup(CredentialGroup cg) {
		this.getId().setCredentialGroup(cg);
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

		CredentialGroupMembers that = (CredentialGroupMembers) o;

		if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (getId() != null ? getId().hashCode() : 0);
	}

}
