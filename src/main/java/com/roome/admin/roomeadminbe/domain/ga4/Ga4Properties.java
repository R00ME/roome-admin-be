package com.roome.admin.roomeadminbe.domain.ga4;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ga4")
public record Ga4Properties(String propertyId, String credentialsFile) {
}
