package com.roome.admin.roomeadminbe.domain.common.repository;

import com.roome.admin.roomeadminbe.domain.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
