package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceActorType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public class BatchTraceStepJdbcRow {

  @NonNull
  public UUID stepId;

  @NonNull
  public UUID workspaceId;

  @Nullable
  public UUID parentStepId;

  @NonNull
  public BatchTraceStepType stepType;

  @NonNull
  public Instant occurredAt;

  @NonNull
  public BatchTraceActorType performedByType;

  @NonNull
  public String performedById;

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
      @NonNull UUID workspaceId,
      @Nullable UUID parentStepId,
      @NonNull BatchTraceStepType stepType,
      @NonNull Instant occurredAt,
      @NonNull BatchTraceActorType performedByType,
      @NonNull String performedById,
      @Nullable String note
  ) {
    this.stepId = requireNonNull(stepId, "batch trace step id cannot be null");
    this.workspaceId = requireNonNull(workspaceId, "batch trace workspace id cannot be null");
    this.parentStepId = parentStepId;
    this.stepType = requireNonNull(stepType, "batch trace step type cannot be null");
    this.occurredAt = requireNonNull(occurredAt, "batch trace occurred at cannot be null");
    this.performedByType = requireNonNull(performedByType, "batch trace actor type cannot be null");
    this.performedById = requireNonNull(performedById, "batch trace actor id cannot be null");
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
