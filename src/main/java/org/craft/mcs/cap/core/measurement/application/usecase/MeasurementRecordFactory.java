package org.craft.mcs.cap.core.measurement.application.usecase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.craft.mcs.cap.core.measurement.application.model.DirectMeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementObservationInput;
import org.craft.mcs.cap.core.measurement.application.model.RepeatedMeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.domain.calculation.ArithmeticMeanMeasurementDerivation;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementDerivationMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecord;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Creates immutable direct and derived records while preserving every repeated observation. */
final class MeasurementRecordFactory {

  @NonNull List<@NonNull MeasurementRecord> create(
      @NonNull List<@NonNull MeasurementCharacteristicInput> inputs
  ) {
    requireNonNull(inputs, "measurement characteristic inputs cannot be null");
    final var represented = new HashSet<>();
    final var records = new ArrayList<MeasurementRecord>();
    for (final var input : inputs) {
      if (!represented.add(input.characteristic())) {
        throw new ApplicationValidationException(
            "measurement characteristic can be supplied only once per revision: " + input.characteristic()
        );
      }
      switch (input) {
        case DirectMeasurementCharacteristicInput(var characteristic, var value, var measuredAt, var provenance) ->
            records.add(directResult(characteristic, value, measuredAt, provenance));
        case RepeatedMeasurementCharacteristicInput(var characteristic, var observations, var derivationMethod) ->
            records.addAll(derivedRecords(characteristic, observations, derivationMethod));
      }
    }
    return List.copyOf(records);
  }

  private @NonNull MeasurementRecord directResult(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull CharacteristicValue<MeasuredValue> value,
      @NonNull Instant measuredAt,
      @NonNull MeasurementProvenance provenance
  ) {
    return new MeasurementRecord(
        MeasurementRecordId.newId(),
        characteristic,
        MeasurementRecordRole.RESULT,
        value,
        measuredAt,
        provenance,
        null,
        List.of()
    );
  }

  private @NonNull List<@NonNull MeasurementRecord> derivedRecords(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull List<@NonNull MeasurementObservationInput> observationInputs,
      @NonNull MeasurementDerivationMethod derivationMethod
  ) {
    final var observations = observationInputs.stream()
        .map(observation -> new MeasurementRecord(
            MeasurementRecordId.newId(),
            characteristic,
            MeasurementRecordRole.OBSERVATION,
            observation.value(),
            observation.measuredAt(),
            observation.provenance(),
            null,
            List.of()
        ))
        .toList();

    final var values = observations.stream()
        .map(MeasurementRecord::value)
        .map(MeasurementRecordFactory::pointValue)
        .toList();
    final var resultValue = switch (derivationMethod) {
      case ARITHMETIC_MEAN -> ArithmeticMeanMeasurementDerivation.derive(values);
    };
    final var measuredAt = observations.stream()
        .map(MeasurementRecord::measuredAt)
        .max(Instant::compareTo)
        .orElseThrow();
    final var result = new MeasurementRecord(
        MeasurementRecordId.newId(),
        characteristic,
        MeasurementRecordRole.RESULT,
        resultValue,
        measuredAt,
        MeasurementProvenance.derived(),
        derivationMethod,
        observations.stream().map(MeasurementRecord::recordId).toList()
    );

    final var records = new ArrayList<>(observations);
    records.add(result);
    return List.copyOf(records);
  }

  private static @NonNull MeasuredValue pointValue(
      @NonNull CharacteristicValue<MeasuredValue> value
  ) {
    return switch (value) {
      case PointCharacteristicValue<MeasuredValue>(var point) -> point;
      case IntervalCharacteristicValue<MeasuredValue> _ -> throw new ApplicationValidationException(
          "repeated measurement observations must be point values"
      );
    };
  }
}
