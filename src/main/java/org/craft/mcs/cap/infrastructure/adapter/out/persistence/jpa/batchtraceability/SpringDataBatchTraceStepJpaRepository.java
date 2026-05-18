package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.batchtraceability;

import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.batchtraceability.entity.BatchTraceStepJpaEntity;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.batchtraceability.entity.BatchTraceStepSubjectRole;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


interface SpringDataBatchTraceStepJpaRepository
    extends JpaRepository<BatchTraceStepJpaEntity, @NonNull UUID> {

  @Query("""
      select distinct step
      from BatchTraceStepJpaEntity step
      join step.subjects matchedSubject
      left join fetch step.subjects loadedSubject
      where matchedSubject.role = :role
        and matchedSubject.subjectType = :subjectType
        and matchedSubject.subjectId = :subjectId
      order by step.occurredAt asc, step.stepId asc
      """)
  @NonNull
  List<@NonNull BatchTraceStepJpaEntity> findBySubject(
      @Param("role") @NonNull BatchTraceStepSubjectRole role,
      @Param("subjectType") @NonNull BatchTraceSubjectType subjectType,
      @Param("subjectId") @NonNull String subjectId
  );
}
