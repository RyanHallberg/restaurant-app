package com.ryanhallberg.restaurant.common.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Time is injected rather than called statically so services that reason about
 * "now" (reservation slots) can be tested with a fixed clock. The zone is the
 * restaurant's business timezone, never the JVM default — containers run UTC,
 * which would silently shift "today" and "past slots" for a non-UTC venue.
 */
@Configuration
public class ClockConfig {

    @Bean
    Clock clock(@Value("${app.timezone}") String timezone) {
        return Clock.system(ZoneId.of(timezone));
    }
}
