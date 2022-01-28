package jp.gr.java_conf.umi.examples.keycloak.users.bulk.rest;

import jp.gr.java_conf.umi.examples.keycloak.users.bulk.spi.UsersBulkService;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.resources.admin.UserResource;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileProvider;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.keycloak.userprofile.UserProfileContext.USER_API;

public class UsersBulkResource {
    @Context
    protected final KeycloakSession session;

    protected final RealmModel realm;

    public UsersBulkResource(KeycloakSession session) {
        this.session = session;
        this.realm = session.getContext().getRealm();
        if (this.realm == null) {
            throw new IllegalStateException("The service cannot accept a session without a realm in its context.");
        }
    }

    @POST
    @Path("create")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUsers(final List<UserRepresentation> reps) {
        if (reps == null || reps.isEmpty()) {
            return ErrorResponse.error("Request is empty", Response.Status.BAD_REQUEST);
        }

        try {
            for (UserRepresentation rep : reps) {
                Response response = checkUserRepresentation(rep);
                if (response != null) {
                    return response;
                }
            }

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

    /**
     * This code has been copied and modified from keycloak org.keycloak.services.resources.admin.UsersResource#createUser;
     * at each upgrade check that it hasn't been modified
     */
    private Response checkUserRepresentation(final UserRepresentation rep) {
        String username = rep.getUsername();
        if (realm.isRegistrationEmailAsUsername()) {
            username = rep.getEmail();
        }
        if (ObjectUtil.isBlank(username)) {
            return ErrorResponse.error("User name is missing", Response.Status.BAD_REQUEST);
        }

        // Check duplicated username and email here due to federation
        if (session.users().getUserByUsername(realm, username) != null) {
            return ErrorResponse.exists(String.format("User exists with same username of \"%s\"", username));
        }
        if (rep.getEmail() != null && !realm.isDuplicateEmailsAllowed()) {
            try {
                if (session.users().getUserByEmail(realm, rep.getEmail()) != null) {
                    return ErrorResponse.exists(String.format("User exists with same email of \"%s\"", rep.getEmail()));
                }
            } catch (ModelDuplicateException e) {
                return ErrorResponse.exists(String.format("User exists with same email of \"%s\"", rep.getEmail()));
            }
        }

        UserProfileProvider profileProvider = session.getProvider(UserProfileProvider.class);

        UserProfile profile = profileProvider.create(USER_API, rep.toAttributes());

        try {
            Response response = UserResource.validateUserProfile(profile, null, session);
            if (response != null) {
                return response;
            }

        } catch (Exception e) {
            throw e;
        }
        return null;
    }
}
