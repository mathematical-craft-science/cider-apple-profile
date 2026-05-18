package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.batchtraceability.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static java.util.Objects.requireNonNull;


@Entity
@Table(name = "batch_trace_steps")
@EntityListeners(AuditingEntityListener.class)
public class BatchTraceStepJpaEntity {

  @Id
  @NonNull
  @Column(name = "step_id", nullable = false, updatable = false)
  UUID stepId;

  @NonNull
  @Column(name = "trace_id", nullable = false, updatable = false)
  UUID traceId;

  @Nullable
  @Column(name = "parent_step_id", updatable = false)
  UUID parentStepId;

  @NonNull
  @Enumerated(EnumType.STRING)
  @Column(name = "operation_type", nullable = false, updatable = false, length = 64)
  BatchTraceOperationType operationType;

  @NonNull
  @Column(name = "occurred_at", nullable = false, updatable = false)
  Instant occurredAt;

  @Nullable
  @Column(name = "performed_by", updatable = false, length = 160)
  String performedBy;

  @Nullable
  @Column(name = "note", updatable = false, length = 2000)
  String note;

  @NonNull
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  Instant createdAt;

  @NonNull
  @OneToMany(mappedBy = "step", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("role ASC, subjectOrder ASC")
  List<@NonNull BatchTraceStepSubjectJpaEntity> subjects = new ArrayList<>();

  @SuppressWarnings("NullAway.Init")
  protected BatchTraceStepJpaEntity() {
  }

  BatchTraceStepJpaEntity(
      @NonNull UUID stepId,
      @NonNull UUID traceId,
      @Nullable UUID parentStepId,
      @NonNull BatchTraceOperationType operationType,
      @NonNull Instant occurredAt,
      @Nullable String performedBy,
      @Nullable String note
  ) {
    this.stepId = requireNonNull(stepId, "batch trace step id cannot be null");
    this.traceId = requireNonNull(traceId, "batch trace id cannot be null");
    this.parentStepId = parentStepId;
    this.operationType = requireNonNull(operationType, "batch trace operation type cannot be null");
    this.occurredAt = requireNonNull(occurredAt, "batch trace occurred at cannot be null");
    this.performedBy = normalize(performedBy);
    this.note = normalize(note);
  }

  void addSubject(@NonNull BatchTraceStepSubjectJpaEntity subject) {
    requireNonNull(subject, "batch trace step subject cannot be null");
    subject.step = this;
    subjects.add(subject);
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
