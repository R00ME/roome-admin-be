package com.roome.admin.roomeadminbe.domain.ga4;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ga4")
@Getter
public class Ga4Properties {
    private String propertyId;
    private String credentialsFile;

    public Ga4Properties(String propertyId, String credentialsFile) {
        this.propertyId = propertyId;
        this.credentialsFile = credentialsFile;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public String getCredentialsFile() {
        return credentialsFile;
    }
}
