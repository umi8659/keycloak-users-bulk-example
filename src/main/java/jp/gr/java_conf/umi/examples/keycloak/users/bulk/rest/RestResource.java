package jp.gr.java_conf.umi.examples.keycloak.users.bulk.rest;

import org.keycloak.common.ClientConnection;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

public class RestResource {
    @Context
    protected ClientConnection clientConnection;

    @Context
    protected final KeycloakSession session;

    public RestResource(KeycloakSession session) {
        this.session = session;
    }

    @Path("non-auth")
    public UsersBulkResource getUsersBulkResourceNonAuthenticated() {
        return new UsersBulkResource(session);
    }

    @Path("")
    public UsersBulkResource getUsersBulkResource(@Context final HttpHeaders headers) {
        AdminAuth auth = authenticateRealmAdminRequest(headers);
        if (!AdminPermissions.realms(session, auth).isAdmin()) {
            throw new ForbiddenException();
        }
        return new UsersBulkResource(session);
    }

    /**
     * This code has been copied from keycloak org.keycloak.services.resources.admin.AdminRoot;
     * it allows to check if a user as realm/master admin
     * at each upgrade check that it hasn't been modified
     */
    protected AdminAuth authenticateRealmAdminRequest(HttpHeaders headers) {
        String tokenString = AppAuthManager.extractAuthorizationHeaderToken(headers);
        if (tokenString == null) throw new NotAuthorizedException("Bearer");
        AccessToken token;
        try {
            JWSInput input = new JWSInput(tokenString);
            token = input.readJsonContent(AccessToken.class);
        } catch (JWSInputException e) {
            throw new NotAuthorizedException("Bearer token format error");
        }
        String realmName = token.getIssuer().substring(token.getIssuer().lastIndexOf('/') + 1);
        RealmManager realmManager = new RealmManager(session);
        RealmModel realm = realmManager.getRealmByName(realmName);
        if (realm == null) {
            throw new NotAuthorizedException("Unknown realm in token");
        }
        session.getContext().setRealm(realm);

        AuthenticationManager.AuthResult authResult = new AppAuthManager.BearerTokenAuthenticator(session)
                .setRealm(realm)
                .setConnection(clientConnection)
                .setHeaders(headers)
                .authenticate();

        if (authResult == null) {
            // logger.debug("Token not valid");
            throw new NotAuthorizedException("Bearer");
        }

        ClientModel client = realm.getClientByClientId(token.getIssuedFor());
        if (client == null) {
            throw new NotFoundException("Could not find client for authorization");

        }

        return new AdminAuth(realm, authResult.getToken(), authResult.getUser(), client);
    }
}
