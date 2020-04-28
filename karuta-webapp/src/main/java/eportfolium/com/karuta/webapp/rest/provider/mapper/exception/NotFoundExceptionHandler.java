package eportfolium.com.karuta.webapp.rest.provider.mapper.exception;

import eportfolium.com.karuta.model.exception.DoesNotExistException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author lgu
 */
@ControllerAdvice
public class NotFoundExceptionHandler {

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(DoesNotExistException.class)
	@ResponseBody
	public String handleNotFoundRequest(Exception ex) {
		return ex.getMessage();
	}
}
