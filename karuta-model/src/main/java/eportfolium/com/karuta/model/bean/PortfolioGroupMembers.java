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
@Table(name = "portfolio_group_members")
@AssociationOverrides({
		@AssociationOverride(name = "id.portfolioGroup", joinColumns = @JoinColumn(name = "pg", nullable = false)),
		@AssociationOverride(name = "id.portfolio", joinColumns = @JoinColumn(name = "portfolio_id", nullable = false)) })
public class PortfolioGroupMembers implements Serializable {

	private static final long serialVersionUID = 7694077274860630934L;

	private PortfolioGroupMembersId id;

	public PortfolioGroupMembers() {
	}

	public PortfolioGroupMembers(PortfolioGroupMembersId id) {
		this.id = id;
	}

	@EmbeddedId
	public PortfolioGroupMembersId getId() {
		return this.id;
	}

	public void setId(PortfolioGroupMembersId id) {
		this.id = id;
	}

	@Transient
	public PortfolioGroup getPortfolioGroup() {
		return getId().getPortfolioGroup();
	}

	public void setPortfolioGroup(PortfolioGroup pg) {
		getId().setPortfolioGroup(pg);
	}

	@Transient
	public Portfolio getPortfolio() {
		return getId().getPortfolio();
	}

	public void setPortfolio(Portfolio portfolio) {
		getId().setPortfolio(portfolio);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		PortfolioGroupMembers that = (PortfolioGroupMembers) o;

		if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (getId() != null ? getId().hashCode() : 0);
	}

}
