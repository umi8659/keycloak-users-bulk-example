package jp.gr.java_conf.umi.examples.keycloak.users.bulk.rest;

import jp.gr.java_conf.umi.examples.keycloak.AdminRootWrapper;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

public class RestResource {
    @Context
    protected final KeycloakSession session;

    protected final AdminRootWrapper adminRoot;

    public RestResource(KeycloakSession session) {
        this.session = session;
        this.adminRoot = new AdminRootWrapper(session);
    }

    @Path("non-auth")
    public UsersBulkResource getUsersBulkResourceNonAuthenticated() {
        return new UsersBulkResource(session);
    }

    @Path("")
    public UsersBulkResource getUsersBulkResource(@Context final HttpHeaders headers) {
        AdminAuth auth = adminRoot.authenticateRealmAdminRequest(headers);
        if (!AdminPermissions.realms(session, auth).isAdmin()) {
            throw new ForbiddenException();
        }
        return new UsersBulkResource(session);
    }
}
