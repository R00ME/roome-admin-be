package com.roome.admin.roomeadminbe.domain.event.repository;

import com.roome.admin.roomeadminbe.domain.event.entity.FirstComeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FirstComeEventRepository extends JpaRepository<FirstComeEvent, Long>, FirstComeEventRepositoryCustom {

}

