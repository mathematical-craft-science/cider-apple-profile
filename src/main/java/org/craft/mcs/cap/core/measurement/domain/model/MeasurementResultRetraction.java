package org.craft.mcs.cap.core.measurement.domain.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Immutable tombstone recording why and by whom one concrete measurement result was retracted. */
@ValueObject
public record MeasurementResultRetraction(
    @NonNull MeasurementSetRevisionId sourceRevisionId,
    @NonNull MeasurementSetRevisionNumber sourceRevisionNumber,
    @NonNull MeasurementRecord result,
    @NonNull List<@NonNull MeasurementRecord> observations,
    @NonNull ActorReference actor,
    @NonNull Instant retractedAt,
    @NonNull MeasurementCorrectionReason reason
) {

  public MeasurementResultRetraction {
    requireNonNull(sourceRevisionId, "source measurement revision id cannot be null");
    requireNonNull(sourceRevisionNumber, "source measurement revision number cannot be null");
    requireNonNull(result, "retracted measurement result cannot be null");
    observations = List.copyOf(requireNonNull(
        observations,
        "retracted measurement observations cannot be null"
    ));
    requireNonNull(actor, "measurement retraction actor cannot be null");
    requireNonNull(retractedAt, "measurement retraction time cannot be null");
    requireNonNull(reason, "measurement retraction reason cannot be null");
    if (result.role() != MeasurementRecordRole.RESULT) {
      throw new DomainRuleViolationException(
          "only a measurement result can be retracted"
      );
    }
    validateObservations(result, observations);
  }

  private static void validateObservations(
      @NonNull MeasurementRecord result,
      @NonNull List<@NonNull MeasurementRecord> observations
  ) {
    final var observationIds = new HashSet<MeasurementRecordId>();
    for (final var observation : observations) {
      if (observation.role() != MeasurementRecordRole.OBSERVATION
          || observation.characteristic() != result.characteristic()) {
        throw new DomainRuleViolationException(
            "retracted observations must belong to the retracted result characteristic"
        );
      }
      if (!observationIds.add(observation.recordId())) {
        throw new DomainRuleViolationException(
            "retracted observation identities must be unique"
        );
      }
    }
    if (!observationIds.equals(new HashSet<>(result.derivationInputs()))) {
      throw new DomainRuleViolationException(
          "retracted observations must match the result derivation inputs"
      );
    }
  }

  /** Returns the characteristic identified by the retracted result evidence. */
  public @NonNull MeasurementCharacteristic characteristic() {
    return result.characteristic();
  }

  /** Returns the immutable evidence identity to which this tombstone applies. */
  public @NonNull MeasurementRecordId recordId() {
    return result.recordId();
  }
}
