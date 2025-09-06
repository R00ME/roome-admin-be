package com.roome.admin.roomeadminbe.domain.event.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.admin.roomeadminbe.domain.common.dto.request.ListRequest;
import com.roome.admin.roomeadminbe.domain.event.dto.EventListResponseDTO;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.roome.admin.roomeadminbe.domain.event.entity.QFirstComeEvent.*;

@RequiredArgsConstructor
public class FirstComeEventRepositoryImpl implements FirstComeEventRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    // 이벤트 목록 조회 - totalPage 조회
    @Override
    public Long selectTotalPagesByEvents() {
        return jpaQueryFactory.select(firstComeEvent.id.count())
                .from(firstComeEvent)
                .fetchOne();
    }

    @Override
    public List<EventListResponseDTO> selectAllByFirstComeEvent(ListRequest listRequest) {
        return jpaQueryFactory.select(
                        Projections.constructor(EventListResponseDTO.class,
                                firstComeEvent.id,
                                firstComeEvent.eventName,
                                firstComeEvent.eventReceiverTarget,
                                firstComeEvent.eventUploadTime,
                                firstComeEvent.eventContent,
                                firstComeEvent.eventTime,
                                firstComeEvent.eventWriter)
                )
                .from(firstComeEvent)
                .orderBy(firstComeEvent.id.desc())
                .offset((long) (listRequest.getPage() - 1)* listRequest.getPageSize())
                .limit(listRequest.getPageSize())
                .fetch();
    }

    // 이벤트 삭제
    @Override
    public void deleteFirstComeEventByEventId(Long eventId) {
        jpaQueryFactory.delete(firstComeEvent)
                .where(firstComeEvent.id.eq(eventId))
                .execute();
    }
}
