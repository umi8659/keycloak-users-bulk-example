package jp.gr.java_conf.umi.examples.keycloak.users.bulk.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class UsersBulkResourceProvider implements RealmResourceProvider {

    protected final KeycloakSession session;

    public UsersBulkResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new RestResource(session);
    }

    @Override
    public void close() {
    }

}
