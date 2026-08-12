package com.ryanhallberg.restaurant.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * The spec served at /v3/api-docs is the frontend's type source
 * (@hey-api/openapi-ts generates the TS client from it), so annotations here
 * and on controllers are part of the public contract.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Restaurant API",
        version = "v1",
        description = "API for the Sage & Ember mock restaurant: menu, reservations, ordering, admin."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
