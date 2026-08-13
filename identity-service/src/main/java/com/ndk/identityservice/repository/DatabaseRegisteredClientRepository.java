package com.ndk.identityservice.repository;


import com.ndk.identityservice.entity.OAuth2Client;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DatabaseRegisteredClientRepository implements RegisteredClientRepository {

    private final OAuth2ClientRepository clientRepository;


    @Override
    public void save(RegisteredClient registeredClient) {
        // Convert RegisteredClient to OAuth2Client entity and save
//        OAuth2Client client = convertToEntity(registeredClient);
//        clientRepository.save(client);
    }

    @Override
    public RegisteredClient findById(String id) {
        return clientRepository.findById(Long.valueOf(id))
            .filter(OAuth2Client::getIsActive)
            .map(this::convertToRegisteredClient)
            .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId)
            .filter(OAuth2Client::getIsActive)
            .map(this::convertToRegisteredClient)
            .orElse(null);
    }

    /**
     * Convert OAuth2Client entity to RegisteredClient
     */
    private RegisteredClient convertToRegisteredClient(OAuth2Client client) {
        RegisteredClient.Builder builder = RegisteredClient.withId(client.getId().toString())
            .clientId(client.getClientId())
            .clientSecret(client.getClientSecret())
            .clientName(client.getClientName());

        // Add redirect URIs
        client.getRedirectUris().forEach(builder::redirectUri);

        // Add post logout redirect URIs
        if (client.getPostLogoutRedirectUris() != null) {
            client.getPostLogoutRedirectUris().forEach(builder::postLogoutRedirectUri);
        }

        // Add scopes
        client.getScopes().forEach(builder::scope);

        // Add grant types
        client.getGrantTypes().forEach(grantType -> {
            switch (grantType.toLowerCase()) {
                case "authorization_code":
                    builder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
                    break;
                case "refresh_token":
                    builder.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN);
                    break;
                case "client_credentials":
                    builder.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS);
                    break;
                default:
                    builder.authorizationGrantType(new AuthorizationGrantType(grantType));
                    break;
            }
        });

        // Add authentication methods
        client.getAuthenticationMethods().forEach(method -> {
            switch (method.toLowerCase()) {
                case "client_secret_basic":
                    builder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
                    break;
                case "client_secret_post":
                    builder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST);
                    break;
                case "client_secret_jwt":
                    builder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_JWT);
                    break;
                case "private_key_jwt":
                    builder.clientAuthenticationMethod(ClientAuthenticationMethod.PRIVATE_KEY_JWT);
                    break;
                case "none":
                    builder.clientAuthenticationMethod(ClientAuthenticationMethod.NONE);
                    break;
                default:
                    builder.clientAuthenticationMethod(new ClientAuthenticationMethod(method));
                    break;
            }
        });

        // Configure client settings
        ClientSettings.Builder clientSettingsBuilder = ClientSettings.builder()
            .requireAuthorizationConsent(client.getRequireAuthorizationConsent())
            .requireProofKey(client.getRequireProofKey());

        builder.clientSettings(clientSettingsBuilder.build());

        // Configure token settings
        TokenSettings.Builder tokenSettingsBuilder = TokenSettings.builder();

        if (client.getAccessTokenTimeToLive() != null) {
            tokenSettingsBuilder.accessTokenTimeToLive(Duration.ofSeconds(client.getAccessTokenTimeToLive()));
        }

        if (client.getRefreshTokenTimeToLive() != null) {
            tokenSettingsBuilder.refreshTokenTimeToLive(Duration.ofSeconds(client.getRefreshTokenTimeToLive()));
        }

        builder.tokenSettings(tokenSettingsBuilder.build());

        return builder.build();
    }

    /**
     * Convert RegisteredClient to OAuth2Client entity
     */
    private OAuth2Client convertToEntity(RegisteredClient registeredClient) {
        OAuth2Client client = new OAuth2Client();

        if (registeredClient.getId() != null) {
            try {
                client.setId(Long.valueOf(registeredClient.getId()));
            } catch (NumberFormatException e) {
                // ID is not a number, this is a new client
            }
        }

        client.setClientId(registeredClient.getClientId());
        client.setClientSecret(registeredClient.getClientSecret());
        client.setClientName(registeredClient.getClientName());

        // Convert redirect URIs
        client.setRedirectUris(registeredClient.getRedirectUris());

        // Convert scopes
        client.setScopes(registeredClient.getScopes());

        // Convert grant types
        Set<String> grantTypes = registeredClient.getAuthorizationGrantTypes().stream()
            .map(AuthorizationGrantType::getValue)
            .collect(Collectors.toSet());
        client.setGrantTypes(grantTypes);

        // Convert authentication methods
        Set<String> authMethods = registeredClient.getClientAuthenticationMethods().stream()
            .map(ClientAuthenticationMethod::getValue)
            .collect(Collectors.toSet());
        client.setAuthenticationMethods(authMethods);

        // Convert client settings
        ClientSettings clientSettings = registeredClient.getClientSettings();
        client.setRequireAuthorizationConsent(clientSettings.isRequireAuthorizationConsent());
        client.setRequireProofKey(clientSettings.isRequireProofKey());

        // Convert token settings
        TokenSettings tokenSettings = registeredClient.getTokenSettings();
        client.setAccessTokenTimeToLive((int) tokenSettings.getAccessTokenTimeToLive().getSeconds());
        client.setRefreshTokenTimeToLive((int) tokenSettings.getRefreshTokenTimeToLive().getSeconds());

        client.setIsActive(true);

        return client;
    }
}
