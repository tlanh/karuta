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

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

@Embeddable
public class CredentialGroupMembersId implements Serializable {

	private static final long serialVersionUID = 2800890235327047840L;

	private CredentialGroup credentialGroup;
	private Credential credential;

	public CredentialGroupMembersId() {
	}

	public CredentialGroupMembersId(CredentialGroup credentialGroup, Credential credential) {
		this.credentialGroup = credentialGroup;
		this.credential = credential;
	}

	@ManyToOne
	public CredentialGroup getCredentialGroup() {
		return this.credentialGroup;
	}

	public void setCredentialGroup(CredentialGroup cg) {
		this.credentialGroup = cg;
	}

	@ManyToOne
	public Credential getCredential() {
		return this.credential;
	}

	public void setCredential(Credential credential) {
		this.credential = credential;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((credential == null) ? 0 : credential.hashCode());
		result = prime * result + ((credentialGroup == null) ? 0 : credentialGroup.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CredentialGroupMembersId other = (CredentialGroupMembersId) obj;
		if (credential == null) {
			if (other.credential != null)
				return false;
		} else if (!credential.equals(other.credential))
			return false;
		if (credentialGroup == null) {
			if (other.credentialGroup != null)
				return false;
		} else if (!credentialGroup.equals(other.credentialGroup))
			return false;
		return true;
	}

}
