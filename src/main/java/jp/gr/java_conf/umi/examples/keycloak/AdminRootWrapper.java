package jp.gr.java_conf.umi.examples.keycloak;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminRoot;

import javax.ws.rs.core.HttpHeaders;

public class AdminRootWrapper extends AdminRoot {
    public AdminRootWrapper(KeycloakSession session) {
        this.session = session;
    }

    public AdminAuth authenticateRealmAdminRequest(HttpHeaders headers) {
        return super.authenticateRealmAdminRequest(headers);
    }
}
