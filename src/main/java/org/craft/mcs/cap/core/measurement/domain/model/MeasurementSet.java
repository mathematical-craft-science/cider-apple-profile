package org.craft.mcs.cap.core.measurement.domain.model;

import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
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
  private @NonNull MeasurementSetLifecycle lifecycle;
  private @Nullable MeasurementSetRetraction retraction;
  private @NonNull MeasurementSetRevision currentRevision;
  private final @NonNull Instant createdAt;
  private @NonNull Instant updatedAt;

  @SuppressWarnings("java:S107")
  private MeasurementSet(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectReference subject,
      @NonNull MeasurementSetLifecycle lifecycle,
      @Nullable MeasurementSetRetraction retraction,
      @NonNull MeasurementSetRevision currentRevision,
      @NonNull Instant createdAt,
      @NonNull Instant updatedAt
  ) {
    this.measurementSetId = requireNonNull(measurementSetId, "measurement set id cannot be null");
    this.workspaceId = requireNonNull(workspaceId, "measurement workspace id cannot be null");
    this.subject = requireNonNull(subject, "measurement subject cannot be null");
    this.lifecycle = requireNonNull(lifecycle, "measurement set lifecycle cannot be null");
    this.retraction = retraction;
    this.currentRevision = requireNonNull(currentRevision, "current measurement revision cannot be null");
    this.createdAt = requireNonNull(createdAt, "measurement set created at cannot be null");
    this.updatedAt = requireNonNull(updatedAt, "measurement set updated at cannot be null");
    validateLifecycle(lifecycle, retraction, updatedAt);
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
        null,
        initialRevision,
        createdAt,
        createdAt
    );
  }

  @SuppressWarnings("java:S107")
  public static @NonNull MeasurementSet restore(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectReference subject,
      @NonNull MeasurementSetLifecycle lifecycle,
      @Nullable MeasurementSetRetraction retraction,
      @NonNull MeasurementSetRevision currentRevision,
      @NonNull Instant createdAt,
      @NonNull Instant updatedAt
  ) {
    return new MeasurementSet(
        measurementSetId,
        workspaceId,
        subject,
        lifecycle,
        retraction,
        currentRevision,
        createdAt,
        updatedAt
    );
  }

  public void correct(@NonNull MeasurementSetRevision revision) {
    requireActive();
    requireNonNull(revision, "corrected measurement revision cannot be null");
    if (revision.revisionId().equals(currentRevision.revisionId())) {
      throw new DomainConflictException("measurement correction requires a new revision identity");
    }
    if (!revision.revisionNumber().equals(currentRevision.revisionNumber().next())) {
      throw new DomainConflictException("measurement correction must append the next revision");
    }
    if (!revision.materialStateId().equals(currentRevision.materialStateId())) {
      throw new DomainConflictException("measurement correction must preserve the observed material state");
    }
    if (revision.createdAt().isBefore(updatedAt)) {
      throw new DomainConflictException("measurement correction cannot predate the current aggregate state");
    }
    MeasurementSetRevisionTransitionPolicy.requireValid(currentRevision, revision);
    currentRevision = revision;
    updatedAt = revision.createdAt();
  }

  public void retract(
      @NonNull ActorReference actor,
      @NonNull Instant retractedAt,
      @NonNull MeasurementRetractionReason reason
  ) {
    requireActive();
    requireNonNull(actor, "measurement-set retraction actor cannot be null");
    requireNonNull(retractedAt, "measurement retraction time cannot be null");
    requireNonNull(reason, "measurement-set retraction reason cannot be null");
    if (retractedAt.isBefore(updatedAt)) {
      throw new DomainConflictException("measurement retraction cannot predate the current aggregate state");
    }
    lifecycle = MeasurementSetLifecycle.RETRACTED;
    retraction = new MeasurementSetRetraction(actor, retractedAt, reason);
    updatedAt = retractedAt;
  }

  private static void validateLifecycle(
      @NonNull MeasurementSetLifecycle lifecycle,
      @Nullable MeasurementSetRetraction retraction,
      @NonNull Instant updatedAt
  ) {
    if (lifecycle == MeasurementSetLifecycle.ACTIVE && retraction != null) {
      throw new DomainRuleViolationException("active measurement set cannot have retraction audit");
    }
    if (lifecycle == MeasurementSetLifecycle.RETRACTED && retraction == null) {
      throw new DomainRuleViolationException("retracted measurement set requires retraction audit");
    }
    if (retraction != null && !retraction.retractedAt().equals(updatedAt)) {
      throw new DomainRuleViolationException(
          "measurement-set retraction time must equal aggregate update time"
      );
    }
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

  public @Nullable MeasurementSetRetraction retraction() {
    return retraction;
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
