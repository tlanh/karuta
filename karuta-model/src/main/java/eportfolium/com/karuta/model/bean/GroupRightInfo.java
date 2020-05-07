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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "group_right_info")
public class GroupRightInfo implements Serializable {

	private static final long serialVersionUID = -2012241612038408227L;

	private Long id;
	private long owner;
	private String label;
	private boolean changeRights;
	private Portfolio portfolio;

	private Set<GroupRights> groupRights = new HashSet<GroupRights>();
	private GroupInfo groupInfo;

	public GroupRightInfo() {
	}

	public GroupRightInfo(Long id) {
		this.id = id;
	}

	public GroupRightInfo(Long id, long owner, String label, boolean changeRights) {
		this.id = id;
		this.owner = owner;
		this.label = label;
		this.changeRights = changeRights;
	}

	public GroupRightInfo(Long id, long owner, String label, boolean changeRights, Portfolio portfolio) {
		this.id = id;
		this.owner = owner;
		this.label = label;
		this.changeRights = changeRights;
		this.portfolio = portfolio;
	}

	public GroupRightInfo(Portfolio portfolio, String label) {
		this.portfolio = portfolio;
		this.label = label;
		this.owner = 1;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "grid", unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
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

	@Column(name = "change_rights", nullable = false)
	public boolean isChangeRights() {
		return this.changeRights;
	}

	public void setChangeRights(boolean changeRights) {
		this.changeRights = changeRights;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "portfolio_id")
	public Portfolio getPortfolio() {
		return this.portfolio;
	}

	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
	}

	@OneToOne(mappedBy = "groupRightInfo", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
	public GroupInfo getGroupInfo() {
		return groupInfo;
	}

	public void setGroupInfo(GroupInfo groupInfo) {
		if (groupInfo == null) {
			if (this.groupInfo != null) {
				this.groupInfo.setGroupRightInfo(null);
			}
		} else {
			groupInfo.setGroupRightInfo(this);
		}
		this.groupInfo = groupInfo;
	}

	@OneToMany(mappedBy = "id.groupRightInfo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Set<GroupRights> getGroupRights() {
		return groupRights;
	}

	public void setGroupRights(Set<GroupRights> groupRights) {
		this.groupRights = groupRights;
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
		GroupRightInfo other = (GroupRightInfo) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

}
