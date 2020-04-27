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
