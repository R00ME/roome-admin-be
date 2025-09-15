package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.roome.admin.roomeadminbe.domain.ga4.entity.GaUserStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GaUserStatRepository extends JpaRepository<GaUserStat, Long>, GaUserStatRepositoryCustom {
}
