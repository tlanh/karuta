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
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

@Embeddable
public class GroupRightsId implements Serializable {

	private static final long serialVersionUID = 7042573400581202433L;

	private GroupRightInfo groupRightInfo;

	// Association with Node entity ?
	private UUID id;

	public GroupRightsId() {
	}

	public GroupRightsId(GroupRightInfo groupRightInfo, UUID id) {
		this.groupRightInfo = groupRightInfo;
		this.id = id;
	}

	@ManyToOne
	public GroupRightInfo getGroupRightInfo() {
		return this.groupRightInfo;
	}

	public void setGroupRightInfo(GroupRightInfo groupRightInfo) {
		this.groupRightInfo = groupRightInfo;
	}

	@Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
	public UUID getId() {
		return this.id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupRightInfo == null) ? 0 : groupRightInfo.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		GroupRightsId other = (GroupRightsId) obj;
		if (groupRightInfo == null) {
			if (other.groupRightInfo != null)
				return false;
		} else if (!groupRightInfo.equals(other.groupRightInfo))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
