package com.roome.admin.roomeadminbe.domain.common.repository;

import com.roome.admin.roomeadminbe.domain.common.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory,Long>, PointHistoryRepositoryCustom {
}
