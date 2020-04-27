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

package eportfolium.com.karuta.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

/**
 * @author mlengagne
 *         <p>
 *         We require a custom type for Bytes. The implementation above does not
 *         expose the underlying byte array to clients, so its safe to define
 *         this as an immutable type.
 *         </p>
 */
public class BytesType implements UserType {

	private static final int[] SQL_TYPES = { Types.VARBINARY };

	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	public boolean isMutable() {
		return false;
	}

	public Class<Bytes> returnedClass() {
		return Bytes.class;
	}

	public boolean equals(Object x, Object y) {
		return (x == y) || (x != null && y != null && x.equals(y));
	}

	public Object deepCopy(Object value) {
		return value;
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		byte[] bytes = rs.getBytes(names[0]);
		if (rs.wasNull())
			return null;
		return new Bytes(bytes);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
			throws HibernateException, SQLException {
		if (value == null) {
			st.setNull(index, Types.VARBINARY);
		} else {
			st.setBytes(index, ((Bytes) value).getBytes());
		}

	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		// TODO Auto-generated method stub
		return 0;
	}

}
