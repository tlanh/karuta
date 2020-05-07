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
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "annotation")
public class Annotation implements Serializable {

	private static final long serialVersionUID = 5673430351996465720L;

	private AnnotationId id;
	private String text;
	private Date CDate;
	private String AUser;
	private String wadIdentifier;

	public Annotation() {
	}

	@EmbeddedId
	@AttributeOverrides({
			@AttributeOverride(name = "nodeid", column = @Column(name = "nodeid", nullable = false, length = 128)),
			@AttributeOverride(name = "rank", column = @Column(name = "rank", nullable = false)) })
	public AnnotationId getId() {
		return this.id;
	}

	public void setId(AnnotationId id) {
		this.id = id;
	}

	@Lob
	@Column(name = "text")
	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "c_date", nullable = false, length = 19)
	public Date getCDate() {
		return this.CDate;
	}

	public void setCDate(Date CDate) {
		this.CDate = CDate;
	}

	@Column(name = "a_user", nullable = false)
	public String getAUser() {
		return this.AUser;
	}

	public void setAUser(String AUser) {
		this.AUser = AUser;
	}

	@Column(name = "wad_identifier")
	public String getWadIdentifier() {
		return this.wadIdentifier;
	}

	public void setWadIdentifier(String wadIdentifier) {
		this.wadIdentifier = wadIdentifier;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Annotation that = (Annotation) o;

		if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (getId() != null ? getId().hashCode() : 0);
	}
}
