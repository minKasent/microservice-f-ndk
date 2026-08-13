package com.ndk.common.api.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
    name = "oauth2",
    type = SecuritySchemeType.OAUTH2,
    flows = @OAuthFlows(
        authorizationCode = @OAuthFlow(
            authorizationUrl = "${api-docs.oauth2.authorization-url}",
            tokenUrl = "${api-docs.oauth2.token-url}",
            scopes = {
                @OAuthScope(
                    name = "profile"
                )
            }
        )
    )
)
@OpenAPIDefinition(
    security = @SecurityRequirement(name = "oauth2"),
    servers = @Server(url = "${api-docs.server}")
)
public class OpenApiConfig {

}
