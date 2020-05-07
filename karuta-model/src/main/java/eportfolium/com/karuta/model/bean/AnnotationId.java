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

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class AnnotationId implements Serializable {

	private static final long serialVersionUID = 6961270590302840044L;
	private String nodeid;
	private int rank;

	public AnnotationId() {
	}

	public AnnotationId(String nodeid, int rank) {
		this.nodeid = nodeid;
		this.rank = rank;
	}

	@Column(name = "nodeid", nullable = false, length = 128)
	public String getNodeid() {
		return this.nodeid;
	}

	public void setNodeid(String nodeid) {
		this.nodeid = nodeid;
	}

	@Column(name = "rank", nullable = false)
	public int getRank() {
		return this.rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeid == null) ? 0 : nodeid.hashCode());
		result = prime * result + rank;
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
		AnnotationId other = (AnnotationId) obj;
		if (nodeid == null) {
			if (other.nodeid != null)
				return false;
		} else if (!nodeid.equals(other.nodeid))
			return false;
		if (rank != other.rank)
			return false;
		return true;
	}

}
