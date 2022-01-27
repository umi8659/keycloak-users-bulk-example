package jp.gr.java_conf.umi.examples.keycloak.users.bulk.rest;

import jp.gr.java_conf.umi.examples.keycloak.users.bulk.spi.UsersBulkService;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ErrorResponseException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class UsersBulkResource {
    @Context
    protected final KeycloakSession session;

    public UsersBulkResource(KeycloakSession session) {
        this.session = session;
    }

    @POST
    @Path("create")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUsers(final List<UserRepresentation> reps) {
        try {
            List<UserModel> users = session.getProvider(UsersBulkService.class).createUsers(reps);
            List<UserRepresentation> created = users.stream().map(user -> ModelToRepresentation.toRepresentation(session, session.getContext().getRealm(), user)).collect(Collectors.toList());

            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().commit();
            }

            URI uri = UriBuilder.fromPath("/realms/{realm}/users").build(session.getContext().getRealm().getName());
            return Response.created(uri).entity(created).build();
        } catch (ModelDuplicateException e) {
            return ErrorResponse.exists(e.getLocalizedMessage());
        } catch (ErrorResponseException error) {
            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().setRollbackOnly();
            }
            return error.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
            Throwable cause = e.getCause();
            if (cause != null) {
                cause.printStackTrace();
            }

            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().setRollbackOnly();
            }
            return ErrorResponse.error(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
