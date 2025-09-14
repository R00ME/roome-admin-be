package com.roome.admin.roomeadminbe.global.init;

import com.roome.admin.roomeadminbe.domain.admin.entity.ActivationStatus;
import com.roome.admin.roomeadminbe.domain.admin.entity.Admin;
import com.roome.admin.roomeadminbe.domain.admin.entity.AdminRole;
import com.roome.admin.roomeadminbe.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

//@Configuration
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String superAdminEmail = "super@admin.com";
        String systemAdminEmail = "9noeyni9@gmail.com";
        String operationAdminEmail = "operation@admin.com";

        boolean existsSuperAdmin = adminRepository.existsByAdminEmail(superAdminEmail);

        if (!existsSuperAdmin) {
            Admin superAdmin = Admin.builder()
                    .adminEmail("super@admin.com")
                    .adminName("최고 관리자")
                    .password(passwordEncoder.encode("superAdmin1!"))
                    .adminRole(AdminRole.SUPER_ADMIN)
                    .activationStatus(ActivationStatus.ACTIVE)
                    .build();
            adminRepository.save(superAdmin);
            System.out.println("최고 관리자 등록 완료");
        }

        boolean existsSystemAdmin = adminRepository.existsByAdminEmail(systemAdminEmail);
        if (!existsSystemAdmin) {
            Admin tempSystemAdmin = Admin.builder()
                    .adminEmail(systemAdminEmail)
                    .adminName("시스템 관리자")
                    .password(passwordEncoder.encode("systemAdmin1!"))
                    .adminRole(AdminRole.SYSTEM_MANAGER)
                    .activationStatus(ActivationStatus.ACTIVE)
                    .build();
            adminRepository.save(tempSystemAdmin);
            System.out.println("시스템 운영자 등록 완료");
        }

        boolean existsOperationAdmin = adminRepository.existsByAdminEmail(operationAdminEmail);
        if (!existsOperationAdmin) {
            Admin tempOperationAdmin = Admin.builder()
                    .adminEmail(operationAdminEmail)
                    .adminName("운영 관리자")
                    .password(passwordEncoder.encode("operationAdmin1!"))
                    .adminRole(AdminRole.OPERATION_MANAGER)
                    .activationStatus(ActivationStatus.ACTIVE)
                    .build();
            adminRepository.save(tempOperationAdmin);
            System.out.println("운영 관리자 등록 완료");
        }
    }
}
