package jp.gr.java_conf.umi.examples.keycloak.users.bulk.spi.impl;

import jp.gr.java_conf.umi.examples.keycloak.users.bulk.spi.UsersBulkService;
import jp.gr.java_conf.umi.examples.keycloak.users.bulk.spi.UsersBulkServiceProviderFactory;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class UsersBulkServiceProviderFactoryImpl implements UsersBulkServiceProviderFactory {

    @Override
    public UsersBulkService create(KeycloakSession session) {
        return new UsersBulkServiceImpl(session);
    }

    @Override
    public void init(Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "usersBulkServiceImpl";
    }

}
