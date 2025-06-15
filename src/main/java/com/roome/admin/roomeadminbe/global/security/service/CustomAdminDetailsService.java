package com.roome.admin.roomeadminbe.global.security.service;

import com.roome.admin.roomeadminbe.domain.admin.entity.ActivationStatus;
import com.roome.admin.roomeadminbe.domain.admin.entity.Admin;
import com.roome.admin.roomeadminbe.domain.admin.repository.AdminRepository;
import com.roome.admin.roomeadminbe.global.security.model.AdminDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomAdminDetailsService implements UserDetailsService {

	private final AdminRepository adminRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String adminEmail) throws UsernameNotFoundException {
		return adminRepository.findByAdminEmail(adminEmail)
				.map(this::createUser)
				.orElseThrow(() -> new UsernameNotFoundException("관리자 이메일을 찾을 수 없습니다: " + adminEmail));
	}

	private AdminDetails createUser(Admin admin) {
		if (admin.getActivationStatus() != ActivationStatus.ACTIVE) {
			throw new RuntimeException(admin.getAdminEmail() + " -> 활성화되어 있지 않습니다.");
		}

		return new AdminDetails(admin);
	}
}
