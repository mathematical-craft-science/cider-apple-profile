package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public class BatchTraceStepJdbcRow {

  @NonNull
  public UUID stepId;

  @NonNull
  public UUID traceId;

  @Nullable
  public UUID parentStepId;

  @NonNull
  public BatchTraceOperationType operationType;

  @NonNull
  public Instant occurredAt;

  @Nullable
  public String performedBy;

  @Nullable
  public String note;

  @Nullable
  public Instant createdAt;

  @NonNull
  public List<@NonNull BatchTraceStepSubjectJdbcRow> subjects = new ArrayList<>();

  @SuppressWarnings("NullAway.Init")
  public BatchTraceStepJdbcRow() {
  }

  public BatchTraceStepJdbcRow(
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

  public void addSubject(@NonNull BatchTraceStepSubjectJdbcRow subject) {
    requireNonNull(subject, "batch trace step subject cannot be null");
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
