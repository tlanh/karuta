package eportfolium.com.karuta.webapp.rest.provider.mapper.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import eportfolium.com.karuta.model.exception.DoesNotExistException;

/**
 * {@link ExceptionMapper} pour les {@link DoesNotExistException}
 *
 * @author lgu
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<DoesNotExistException> {

	@Override
	public Response toResponse(DoesNotExistException pException) {
		return Response.status(Response.Status.NOT_FOUND).entity(pException.getMessage()).build();
	}
}
