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
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "credential_substitution")
@AssociationOverrides({
		@AssociationOverride(name = "id.credential", joinColumns = @JoinColumn(name = "userid", nullable = false)) })
public class CredentialSubstitution implements Serializable {

	private static final long serialVersionUID = 5216864434102983759L;
	private CredentialSubstitutionId id;

	public CredentialSubstitution() {
	}

	public CredentialSubstitution(CredentialSubstitutionId id) {
		this.id = id;
	}

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "id", nullable = false)),
			@AttributeOverride(name = "type", column = @Column(name = "type", nullable = false, length = 5)) })
	public CredentialSubstitutionId getId() {
		return this.id;
	}

	public void setId(CredentialSubstitutionId id) {
		this.id = id;
	}

	@Transient
	public Credential getCredential() {
		return getId().getCredential();
	}

	public void setCredential(Credential credential) {
		getId().setCredential(credential);
	}

	@Transient
	public Long getCredentialSubstitutionId() {
		return getId().getId();
	}

	public void setCredentialSubstitutionId(Long id) {
		getId().setId(id);
	}

	@Transient
	public String getType() {
		return getId().getType();
	}

	public void setType(String type) {
		getId().setType(type);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		CredentialSubstitution that = (CredentialSubstitution) o;

		if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (getId() != null ? getId().hashCode() : 0);
	}

}