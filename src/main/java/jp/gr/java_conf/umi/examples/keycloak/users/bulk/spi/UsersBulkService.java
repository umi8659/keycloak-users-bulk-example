package jp.gr.java_conf.umi.examples.keycloak.users.bulk.spi;

import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface UsersBulkService extends Provider {

    List<UserModel> createUsers(List<UserRepresentation> users);
}
