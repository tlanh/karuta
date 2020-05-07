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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "data_table")
public class DataTable implements Serializable {

	private static final long serialVersionUID = -3258271925946548727L;

	private String id;
	private long owner;
	private long creator;
	private String type;
	private String mimetype;
	private String filename;
	private Long CDate;
	private byte[] data;

	public DataTable() {
	}

	public DataTable(String id, long owner, long creator, String type) {
		this.id = id;
		this.owner = owner;
		this.creator = creator;
		this.type = type;
	}

	public DataTable(String id, long owner, long creator, String type, String mimetype, String filename, Long CDate,
			byte[] data) {
		this.id = id;
		this.owner = owner;
		this.creator = creator;
		this.type = type;
		this.mimetype = mimetype;
		this.filename = filename;
		this.CDate = CDate;
		this.data = data;
	}

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "id", unique = true, nullable = false, length = 36)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "owner", nullable = false)
	public long getOwner() {
		return this.owner;
	}

	public void setOwner(long owner) {
		this.owner = owner;
	}

	@Column(name = "creator", nullable = false)
	public long getCreator() {
		return this.creator;
	}

	public void setCreator(long creator) {
		this.creator = creator;
	}

	@Column(name = "type", nullable = false)
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(name = "mimetype")
	public String getMimetype() {
		return this.mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	@Column(name = "filename")
	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Column(name = "c_date")
	public Long getCDate() {
		return this.CDate;
	}

	public void setCDate(Long CDate) {
		this.CDate = CDate;
	}

	@Column(name = "data")
	public byte[] getData() {
		return this.data;
	}

	public void setData(byte[] data) {
		this.data = data;
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
		DataTable other = (DataTable) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

}
