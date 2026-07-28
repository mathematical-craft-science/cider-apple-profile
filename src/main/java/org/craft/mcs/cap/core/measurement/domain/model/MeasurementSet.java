package org.craft.mcs.cap.core.measurement.domain.model;

import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Logical measurement aggregate whose immutable revisions preserve corrections and evidence.
 */
@AggregateRoot
public final class MeasurementSet {

  @Identity
  private final @NonNull MeasurementSetId measurementSetId;
  private final @NonNull WorkspaceId workspaceId;
  private final @NonNull MeasurementSubjectReference subject;
  private @NonNull MeasurementSetLifecycle lifecycle;
  private @NonNull MeasurementSetRevision currentRevision;
  private final @NonNull Instant createdAt;
  private @NonNull Instant updatedAt;

  private MeasurementSet(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectReference subject,
      @NonNull MeasurementSetLifecycle lifecycle,
      @NonNull MeasurementSetRevision currentRevision,
      @NonNull Instant createdAt,
      @NonNull Instant updatedAt
  ) {
    this.measurementSetId = requireNonNull(measurementSetId, "measurement set id cannot be null");
    this.workspaceId = requireNonNull(workspaceId, "measurement workspace id cannot be null");
    this.subject = requireNonNull(subject, "measurement subject cannot be null");
    this.lifecycle = requireNonNull(lifecycle, "measurement set lifecycle cannot be null");
    this.currentRevision = requireNonNull(currentRevision, "current measurement revision cannot be null");
    this.createdAt = requireNonNull(createdAt, "measurement set created at cannot be null");
    this.updatedAt = requireNonNull(updatedAt, "measurement set updated at cannot be null");
    validateChronology(currentRevision, createdAt, updatedAt);
  }

  public static @NonNull MeasurementSet create(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectReference subject,
      @NonNull MeasurementSetRevision initialRevision
  ) {
    requireNonNull(initialRevision, "initial measurement revision cannot be null");
    if (!initialRevision.revisionNumber().isInitial()) {
      throw new DomainRuleViolationException("initial measurement revision number must be one");
    }
    final var createdAt = initialRevision.createdAt();
    return new MeasurementSet(
        measurementSetId,
        workspaceId,
        subject,
        MeasurementSetLifecycle.ACTIVE,
        initialRevision,
        createdAt,
        createdAt
    );
  }

  public static @NonNull MeasurementSet restore(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectReference subject,
      @NonNull MeasurementSetLifecycle lifecycle,
      @NonNull MeasurementSetRevision currentRevision,
      @NonNull Instant createdAt,
      @NonNull Instant updatedAt
  ) {
    return new MeasurementSet(
        measurementSetId,
        workspaceId,
        subject,
        lifecycle,
        currentRevision,
        createdAt,
        updatedAt
    );
  }

  public void correct(@NonNull MeasurementSetRevision revision) {
    requireActive();
    requireNonNull(revision, "corrected measurement revision cannot be null");
    if (!revision.revisionNumber().equals(currentRevision.revisionNumber().next())) {
      throw new DomainConflictException("measurement correction must append the next revision");
    }
    if (!revision.materialStateId().equals(currentRevision.materialStateId())) {
      throw new DomainConflictException("measurement correction must preserve the observed material state");
    }
    if (revision.createdAt().isBefore(updatedAt)) {
      throw new DomainConflictException("measurement correction cannot predate the current aggregate state");
    }
    currentRevision = revision;
    updatedAt = revision.createdAt();
  }

  public void retract(@NonNull Instant retractedAt) {
    requireActive();
    requireNonNull(retractedAt, "measurement retraction time cannot be null");
    if (retractedAt.isBefore(updatedAt)) {
      throw new DomainConflictException("measurement retraction cannot predate the current aggregate state");
    }
    lifecycle = MeasurementSetLifecycle.RETRACTED;
    updatedAt = retractedAt;
  }

  private void requireActive() {
    if (lifecycle != MeasurementSetLifecycle.ACTIVE) {
      throw new DomainConflictException("retracted measurement set cannot be changed");
    }
  }

  private static void validateChronology(
      @NonNull MeasurementSetRevision currentRevision,
      @NonNull Instant createdAt,
      @NonNull Instant updatedAt
  ) {
    if (createdAt.isAfter(updatedAt)) {
      throw new DomainRuleViolationException("measurement set creation time cannot follow its update time");
    }
    if (currentRevision.createdAt().isBefore(createdAt)
        || currentRevision.createdAt().isAfter(updatedAt)) {
      throw new DomainRuleViolationException(
          "current measurement revision time must belong to the aggregate lifetime"
      );
    }
  }

  public @NonNull MeasurementSetId measurementSetId() {
    return measurementSetId;
  }

  public @NonNull WorkspaceId workspaceId() {
    return workspaceId;
  }

  public @NonNull MeasurementSubjectReference subject() {
    return subject;
  }

  public @NonNull MeasurementSetLifecycle lifecycle() {
    return lifecycle;
  }

  public @NonNull MeasurementSetRevision currentRevision() {
    return currentRevision;
  }

  public @NonNull Instant createdAt() {
    return createdAt;
  }

  public @NonNull Instant updatedAt() {
    return updatedAt;
  }
}
