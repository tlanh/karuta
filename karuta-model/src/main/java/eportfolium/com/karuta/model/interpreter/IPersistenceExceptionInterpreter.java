package eportfolium.com.karuta.model.interpreter;

import java.sql.SQLException;

import javax.persistence.PersistenceException;

import eportfolium.com.karuta.model.exception.BusinessException;

public interface IPersistenceExceptionInterpreter {

	public BusinessException interpret(PersistenceException e);
	
	// SQLException is leaking out of JBoss 5.0 in duplicate alternate key situations
	public BusinessException interpret(SQLException e);

}
