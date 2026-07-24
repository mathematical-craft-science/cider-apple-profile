package org.craft.mcs.cap.core.measurement.domain.model;

import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
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
  }

  public static @NonNull MeasurementSet create(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectReference subject,
      @NonNull MeasurementSetRevision initialRevision,
      @NonNull Instant createdAt
  ) {
    if (!initialRevision.revisionNumber().isInitial()) {
      throw new DomainConflictException("initial measurement revision number must be one");
    }
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

  public void correct(
      @NonNull MeasurementSetRevision revision,
      @NonNull Instant correctedAt
  ) {
    requireActive();
    requireNonNull(revision, "corrected measurement revision cannot be null");
    requireNonNull(correctedAt, "measurement correction time cannot be null");
    if (!revision.revisionNumber().equals(currentRevision.revisionNumber().next())) {
      throw new DomainConflictException("measurement correction must append the next revision");
    }
    currentRevision = revision;
    updatedAt = correctedAt;
  }

  public void retract(@NonNull Instant retractedAt) {
    requireActive();
    lifecycle = MeasurementSetLifecycle.RETRACTED;
    updatedAt = requireNonNull(retractedAt, "measurement retraction time cannot be null");
  }

  private void requireActive() {
    if (lifecycle != MeasurementSetLifecycle.ACTIVE) {
      throw new DomainConflictException("retracted measurement set cannot be changed");
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
