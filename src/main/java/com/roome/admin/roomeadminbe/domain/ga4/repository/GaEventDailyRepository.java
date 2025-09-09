//package com.roome.admin.roomeadminbe.domain.ga4.repository;
//
//import com.roome.admin.roomeadminbe.domain.ga4.entity.GaEventDaily;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface GaEventDailyRepository extends JpaRepository<GaEventDaily, Long>, GaEventDailyRepositoryCustom {
//    List<GaEventDaily> findByStatDate(LocalDate date);
//
//    Optional<GaEventDaily> findByStatDateAndEventNameAndCustomUserId(
//            LocalDate statDate, String eventName, String customUserId);
//}
