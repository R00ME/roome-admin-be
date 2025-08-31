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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ApiUsageService {

    private static final List<String> FIXED_DOMAINS = List.of(
            "cd", "book", "room", "roomVisit", "mates", "comment", "guestbook"
    );
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
                        user.status,
                        userApiUsage.domain,
                        userApiUsage.count.sum()
                )
                .from(user)
                .leftJoin(userApiUsage).on(userApiUsage.userId.eq(user.id))
                .groupBy(user.id, userApiUsage.domain)
                .orderBy(user.id.asc(), userApiUsage.count.sum().desc())
                .fetch();

        for (Tuple tuple : dbTuples) {
            log.info("DB tuple -> uid={}, email={}, domain={}, cnt={}",
                    tuple.get(user.id),
                    tuple.get(user.email),
                    tuple.get(userApiUsage.domain),
                    tuple.get(userApiUsage.count.sum()));
        }

        // 2. Redis에서 오늘 데이터 조회
        LocalDate today = LocalDate.now();
        String pattern = "api_count:" + today + ":*:*";
        Set<String> keys = apiCountRedisTemplate.keys(pattern);

        Map<String, Long> redisMap = new HashMap<>();
        if (keys != null) {
            for (String key : keys) {
                try {
                    Long count = apiCountRedisTemplate.opsForValue().get(key);
                    String[] parts = key.split(":");

                    if (parts.length < 4) {
                        continue;
                    }

                    Long userId = Long.parseLong(parts[2]);
                    String uri = parts[3];
                    String domain = resolveDomain(uri);

                    String compositeKey = userId + "|" + domain;
                    redisMap.merge(compositeKey, count != null ? count : 0L, Long::sum);
                } catch (Exception e) {
                    log.error("Error parsing Redis key: {}", key, e);
                }
            }
        }

        // 3. DB + Redis 합치기
        Map<Long, Map<String, Long>> usageMap = new HashMap<>();

        // DB 반영
        for (Tuple tuple : dbTuples) {
            Long uid = tuple.get(user.id);
            String domain = tuple.get(userApiUsage.domain);
            Number cntNum = tuple.get(userApiUsage.count.sum());
            Long cnt = cntNum != null ? cntNum.longValue() : 0L;

            usageMap.computeIfAbsent(uid, k -> new HashMap<>())
                    .merge(domain, cnt, Long::sum);
        }

        // Redis 반영
        redisMap.forEach((key, cnt) -> {
            String[] parts = key.split("\\|");
            if (parts.length < 2) {
                log.warn("Invalid compositeKey format: {}", key);
                return;
            }
            Long uid = Long.parseLong(parts[0]);
            String domain = parts[1];

            usageMap.computeIfAbsent(uid, k -> new HashMap<>())
                    .merge(domain, cnt, Long::sum);

            log.info("Redis merged -> uid={}, domain={}, cnt={}", uid, domain, cnt);
        });

        // 4. 유저별 최다 사용 도메인 pick
        List<GetUserMostDomainResponse> result = new ArrayList<>();
        usageMap.forEach((uid, domainCounts) -> {
            Map.Entry<String, Long> top = domainCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(Map.entry("etc", 0L));

            User u = jpaQueryFactory.selectFrom(user)
                    .where(user.id.eq(uid))
                    .fetchFirst(); // fetchOne → fetchFirst로 안정화

            if (u != null) {
                log.info("Picked top domain -> uid={}, nickname={}, domain={}, cnt={}",
                        u.getId(), u.getNickname(), top.getKey(), top.getValue());

                result.add(new GetUserMostDomainResponse(
                        new UserInfoResponse(
                                u.getId(),
                                u.getEmail(),
                                u.getNickname(),
                                u.getGender(),
                                u.getLastLogin(),
                                u.getCreatedAt(),
                                u.getStatus()
                        ),
                        new MostUsedDomainResponse(top.getKey(), top.getValue())
                ));
            } else {
                log.warn("User not found for uid={}", uid);
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

        MergedResult recent = mergeStats(userId, 30, start);
        MergedResult ninety = mergeStats(userId, 90, start);

        return new UserDomainStatsResponse(
                userId,
                new PeriodDomainStatsResponse(recent.total(), recent.domainCounts()),
                new PeriodDomainStatsResponse(ninety.total(), ninety.domainCounts())
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
        String[] parts = uri.split("/");
        if (parts.length >= 3) {
            return parts[2];
        }
        return "etc";
    }

    private MergedResult mergeStats(Long userId, long days, LocalDate start) {
        List<DomainCountResponse> fromDb =
                userApiUsageRepository.findDomainCounts(userId, start.minusDays(days), start);

        List<DomainCountResponse> todayFromRedis = loadDomainCountsFromRedis(userId, start);
        Map<String, Long> merged = new HashMap<>();
        Stream.concat(fromDb.stream(), todayFromRedis.stream())
                .forEach(dto -> merged.merge(dto.getDomain(), dto.getCount(), Long::sum));

        long total = merged.values().stream().mapToLong(Long::longValue).sum();

        List<DomainCountResponse> domainCounts = FIXED_DOMAINS.stream()
                .map(domain -> new DomainCountResponse(domain, merged.getOrDefault(domain, 0L), total))
                .toList();

        return new MergedResult(total, domainCounts);
    }

    private record MergedResult(long total, List<DomainCountResponse> domainCounts) {
    }
}
