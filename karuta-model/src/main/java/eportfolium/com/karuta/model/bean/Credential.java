package eportfolium.com.karuta.model.bean;
// Generated 13 juin 2019 19:14:13 by Hibernate Tools 5.2.10.Final

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.search.annotations.Indexed;

/**
 * @author mlengagne
 *         <p>
 *         A qualification, achievement, personal quality, or aspect of a
 *         person's background, typically when used to indicate that they are
 *         suitable for something. recruitment is based mainly on academic
 *         credentials
 *         </p>
 *
 */
@Indexed
@Entity
@Table(name = "credential", uniqueConstraints = @UniqueConstraint(columnNames = "login"))
public class Credential implements Serializable {

	private static final long serialVersionUID = -8685729959105792177L;

	public static final String defaultDateInputPattern = "dd/MM/yy";
	public static final String defaultDateViewPattern = "dd MMM yyyy";
	public static final String defaultDateListPattern = "yyyy-MM-dd";

	private Long id;
	private String login;
	private int canSubstitute;
	private int isAdmin;
	private int isDesigner;
	private int active;
	private String displayFirstname;
	private String displayLastname;
	private String email;
	private String password;
	private String token;
	private Long CDate;
	private String other;
	private CredentialSubstitution credentialSubstitution;

	private Set<GroupUser> groups = new HashSet<GroupUser>();
	private Set<Portfolio> portfolios = new HashSet<Portfolio>();

	private String subUser;

	public Credential() {
	}

	public Credential(String login) {
		this.login = login;
	}

	public Credential(Long id) {
		this.id = id;
	}

	public Credential(Long id, String login, int canSubstitute, int isAdmin, int isDesigner, int active,
			String displayFirstname, String displayLastname, String password, String other) {
		this.id = id;
		this.login = login;
		this.canSubstitute = canSubstitute;
		this.isAdmin = isAdmin;
		this.isDesigner = isDesigner;
		this.active = active;
		this.displayFirstname = displayFirstname;
		this.displayLastname = displayLastname;
		this.password = password;
		this.other = other;
	}

	public Credential(Long id, String login, int canSubstitute, int isAdmin, int isDesigner, int active,
			String displayFirstname, String displayLastname, String email, String password, String token, Long CDate,
			String other) {
		this.id = id;
		this.login = login;
		this.canSubstitute = canSubstitute;
		this.isAdmin = isAdmin;
		this.isDesigner = isDesigner;
		this.active = active;
		this.displayFirstname = displayFirstname;
		this.displayLastname = displayLastname;
		this.email = email;
		this.password = password;
		this.token = token;
		this.CDate = CDate;
		this.other = other;
	}

	public Credential(Long crId, String login, String firstName) {
		this.id = crId;
		this.login = login;
		this.displayFirstname = firstName;
	}

	@Id
	@Column(name = "userid", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "login", unique = true, nullable = false)
	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	@Column(name = "can_substitute", nullable = false)
	public int getCanSubstitute() {
		return this.canSubstitute;
	}

	public void setCanSubstitute(int canSubstitute) {
		this.canSubstitute = canSubstitute;
	}

	@Column(name = "is_admin", nullable = false)
	public int getIsAdmin() {
		return this.isAdmin;
	}

	public void setIsAdmin(int isAdmin) {
		this.isAdmin = isAdmin;
	}

	@Column(name = "is_designer", nullable = false)
	public int getIsDesigner() {
		return this.isDesigner;
	}

	public void setIsDesigner(int isDesigner) {
		this.isDesigner = isDesigner;
	}

	@Column(name = "active", nullable = false)
	public int getActive() {
		return this.active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	@Column(name = "display_firstname", nullable = false)
	public String getDisplayFirstname() {
		return this.displayFirstname;
	}

	public void setDisplayFirstname(String displayFirstname) {
		this.displayFirstname = displayFirstname;
	}

	@Column(name = "display_lastname", nullable = false)
	public String getDisplayLastname() {
		return this.displayLastname;
	}

	public void setDisplayLastname(String displayLastname) {
		this.displayLastname = displayLastname;
	}

	@Column(name = "email")
	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "password", nullable = false, length = 128)
	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Column(name = "token")
	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Column(name = "c_date")
	public Long getCDate() {
		return this.CDate;
	}

	public void setCDate(Long CDate) {
		this.CDate = CDate;
	}

	@Column(name = "other", nullable = false)
	public String getOther() {
		return this.other;
	}

	public void setOther(String other) {
		this.other = other;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "id.credential")
	public CredentialSubstitution getCredentialSubstitution() {
		return credentialSubstitution;
	}

	public void setCredentialSubstitution(CredentialSubstitution credentialSubstitution) {
		this.credentialSubstitution = credentialSubstitution;
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
		Credential other = (Credential) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	/**
	 * Credential is in a COMPOSITION relationship with CredentialGroup in which
	 * Credential is the parent.
	 */
	@OneToMany(mappedBy = "id.credential", fetch = FetchType.LAZY)
	public Set<GroupUser> getGroups() {
		return groups;
	}

	public void setGroups(Set<GroupUser> groups) {
		this.groups = groups;
	}

	@OneToMany(mappedBy = "credential", fetch = FetchType.LAZY)
	public Set<Portfolio> getPortfolios() {
		return portfolios;
	}

	public void setPortfolios(Set<Portfolio> portfolios) {
		this.portfolios = portfolios;
	}

	@Transient
	public String getSubUser() {
		return this.subUser;
	}

	public void setSubUser(String subUser) {
		this.subUser = subUser;
	}

	public void addPortfolio(Portfolio portfolio) {
		portfolio.setCredential(this);
	}

	/**
	 * Beware - in JPA to persist this change you must merge or remove the child.
	 * Merging the parent will not cascade to the child because it no longer has a
	 * reference to the child.
	 */
	public void removePortfolio(Portfolio portfolio) {
		portfolio.setCredential(null);
	}

	public void internalAddPortfolio(Portfolio portfolio) {
		portfolios.add(portfolio);
	}

	/**
	 * Beware - in JPA to persist this change you must merge or remove the child.
	 * Merging the parent will not cascade to the child because it no longer has a
	 * reference to the child.
	 */
	public void internalRemovePortfolio(Portfolio portfolio) {
		portfolios.remove(portfolio);
	}

	@Override
	public String toString() {
		return "Credential [id=" + id + ", login=" + login + ", canSubstitute=" + canSubstitute + ", isAdmin=" + isAdmin
				+ ", isDesigner=" + isDesigner + ", active=" + active + ", displayFirstname=" + displayFirstname
				+ ", displayLastname=" + displayLastname + ", email=" + email + ", password=" + password + ", token="
				+ token + ", other=" + other + "]";
	}

}
