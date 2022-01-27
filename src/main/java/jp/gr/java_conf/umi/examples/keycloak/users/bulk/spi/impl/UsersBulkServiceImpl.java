package jp.gr.java_conf.umi.examples.keycloak.users.bulk.spi.impl;

import jp.gr.java_conf.umi.examples.keycloak.users.bulk.spi.UsersBulkService;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.List;

public class UsersBulkServiceImpl implements UsersBulkService {
    @Context
    protected final KeycloakSession session;

    public UsersBulkServiceImpl(KeycloakSession session) {
        this.session = session;
        if (getRealm() == null) {
            throw new IllegalStateException("The service cannot accept a session without a realm in its context.");
        }
    }

    protected RealmModel getRealm() {
        return session.getContext().getRealm();
    }

    @Override
    public List<UserModel> createUsers(List<UserRepresentation> reps) {
        RealmModel realm = getRealm();
        List<UserModel> users = new ArrayList<>();

        reps.forEach(user -> users.add(RepresentationToModel.createUser(session, realm, user)));

        return users;
    }

    public void close() {
        // Nothing to do.
    }

}
