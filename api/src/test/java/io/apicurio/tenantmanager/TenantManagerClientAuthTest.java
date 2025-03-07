/*
 * Copyright 2021 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.tenantmanager;

import java.util.Collections;

import javax.enterprise.inject.Typed;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.apicurio.common.apps.test.ApicurioTestTags;
import io.apicurio.common.apps.test.AuthTestProfileWithoutRoles;
import io.apicurio.tenantmanager.client.TenantManagerClient;
import io.apicurio.tenantmanager.client.TenantManagerClientImpl;
import io.apicurio.rest.client.auth.OidcAuth;
import io.apicurio.rest.client.auth.exception.AuthErrorHandler;
import io.apicurio.rest.client.auth.exception.NotAuthorizedException;
import io.apicurio.rest.client.spi.ApicurioHttpClient;
import io.apicurio.rest.client.spi.ApicurioHttpClientFactory;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;


@QuarkusTest
@TestProfile(AuthTestProfileWithoutRoles.class)
@Tag(ApicurioTestTags.DOCKER)
@Typed(TenantManagerClientAuthTest.class)
@Disabled
//TODO fix AuthTestProfileWithoutRoles , realm is not being created , use a oidc mock?
public class TenantManagerClientAuthTest extends TenantManagerClientTest {

    @ConfigProperty(name = "tenant-manager.keycloak.url.configured")
    String authServerUrl;

    String clientId = "registry-api";

    ApicurioHttpClient httpClient;

    private TenantManagerClient createClient(OidcAuth auth) {
        return new TenantManagerClientImpl("http://localhost:8081/", Collections.emptyMap(), auth);
    }

    @Override
    protected TenantManagerClient createRestClient() {
        httpClient = ApicurioHttpClientFactory.create(authServerUrl, new AuthErrorHandler());
        OidcAuth auth = new OidcAuth(httpClient, clientId, "test1");
        return this.createClient(auth);
    }

    @Test
    public void testWrongCreds() throws Exception {
        OidcAuth auth = new OidcAuth(httpClient, clientId, "wrongsecret");
        TenantManagerClient client = createClient(auth);
        Assertions.assertThrows(NotAuthorizedException.class, () -> client.listTenants(null, 0, 10, null, null));
    }
}
