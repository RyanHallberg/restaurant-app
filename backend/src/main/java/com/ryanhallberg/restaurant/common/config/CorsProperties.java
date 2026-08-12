package com.ryanhallberg.restaurant.common.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
record CorsProperties(List<String> allowedOrigins) {
}
