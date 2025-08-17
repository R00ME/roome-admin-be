package com.roome.admin.roomeadminbe.global.config;

import com.google.analytics.data.v1beta.BetaAnalyticsDataClient;
import com.google.analytics.data.v1beta.BetaAnalyticsDataSettings;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.roome.admin.roomeadminbe.global.ga4.Ga4Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableConfigurationProperties(Ga4Properties.class)
public class Ga4Config {
    private final Ga4Properties props;

    public Ga4Config(Ga4Properties props) {
        this.props = props;
    }

    @Bean
    public BetaAnalyticsDataClient gaClient() throws IOException {
        GoogleCredentials cred = GoogleCredentials
                .fromStream(ResourceUtils.getURL(props.getCredentialsFile()).openStream())
                .createScoped(List.of("https://www.googleapis.com/auth/analytics.readonly"));
        BetaAnalyticsDataSettings settings = BetaAnalyticsDataSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(cred))
                .build();
        return BetaAnalyticsDataClient.create(settings);
    }
}
