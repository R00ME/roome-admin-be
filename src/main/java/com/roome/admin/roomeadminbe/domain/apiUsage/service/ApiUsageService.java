package com.roome.admin.roomeadminbe.domain.apiUsage.service;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.ApiUsageSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.UserMostUsedDomainSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.*;
import com.roome.admin.roomeadminbe.domain.apiUsage.repository.UserApiUsageRepository;
import com.roome.admin.roomeadminbe.domain.common.dto.response.ListResponse;
import com.roome.admin.roomeadminbe.domain.common.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static com.roome.admin.roomeadminbe.domain.apiUsage.entity.QUserApiUsage.userApiUsage;
import static com.roome.admin.roomeadminbe.domain.common.entity.QUser.user;

@Service
@RequiredArgsConstructor
public class ApiUsageService {

    private final UserApiUsageRepository userApiUsageRepository;
    @Qualifier("apiCountRedisTemplate")
    private final RedisTemplate<String, Long> apiCountRedisTemplate;
    private final JPAQueryFactory jpaQueryFactory;

    @Transactional(readOnly = true)
    public ListResponse<ApiUsageResponse> getApiUsageList(ApiUsageSearchRequest request) {
        Pageable pageable = request.toPageable();

        // 1. DB 조회 (오늘 이전 데이터만)
        Page<ApiUsageResponse> dbPage =
                userApiUsageRepository.findAllBeforeDate(request, pageable);

        // 2. Redis 조회 (오늘 데이터)
        List<ApiUsageResponse> redisList = loadFromRedis(request);

        // 3. DB + Redis 합치기
        List<ApiUsageResponse> merged =
                Stream.concat(dbPage.getContent().stream(), redisList.stream())
                        .sorted(Comparator.comparing(ApiUsageResponse::getDate).reversed())
                        .toList();

        // 4. PageImpl 로 감싸기 (페이징 total 계산 포함)
        Page<ApiUsageResponse> mergedPage =
                new PageImpl<>(merged, pageable, dbPage.getTotalElements() + redisList.size());

        return ListResponse.from(mergedPage);
    }

    @Transactional(readOnly = true)
    public ListResponse<GetUserMostDomainResponse> getUsersMostUsedDomain(UserMostUsedDomainSearchRequest request) {
        Pageable pageable = request.toPageable();

        // 1. DB에서 유저별 도메인 count 조회
        List<Tuple> dbTuples = jpaQueryFactory
                .select(
                        user.id,
                        user.email,
                        user.nickname,
                        user.gender,
                        user.lastLogin,
                        user.createdAt,
                        userApiUsage.domain,
                        userApiUsage.count.sum()
                )
                .from(user)
                .leftJoin(userApiUsage).on(userApiUsage.userId.eq(user.id))
                .groupBy(user.id, userApiUsage.domain)
                .orderBy(user.id.asc(), userApiUsage.count.sum().desc())
                .fetch();

        // 2. Redis에서 오늘 데이터 조회
        LocalDate today = LocalDate.now();
        String pattern = "api_count:" + today + ":*:*";
        Set<String> keys = apiCountRedisTemplate.keys(pattern);

        Map<String, Long> redisMap = new HashMap<>();
        if (keys != null) {
            for (String key : keys) {
                Long count = apiCountRedisTemplate.opsForValue().get(key);
                String[] parts = key.split(":");
                Long userId = Long.parseLong(parts[2]);
                String uri = parts[3];
                String domain = resolveDomain(uri);

                String compositeKey = userId + "|" + domain;
                redisMap.merge(compositeKey, count, Long::sum);
            }
        }

        // 3. DB + Redis 합치기
        Map<Long, Map<String, Long>> usageMap = new HashMap<>();

        // DB 반영
        for (Tuple tuple : dbTuples) {
            Long uid = tuple.get(user.id);
            String domain = tuple.get(userApiUsage.domain);
            Long cnt = tuple.get(userApiUsage.count.sum());

            usageMap.computeIfAbsent(uid, k -> new HashMap<>())
                    .merge(domain, cnt, Long::sum);
        }

        // Redis 반영
        redisMap.forEach((key, cnt) -> {
            String[] parts = key.split("\\|");
            Long uid = Long.parseLong(parts[0]);
            String domain = parts[1];

            usageMap.computeIfAbsent(uid, k -> new HashMap<>())
                    .merge(domain, cnt, Long::sum);
        });

        // 4. 유저별 최다 사용 도메인 pick
        List<GetUserMostDomainResponse> result = new ArrayList<>();
        usageMap.forEach((uid, domainCounts) -> {
            // 최대 count 도메인 선택
            Map.Entry<String, Long> top = domainCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(Map.entry("etc", 0L));

            // User 엔티티에서 유저 정보 조회 (join 대신 필요시 캐시 가능)
            User u = jpaQueryFactory.selectFrom(user)
                    .where(user.id.eq(uid))
                    .fetchOne();

            if (u != null) {
                result.add(new GetUserMostDomainResponse(
                        new UserInfoResponse(
                                u.getId(),
                                u.getEmail(),
                                u.getNickname(),
                                u.getGender(),
                                u.getLastLogin(),
                                u.getCreatedAt()
                        ),
                        new MostUsedDomainResponse(top.getKey(), top.getValue())
                ));
            }
        });

        // 5. 페이지네이션 적용
        int start = (int) pageable.getOffset();
        if (start >= result.size()) {
            return ListResponse.from(new PageImpl<>(List.of(), pageable, result.size()));
        }
        int end = Math.min(start + pageable.getPageSize(), result.size());
        Page<GetUserMostDomainResponse> page = new PageImpl<>(
                result.subList(start, end), pageable, result.size()
        );

        return ListResponse.from(page);
    }

