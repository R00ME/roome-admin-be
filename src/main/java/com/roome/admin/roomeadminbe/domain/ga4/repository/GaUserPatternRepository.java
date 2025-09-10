package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.roome.admin.roomeadminbe.domain.ga4.entity.GaUserPattern;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GaUserPatternRepository extends JpaRepository<GaUserPattern,Long>, GaUserPatternRepositoryCustom {
}
