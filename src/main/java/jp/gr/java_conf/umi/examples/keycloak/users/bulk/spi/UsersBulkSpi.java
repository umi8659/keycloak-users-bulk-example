package jp.gr.java_conf.umi.examples.keycloak.users.bulk.spi;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class UsersBulkSpi implements Spi {

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return "usersBulk";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return UsersBulkService.class;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return UsersBulkServiceProviderFactory.class;
    }

}