    @Transactional(readOnly = true)
    public UserDomainStatsResponse getUserDomainStats(Long userId, LocalDate startDate) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now();

        LocalDate thirtyDaysAgo = start.minusDays(30);
        LocalDate sixtyDaysAgo = start.minusDays(60);

        // --- 최근 30일 (DB: 오늘 제외) ---
        List<DomainCountResponse> recentFromDb =
                userApiUsageRepository.findDomainCounts(userId, thirtyDaysAgo, start.minusDays(1));

        // Redis (오늘 데이터)
        List<DomainCountResponse> todayFromRedis = loadDomainCountsFromRedis(userId, start);

        // 합치기
        Map<String, Long> mergedRecent = new HashMap<>();
        Stream.concat(recentFromDb.stream(), todayFromRedis.stream())
                .forEach(dto -> mergedRecent.merge(dto.getDomain(), dto.getCount(), Long::sum));

        List<DomainCountResponse> recentDomainCounts = mergedRecent.entrySet().stream()
                .map(e -> new DomainCountResponse(e.getKey(), e.getValue()))
                .toList();
        Long recentTotal = recentDomainCounts.stream().mapToLong(DomainCountResponse::getCount).sum();

        // --- 이전 30일 ---
        List<DomainCountResponse> prevDomainCounts =
                userApiUsageRepository.findDomainCounts(userId, sixtyDaysAgo, thirtyDaysAgo.minusDays(1));
        Long prevTotal = prevDomainCounts.stream().mapToLong(DomainCountResponse::getCount).sum();

        return new UserDomainStatsResponse(
                userId,
                new PeriodDomainStatsResponse(recentTotal, recentDomainCounts),
                new PeriodDomainStatsResponse(prevTotal, prevDomainCounts)
        );
    }

    private List<ApiUsageResponse> loadFromRedis(ApiUsageSearchRequest request) {
        LocalDate today = LocalDate.now();
        String pattern = "api_count:" + today + ":" + (request.getUserId() != null ? request.getUserId() : "*") + ":*";
        Set<String> keys = apiCountRedisTemplate.keys(pattern);

        if (keys == null || keys.isEmpty()) return List.of();

        List<ApiUsageResponse> result = new ArrayList<>();
        for (String key : keys) {
            Long count = apiCountRedisTemplate.opsForValue().get(key);
            String[] parts = key.split(":");

            Long userId = Long.parseLong(parts[2]);
            String uri = parts[3];

            result.add(new ApiUsageResponse(userId, resolveDomain(uri), uri, count, today));
        }
        return result;
    }

    // 통계 전용
    private List<DomainCountResponse> loadDomainCountsFromRedis(Long userId, LocalDate date) {
        String pattern = "api_count:" + date + ":" + userId + ":*";
        Set<String> keys = apiCountRedisTemplate.keys(pattern);

        if (keys == null || keys.isEmpty()) return List.of();

        Map<String, Long> domainCountMap = new HashMap<>();
        for (String key : keys) {
            Long count = apiCountRedisTemplate.opsForValue().get(key);
            String[] parts = key.split(":");
            String uri = parts[3];
            String domain = resolveDomain(uri);

            domainCountMap.merge(domain, count, Long::sum);
        }

        return domainCountMap.entrySet().stream()
                .map(e -> new DomainCountResponse(e.getKey(), e.getValue()))
                .toList();
    }

    private String resolveDomain(String uri) {
        // "/api/auth/login" → ["", "api", "auth", "login"]
        String[] parts = uri.split("/");
        if (parts.length >= 3) {
            return parts[2];
        }
        return "etc";
    }
}
