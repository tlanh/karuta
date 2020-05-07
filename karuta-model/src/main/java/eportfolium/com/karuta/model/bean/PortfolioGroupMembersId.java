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
public class PortfolioGroupMembersId implements Serializable {

	private static final long serialVersionUID = -549500884031324847L;

	private PortfolioGroup portfolioGroup;
	private Portfolio portfolio;

	public PortfolioGroupMembersId() {
	}

	public PortfolioGroupMembersId(PortfolioGroup portfolioGroup, Portfolio portfolio) {
		this.portfolioGroup = portfolioGroup;
		this.portfolio = portfolio;
	}

	@ManyToOne
	public PortfolioGroup getPortfolioGroup() {
		return this.portfolioGroup;
	}

	public void setPortfolioGroup(PortfolioGroup pg) {
		this.portfolioGroup = pg;
	}

	@ManyToOne
	public Portfolio getPortfolio() {
		return this.portfolio;
	}

	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((portfolio == null) ? 0 : portfolio.hashCode());
		result = prime * result + ((portfolioGroup == null) ? 0 : portfolioGroup.hashCode());
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
		PortfolioGroupMembersId other = (PortfolioGroupMembersId) obj;
		if (portfolio == null) {
			if (other.portfolio != null)
				return false;
		} else if (!portfolio.equals(other.portfolio))
			return false;
		if (portfolioGroup == null) {
			if (other.portfolioGroup != null)
				return false;
		} else if (!portfolioGroup.equals(other.portfolioGroup))
			return false;
		return true;
	}

}
