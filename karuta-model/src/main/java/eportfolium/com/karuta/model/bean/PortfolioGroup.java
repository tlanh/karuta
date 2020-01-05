package eportfolium.com.karuta.model.bean;
// Generated 13 juin 2019 19:14:13 by Hibernate Tools 5.2.10.Final

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.search.annotations.Indexed;

/**
 * PortfolioGroup generated by hbm2java
 */
@Indexed
@Entity
@Table(name = "portfolio_group")
public class PortfolioGroup implements Serializable {

	private static final long serialVersionUID = 3813446001586883933L;

	private Long id;
	private String label;
	private String type;
	private PortfolioGroup parent;

	public PortfolioGroup() {
	}

	public PortfolioGroup(Long id) {
		this.id = id;
	}

	public PortfolioGroup(Long id, String label, String type) {
		this.id = id;
		this.label = label;
		this.type = type;
	}

	public PortfolioGroup(Long id, String label, String type, PortfolioGroup parent) {
		this.id = id;
		this.label = label;
		this.type = type;
		this.parent = parent;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pg", unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "label", unique = true, nullable = false, length = 255)
	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Column(name = "type", nullable = false, length = 9)
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "pg_parent")
	public PortfolioGroup getParent() {
		return this.parent;
	}

	public void setParent(PortfolioGroup parent) {
		this.parent = parent;
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
		PortfolioGroup other = (PortfolioGroup) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

}
