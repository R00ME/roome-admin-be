package com.roome.admin.roomeadminbe.domain.ga4.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface GaFeatureStatRepositoryCustom {
    String getContentValue();

    String getContentChangeRate();

    String getMostUsedFeature();

    String getMostDroppedFeature();
}
