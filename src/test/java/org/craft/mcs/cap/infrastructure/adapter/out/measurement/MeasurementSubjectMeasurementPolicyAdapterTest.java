package org.craft.mcs.cap.infrastructure.adapter.out.measurement;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.application.measurement.usecase.ValidateFeedstockSpecificGravityService;
import org.craft.mcs.cap.core.measurement.application.model.DirectMeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementObservationInput;
import org.craft.mcs.cap.core.measurement.application.model.RepeatedMeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementDerivationMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class MeasurementSubjectMeasurementPolicyAdapterTest {

  private static final Instant MEASURED_AT = Instant.parse("2026-07-25T10:00:00Z");

  private final MeasurementSubjectMeasurementPolicyAdapter policy =
      new MeasurementSubjectMeasurementPolicyAdapter(new ValidateFeedstockSpecificGravityService());

  @Test
  void rejectsDrySpecificGravityForFeedstock() {
    assertThatThrownBy(() -> policy.validate(
        subject(MeasurementSubjectType.FEEDSTOCK_BATCH),
        List.of(specificGravity("0.996"))
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("greater than 1.000");
  }

  @Test
  void allowsDrySpecificGravityForProduction() {
    assertThatCode(() -> policy.validate(
        subject(MeasurementSubjectType.PRODUCTION_BATCH),
        List.of(specificGravity("0.996"))
    )).doesNotThrowAnyException();
  }

  @Test
  void rejectsFeedstockSpecificGravityWhenEitherIntervalBoundIsNotAboveOne() {
    final var value = IntervalCharacteristicValue.<MeasuredValue>of(
        SpecificGravityValue.of("1.000"),
        SpecificGravityValue.of("1.052")
    );
    final var input = new DirectMeasurementCharacteristicInput(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        value,
        MEASURED_AT,
        provenance()
    );

    assertThatThrownBy(() -> policy.validate(
        subject(MeasurementSubjectType.FEEDSTOCK_BATCH),
        List.of(input)
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("greater than 1.000");
  }

  @Test
  void rejectsFeedstockSpecificGravityWhenAnyRepeatedObservationIsNotAboveOne() {
    final var input = new RepeatedMeasurementCharacteristicInput(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        List.of(observation("1.052"), observation("0.998")),
        MeasurementDerivationMethod.ARITHMETIC_MEAN
    );

    assertThatThrownBy(() -> policy.validate(
        subject(MeasurementSubjectType.FEEDSTOCK_BATCH),
        List.of(input)
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("greater than 1.000");
  }

  private static MeasurementSubjectReference subject(MeasurementSubjectType type) {
    return new MeasurementSubjectReference(type, UuidV7Generator.newUuid());
  }

  private static DirectMeasurementCharacteristicInput specificGravity(String value) {
    return new DirectMeasurementCharacteristicInput(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        PointCharacteristicValue.of(SpecificGravityValue.of(value)),
        MEASURED_AT,
        provenance()
    );
  }

  private static MeasurementObservationInput observation(String value) {
    return new MeasurementObservationInput(
        PointCharacteristicValue.of(SpecificGravityValue.of(value)),
        MEASURED_AT,
        provenance()
    );
  }

  private static MeasurementProvenance provenance() {
    return new MeasurementProvenance(MeasurementSourceType.USER, null, null, null, null);
  }
}
