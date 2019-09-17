package eportfolium.com.karuta.util;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.UUIDGenerator;

/**
 * @author mlengagne
 *         <p>
 *         Now we need to define an IdentifierGenerator. We will reuse one of
 *         Hibernate's built-in UUID algorithms.
 *         </p>
 */
public class BytesGenerator implements IdentifierGenerator {

	private IdentifierGenerator uuid = UUIDGenerator.buildSessionFactoryUniqueIdentifierGenerator();

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
		return new Bytes(((String) uuid.generate(session, object)).getBytes());
	}

}