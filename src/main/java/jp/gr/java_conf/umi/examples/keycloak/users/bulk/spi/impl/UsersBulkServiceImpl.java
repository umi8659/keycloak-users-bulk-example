package jp.gr.java_conf.umi.examples.keycloak.users.bulk.spi.impl;

import jp.gr.java_conf.umi.examples.keycloak.users.bulk.spi.UsersBulkService;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.idm.UserRepresentation;

import javax.persistence.EntityManager;
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

    private EntityManager getEntityManager() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    protected RealmModel getRealm() {
        return session.getContext().getRealm();
    }

    @Override
    public List<UserModel> createUsers(List<UserRepresentation> reps) {
        RealmModel realm = getRealm();
        EntityManager em = getEntityManager();
        List<UserModel> users = new ArrayList<>();

        reps.forEach(user -> {
            UserEntity entity = new UserEntity();
            String id = KeycloakModelUtils.generateId();
            entity.setId(id);
            entity.setCreatedTimestamp(System.currentTimeMillis());
            entity.setUsername(user.getUsername().toLowerCase());
            entity.setRealmId(realm.getId());
            em.persist(entity);
            UserAdapter userModel = new UserAdapter(session, realm, em, entity);
            users.add(userModel);
        });

        return users;
    }

    public void close() {
        // Nothing to do.
    }

}
