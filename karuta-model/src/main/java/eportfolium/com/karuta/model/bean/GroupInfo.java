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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "group_info")
public class GroupInfo implements Serializable {

	private static final long serialVersionUID = -4066550096842378284L;

	private Long id;
	private GroupRightInfo groupRightInfo;
	private long owner;
	private String label;
	private Set<GroupUser> groupUser = new HashSet<GroupUser>(0);

	public GroupInfo() {
	}

	public GroupInfo(Long grid, Long owner, String label) {
		this.groupRightInfo = new GroupRightInfo(grid);
		this.owner = owner;
		this.label = label;
	}

	public GroupInfo(Long groupId) {
		this.id = groupId;
	}

	public GroupInfo(Long id, long owner, String label) {
		this.id = id;
		this.owner = owner;
		this.label = label;
	}

	public GroupInfo(GroupRightInfo groupRightInfo, long owner, String label) {
		this.groupRightInfo = groupRightInfo;
		this.owner = owner;
		this.label = label;
	}

	public GroupInfo(GroupInfo group) {
		this.groupRightInfo = group.getGroupRightInfo();
		this.owner = group.getOwner();
		this.label = group.getLabel();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "gid", unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "grid")
	public GroupRightInfo getGroupRightInfo() {
		return this.groupRightInfo;
	}

	public void setGroupRightInfo(GroupRightInfo gri) {
		this.groupRightInfo = gri;
	}

	@Column(name = "owner", nullable = false)
	public long getOwner() {
		return this.owner;
	}

	public void setOwner(long owner) {
		this.owner = owner;
	}

	@Column(name = "label", nullable = false)
	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@OneToMany(mappedBy = "id.groupInfo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	public Set<GroupUser> getGroupUser() {
		return groupUser;
	}

	public void setGroupUser(Set<GroupUser> groupUser) {
		this.groupUser = groupUser;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
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
		GroupInfo other = (GroupInfo) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

}
