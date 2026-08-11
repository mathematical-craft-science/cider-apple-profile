package org.craft.mcs.cap.core.measurement.domain.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Immutable evidence for one characteristic within one measurement-set revision. */
@Entity
public record MeasurementRecord(
    @Identity @NonNull MeasurementRecordId recordId,
    @NonNull MeasurementCharacteristic characteristic,
    @NonNull MeasurementRecordRole role,
    @NonNull CharacteristicValue<MeasuredValue> value,
    @NonNull Instant measuredAt,
    @NonNull MeasurementProvenance provenance,
    @Nullable MeasurementDerivationMethod derivationMethod,
    @NonNull List<@NonNull MeasurementRecordId> derivationInputs
) {

  public MeasurementRecord {
    requireNonNull(recordId, "measurement record id cannot be null");
    requireNonNull(characteristic, "measurement characteristic cannot be null");
    requireNonNull(role, "measurement record role cannot be null");
    requireNonNull(value, "measurement record value cannot be null");
    requireNonNull(measuredAt, "measurement record measured at cannot be null");
    requireNonNull(provenance, "measurement provenance cannot be null");
    derivationInputs = List.copyOf(requireNonNull(
        derivationInputs,
        "measurement derivation inputs cannot be null"
    ));

    validateObservation(role, value, derivationMethod, derivationInputs);
    validateValueCompatibility(characteristic, value);
    validateDerivation(value, provenance, derivationMethod, derivationInputs);
    validateUniqueDerivationInputs(derivationInputs);
    validateNoSelfReference(recordId, derivationInputs);
  }

  /** Returns the point-or-interval shape retained by this measurement record. */
  public @NonNull MeasurementValueKind valueKind() {
    return valueKind(value);
  }

  private static @NonNull MeasurementValueKind valueKind(
      @NonNull CharacteristicValue<MeasuredValue> value
  ) {
    return switch (value) {
      case PointCharacteristicValue<MeasuredValue> _ -> MeasurementValueKind.POINT;
      case IntervalCharacteristicValue<MeasuredValue> _ -> MeasurementValueKind.INTERVAL;
    };
  }

  private static void validateObservation(
      @NonNull MeasurementRecordRole role,
      @NonNull CharacteristicValue<MeasuredValue> value,
      @Nullable MeasurementDerivationMethod derivationMethod,
      @NonNull List<@NonNull MeasurementRecordId> derivationInputs
  ) {
    if (role != MeasurementRecordRole.OBSERVATION) {
      return;
    }
    if (valueKind(value) != MeasurementValueKind.POINT) {
      throw new DomainRuleViolationException("measurement observations must be point values");
    }
    if (derivationMethod != null) {
      throw new DomainRuleViolationException("measurement observation cannot declare a derivation");
    }
    if (!derivationInputs.isEmpty()) {
      throw new DomainRuleViolationException("measurement observation cannot reference derivation inputs");
    }
  }

  private static void validateValueCompatibility(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull CharacteristicValue<MeasuredValue> value
  ) {
    if (!characteristic.supports(value)) {
      throw new DomainRuleViolationException("measurement value type does not match " + characteristic);
    }
  }

  private static void validateDerivation(
      @NonNull CharacteristicValue<MeasuredValue> value,
      @NonNull MeasurementProvenance provenance,
      @Nullable MeasurementDerivationMethod derivationMethod,
      @NonNull List<@NonNull MeasurementRecordId> derivationInputs
  ) {
    if (derivationMethod == null) {
      validateAbsentDerivation(provenance, derivationInputs);
      return;
    }
    if (derivationInputs.size() < 2) {
      throw new DomainRuleViolationException("derived measurement result requires at least two observations");
    }
    if (valueKind(value) != MeasurementValueKind.POINT) {
      throw new DomainRuleViolationException("derived measurement result must be a point value");
    }
    if (provenance.sourceType() != MeasurementSourceType.DERIVED) {
      throw new DomainRuleViolationException("derived measurement result requires DERIVED provenance");
    }
  }

  private static void validateAbsentDerivation(
      @NonNull MeasurementProvenance provenance,
      @NonNull List<@NonNull MeasurementRecordId> derivationInputs
  ) {
    if (!derivationInputs.isEmpty()) {
      throw new DomainRuleViolationException("measurement derivation inputs require a derivation method");
    }
    if (provenance.sourceType() == MeasurementSourceType.DERIVED) {
      throw new DomainRuleViolationException("DERIVED provenance requires a measurement derivation");
    }
  }

  private static void validateUniqueDerivationInputs(
      @NonNull List<@NonNull MeasurementRecordId> derivationInputs
  ) {
    final var uniqueInputs = new HashSet<MeasurementRecordId>();
    for (final var derivationInput : derivationInputs) {
      if (!uniqueInputs.add(derivationInput)) {
        throw new DomainRuleViolationException("measurement derivation inputs must be unique");
      }
    }
  }

  private static void validateNoSelfReference(
      @NonNull MeasurementRecordId recordId,
      @NonNull List<@NonNull MeasurementRecordId> derivationInputs
  ) {
    if (derivationInputs.contains(recordId)) {
      throw new DomainRuleViolationException(
          "measurement result cannot reference itself as derivation evidence"
      );
    }
  }
}
