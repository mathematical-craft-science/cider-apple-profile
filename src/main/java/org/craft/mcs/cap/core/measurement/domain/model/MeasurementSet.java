package org.craft.mcs.cap.core.measurement.domain.model;

import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
  private @NonNull MeasurementSetRevision currentRevision;
  private final @NonNull Instant createdAt;

  private MeasurementSet(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectReference subject,
      @NonNull MeasurementSetRevision currentRevision,
      @NonNull Instant createdAt
  ) {
    this.measurementSetId = requireNonNull(measurementSetId, "measurement set id cannot be null");
    this.workspaceId = requireNonNull(workspaceId, "measurement workspace id cannot be null");
    this.subject = requireNonNull(subject, "measurement subject cannot be null");
    this.currentRevision = requireNonNull(currentRevision, "current measurement revision cannot be null");
    this.createdAt = requireNonNull(createdAt, "measurement set created at cannot be null");
    validateChronology(currentRevision, createdAt);
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
    if (initialRevision.changeKind() != MeasurementSetRevisionChangeKind.INITIAL) {
      throw new DomainRuleViolationException("measurement set must start with an initial revision");
    }
    final var createdAt = initialRevision.createdAt();
    return new MeasurementSet(
        measurementSetId,
        workspaceId,
        subject,
        initialRevision,
        createdAt
    );
  }

  public static @NonNull MeasurementSet restore(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectReference subject,
      @NonNull MeasurementSetRevision currentRevision,
      @NonNull Instant createdAt
  ) {
    return new MeasurementSet(
        measurementSetId,
        workspaceId,
        subject,
        currentRevision,
        createdAt
    );
  }

  public void correct(@NonNull MeasurementSetRevision revision) {
    requireActive();
    requireNonNull(revision, "corrected measurement revision cannot be null");
    requireNextRevision(revision);
    if (revision.changeKind() != MeasurementSetRevisionChangeKind.CORRECTION) {
      throw new DomainConflictException("measurement correction requires a correction revision");
    }
    MeasurementSetRevisionTransitionPolicy.requireValid(currentRevision, revision);
    currentRevision = revision;
  }

  public void retract(@NonNull MeasurementSetRevision revision) {
    requireActive();
    requireNonNull(revision, "measurement retraction revision cannot be null");
    requireNextRevision(revision);
    if (revision.changeKind() != MeasurementSetRevisionChangeKind.RETRACTION) {
      throw new DomainConflictException("measurement retraction requires a retraction revision");
    }
    MeasurementSetRevisionTransitionPolicy.requireRetraction(currentRevision, revision);
    currentRevision = revision;
  }

  private void requireNextRevision(@NonNull MeasurementSetRevision revision) {
    if (revision.revisionId().equals(currentRevision.revisionId())) {
      throw new DomainConflictException("measurement change requires a new revision identity");
    }
    if (!revision.revisionNumber().equals(currentRevision.revisionNumber().next())) {
      throw new DomainConflictException("measurement change must append the next revision");
    }
    if (!revision.materialStateId().equals(currentRevision.materialStateId())) {
      throw new DomainConflictException("measurement change must preserve the observed material state");
    }
    if (revision.createdAt().isBefore(currentRevision.createdAt())) {
      throw new DomainConflictException("measurement change cannot predate the current revision");
    }
  }

  private void requireActive() {
    if (lifecycle() != MeasurementSetLifecycle.ACTIVE) {
      throw new DomainConflictException("retracted measurement set cannot be changed");
    }
  }

  private static void validateChronology(
      @NonNull MeasurementSetRevision currentRevision,
      @NonNull Instant createdAt
  ) {
    if (currentRevision.createdAt().isBefore(createdAt)) {
      throw new DomainRuleViolationException(
          "current measurement revision cannot predate measurement set creation"
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
    return currentRevision.lifecycle();
  }

  public @Nullable MeasurementSetRetraction retraction() {
    return currentRevision.changeKind() == MeasurementSetRevisionChangeKind.RETRACTION
        ? new MeasurementSetRetraction(
            currentRevision.actor(),
            currentRevision.createdAt(),
            requireNonNull(currentRevision.retractionReason())
        )
        : null;
  }

  public @NonNull MeasurementSetRevision currentRevision() {
    return currentRevision;
  }

  public @NonNull Instant createdAt() {
    return createdAt;
  }

  public @NonNull Instant updatedAt() {
    return currentRevision.createdAt();
  }
}
