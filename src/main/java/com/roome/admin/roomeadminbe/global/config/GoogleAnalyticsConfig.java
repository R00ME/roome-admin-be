//package com.roome.admin.roomeadminbe.global.config;
//
//import com.google.analytics.data.v1beta.BetaAnalyticsDataClient;
//import com.google.analytics.data.v1beta.BetaAnalyticsDataSettings;
//import com.google.api.gax.core.FixedCredentialsProvider;
//import com.google.auth.oauth2.GoogleCredentials;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.ResourceLoader;
//
//import java.io.IOException;
//
//@Configuration
//@RequiredArgsConstructor
//public class GoogleAnalyticsConfig {
//
//    @Value("${ga4.credentials-file}")
//    private String credentialsFile;
//
//    private final ResourceLoader resourceLoader;
//
//    @Bean
//    public BetaAnalyticsDataClient analyticsDataClient() throws IOException {
//        Resource resource = resourceLoader.getResource(credentialsFile);
//        GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());
//        return BetaAnalyticsDataClient.create(
//                BetaAnalyticsDataSettings.newBuilder()
//                        .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
//                        .build()
//        );
//    }
//}
