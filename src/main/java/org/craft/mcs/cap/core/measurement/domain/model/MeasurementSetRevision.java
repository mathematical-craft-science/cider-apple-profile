package org.craft.mcs.cap.core.measurement.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.measurement.domain.calculation.ArithmeticMeanMeasurementDerivation;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Immutable full representation of a measurement set at one correction revision. */
@Entity
public record MeasurementSetRevision(
    @Identity @NonNull MeasurementSetRevisionId revisionId,
    @NonNull MeasurementSetRevisionNumber revisionNumber,
    @NonNull MaterialStateId materialStateId,
    @NonNull MeasurementSetName name,
    @Nullable String note,
    @Nullable MeasurementCorrectionReason correctionReason,
    @NonNull Instant createdAt,
    @NonNull ActorReference actor,
    @NonNull List<@NonNull MeasurementRecord> records
) {

  private static final int MAX_NOTE_LENGTH = 2_000;

  public MeasurementSetRevision {
    requireNonNull(revisionId, "measurement set revision id cannot be null");
    requireNonNull(revisionNumber, "measurement set revision number cannot be null");
    requireNonNull(materialStateId, "measurement revision material state id cannot be null");
    requireNonNull(name, "measurement set name cannot be null");
    requireNonNull(createdAt, "measurement set revision creation time cannot be null");
    requireNonNull(actor, "measurement actor reference cannot be null");
    records = List.copyOf(requireNonNull(records, "measurement records cannot be null"));
    note = normalize(note);

    if (revisionNumber.isInitial() && correctionReason != null) {
      throw new DomainRuleViolationException("initial measurement revision cannot have a correction reason");
    }
    if (!revisionNumber.isInitial() && correctionReason == null) {
      throw new DomainRuleViolationException("corrected measurement revision requires a correction reason");
    }
    if (records.isEmpty()) {
      throw new DomainRuleViolationException("measurement revision must contain at least one record");
    }
    requireMaximumLength(note, MAX_NOTE_LENGTH, "measurement set note");
    validateRecordIdentities(records);
    validateResults(records);
    validateRecordChronology(records, createdAt);
    validateDerivations(records);
  }

  private static void validateRecordIdentities(@NonNull List<@NonNull MeasurementRecord> records) {
    if (records.stream().map(MeasurementRecord::recordId).distinct().count() != records.size()) {
      throw new DomainRuleViolationException("measurement record ids must be unique within a revision");
    }
  }

  private static void validateResults(@NonNull List<@NonNull MeasurementRecord> records) {
    final var resultCountByCharacteristic = records.stream()
        .filter(r -> r.role() == MeasurementRecordRole.RESULT)
        .collect(Collectors.groupingBy(MeasurementRecord::characteristic, Collectors.counting()));

    if (resultCountByCharacteristic.values().stream().anyMatch(count -> count != 1L)) {
      throw new DomainRuleViolationException(
          "measurement revision must contain exactly one result per represented characteristic"
      );
    }

    final var representedCharacteristics = records.stream()
        .map(MeasurementRecord::characteristic)
        .distinct()
        .count();
    if (resultCountByCharacteristic.size() != representedCharacteristics) {
      throw new DomainRuleViolationException(
          "each represented measurement characteristic requires one result record"
      );
    }
  }

  private static void validateDerivations(@NonNull List<@NonNull MeasurementRecord> records) {
    final Map<MeasurementRecordId, MeasurementRecord> recordsById = records.stream()
        .collect(Collectors.toUnmodifiableMap(MeasurementRecord::recordId, Function.identity()));

    for (final var result : records) {
      if (result.derivationMethod() == null) {
        continue;
      }
      final var inputs = result.derivationInputs().stream()
          .map(inputId -> requireCompatibleObservation(result, recordsById.get(inputId)))
          .toList();
      requireEveryObservationParticipates(result, records);
      requireDerivedResultTime(result, inputs);
      final var expectedValue = switch (result.derivationMethod()) {
        case ARITHMETIC_MEAN -> ArithmeticMeanMeasurementDerivation.derive(
            inputs.stream()
                .map(MeasurementRecord::value)
                .map(MeasurementSetRevision::pointValue)
                .toList()
        );
      };
      if (!expectedValue.equals(result.value())) {
        throw new DomainRuleViolationException(
            "derived measurement result must equal the declared derivation of its observations"
        );
      }
    }

    records.stream()
        .filter(measurementRecord -> measurementRecord.role() == MeasurementRecordRole.RESULT)
        .filter(result -> result.derivationMethod() == null)
        .forEach(result -> requireNoUnassociatedObservations(result, records));
  }

  private static void requireEveryObservationParticipates(
      @NonNull MeasurementRecord result,
      @NonNull List<@NonNull MeasurementRecord> records
  ) {
    final var observationIds = observationIds(result, records);
    if (!observationIds.equals(Set.copyOf(result.derivationInputs()))) {
      throw new DomainRuleViolationException(
          "derived measurement result must reference every observation of its characteristic"
      );
    }
  }

  private static void requireNoUnassociatedObservations(
      @NonNull MeasurementRecord result,
      @NonNull List<@NonNull MeasurementRecord> records
  ) {
    if (!observationIds(result, records).isEmpty()) {
      throw new DomainRuleViolationException(
          "measurement observations require a derived result"
      );
    }
  }

  private static @NonNull Set<@NonNull MeasurementRecordId> observationIds(
      @NonNull MeasurementRecord result,
      @NonNull List<@NonNull MeasurementRecord> records
  ) {
    return records.stream()
        .filter(measurementRecord -> measurementRecord.role() == MeasurementRecordRole.OBSERVATION)
        .filter(measurementRecord -> measurementRecord.characteristic() == result.characteristic())
        .map(MeasurementRecord::recordId)
        .collect(Collectors.toUnmodifiableSet());
  }

  private static void validateRecordChronology(
      @NonNull List<@NonNull MeasurementRecord> records,
      @NonNull Instant createdAt
  ) {
    if (records.stream().anyMatch(measurementRecord -> measurementRecord.measuredAt().isAfter(createdAt))) {
      throw new DomainRuleViolationException(
          "measurement record time cannot be after its revision creation time"
      );
    }
  }

  private static void requireDerivedResultTime(
      @NonNull MeasurementRecord result,
      @NonNull List<@NonNull MeasurementRecord> inputs
  ) {
    final var latestObservationTime = inputs.stream()
        .map(MeasurementRecord::measuredAt)
        .max(Instant::compareTo)
        .orElse(inputs.getFirst().measuredAt());
    if (!result.measuredAt().equals(latestObservationTime)) {
      throw new DomainRuleViolationException(
          "derived measurement result time must equal its latest observation time"
      );
    }
  }

  private static @NonNull MeasurementRecord requireCompatibleObservation(
      @NonNull MeasurementRecord result,
      @Nullable MeasurementRecord input
  ) {
    if (input == null
        || input.role() != MeasurementRecordRole.OBSERVATION
        || input.characteristic() != result.characteristic()) {
      throw new DomainRuleViolationException(
          "derived measurement inputs must reference observations of the same characteristic"
      );
    }
    return input;
  }

  private static @NonNull MeasuredValue pointValue(
      @NonNull CharacteristicValue<MeasuredValue> value
  ) {
    return switch (value) {
      case PointCharacteristicValue<MeasuredValue>(var point) -> point;
      case IntervalCharacteristicValue<MeasuredValue> _ ->
          throw new DomainRuleViolationException("derived measurement inputs must be point observations");
    };
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }
    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }

  private static void requireMaximumLength(
      @Nullable String value,
      int maximumLength,
      @NonNull String field
  ) {
    if (value != null && value.length() > maximumLength) {
      throw new DomainRuleViolationException(
          field + " length must not exceed " + maximumLength + " characters"
      );
    }
  }
}
