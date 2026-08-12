package com.ryanhallberg.restaurant.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * PEM resource locations: classpath dev keys locally, Secret Manager volume
 * mounts (file:/secrets/...) in prod.
 */
@ConfigurationProperties(prefix = "app.jwt")
record JwtKeyProperties(Resource publicKey, Resource privateKey) {
}
