package org.craft.mcs.cap.core.measurement.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementObservationInput;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementDerivationMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecord;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceReference;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.testing.MeasurementTestValues.pointValue;


class MeasurementRecordFactoryTest {

  private static final MeasurementProvenance PROVENANCE = new MeasurementProvenance(
      MeasurementSourceType.INSTRUMENT,
      MeasurementSourceName.of("Hydrometer H-1"),
      MeasurementMethod.of("Hydrometer"),
      MeasurementSourceReference.of("instrument:H-1"),
      ConfidenceLevel.HIGH
  );

  @Test
  void retainsRepeatedObservationsAndCreatesArithmeticMeanResult() {
    final var first = observation("1.050", "2026-07-24T00:00:00Z");
    final var second = observation("1.052", "2026-07-24T00:01:00Z");
    final var input = MeasurementCharacteristicInput.repeated(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        List.of(first, second),
        MeasurementDerivationMethod.ARITHMETIC_MEAN
    );
    final var factory = new MeasurementRecordFactory();

    final var records = factory.create(List.of(input));
    final var observations = records.stream()
        .filter(measurementRecord -> measurementRecord.role() == MeasurementRecordRole.OBSERVATION)
        .toList();

    assertThat(observations)
        .hasSize(2)
        .extracting(measurementRecord -> pointValue(measurementRecord.value()).asBigDecimal())
        .containsExactly(new BigDecimal("1.050"), new BigDecimal("1.052"));
    assertThat(records)
        .filteredOn(measurementRecord -> measurementRecord.role() == MeasurementRecordRole.RESULT)
        .singleElement()
        .returns(
            MeasurementDerivationMethod.ARITHMETIC_MEAN,
            MeasurementRecord::derivationMethod
        )
        .returns(
            observations.stream().map(MeasurementRecord::recordId).toList(),
            MeasurementRecord::derivationInputs
        )
        .satisfies(result -> assertThat(pointValue(result.value()))
            .isInstanceOfSatisfying(
                SpecificGravityValue.class,
                value -> assertThat(value.asBigDecimal()).isEqualByComparingTo("1.051")
            ))
        .satisfies(result -> assertThat(result.provenance())
            .returns(MeasurementSourceType.DERIVED, MeasurementProvenance::sourceType)
            .returns(ConfidenceLevel.HIGH, MeasurementProvenance::confidence));
  }

  @Test
  void derivesWeakestConfidenceAndRetainsMissingConfidence() {
    final var highConfidence = observation(
        "1.050",
        "2026-07-24T00:00:00Z",
        ConfidenceLevel.HIGH
    );
    final var mediumConfidence = observation(
        "1.052",
        "2026-07-24T00:01:00Z",
        ConfidenceLevel.MEDIUM
    );
    final var missingConfidence = observation(
        "1.054",
        "2026-07-24T00:02:00Z",
        null
    );
    final var factory = new MeasurementRecordFactory();

    assertThat(result(factory.create(List.of(repeated(highConfidence, mediumConfidence)))))
        .extracting(MeasurementRecord::provenance)
        .returns(ConfidenceLevel.MEDIUM, MeasurementProvenance::confidence);
    assertThat(result(factory.create(List.of(repeated(highConfidence, missingConfidence)))))
        .extracting(MeasurementRecord::provenance)
        .returns(null, MeasurementProvenance::confidence);
  }

  private static @NonNull MeasurementObservationInput observation(
      @NonNull String value,
      @NonNull String measuredAt
  ) {
    return observation(value, measuredAt, PROVENANCE.confidence());
  }

  private static @NonNull MeasurementObservationInput observation(
      @NonNull String value,
      @NonNull String measuredAt,
      @Nullable ConfidenceLevel confidence
  ) {
    return new MeasurementObservationInput(
        PointCharacteristicValue.of(SpecificGravityValue.of(new BigDecimal(value))),
        Instant.parse(measuredAt),
        new MeasurementProvenance(
            PROVENANCE.sourceType(),
            PROVENANCE.sourceName(),
            PROVENANCE.method(),
            PROVENANCE.reference(),
            confidence
        )
    );
  }

  private static @NonNull MeasurementCharacteristicInput repeated(
      @NonNull MeasurementObservationInput first,
      @NonNull MeasurementObservationInput second
  ) {
    return MeasurementCharacteristicInput.repeated(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        List.of(first, second),
        MeasurementDerivationMethod.ARITHMETIC_MEAN
    );
  }

  private static @NonNull MeasurementRecord result(
      @NonNull List<@NonNull MeasurementRecord> records
  ) {
    return records.stream()
        .filter(measurementRecord -> measurementRecord.role() == MeasurementRecordRole.RESULT)
        .findFirst()
        .orElseThrow();
  }
}
