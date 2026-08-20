package org.craft.mcs.cap.core.measurement.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.MeasurementTestValues.value;


class MeasurementRecordTest {

  private static final Instant MEASURED_AT = Instant.parse("2026-07-20T10:00:00Z");
  private static final MeasurementProvenance PROVENANCE = new MeasurementProvenance(
      MeasurementSourceType.USER,
      MeasurementSourceName.of("Bench meter"),
      null,
      null,
      null
  );

  @Test
  void retainsPointAndIntervalValues() {
    final var recordId = MeasurementRecordId.of(newUuid());
    final var point = result(
        recordId,
        PointCharacteristicValue.of(measuredValue("3.40")),
        PROVENANCE,
        null,
        List.of()
    );
    final var interval = result(
        MeasurementRecordId.newId(),
        IntervalCharacteristicValue.of(measuredValue("3.30"), measuredValue("3.50")),
        PROVENANCE,
        null,
        List.of()
    );

    assertThat(point)
        .returns(recordId, MeasurementRecord::recordId)
        .extracting(MeasurementRecord::value)
        .isInstanceOf(PointCharacteristicValue.class);
    assertThat(interval.value()).isInstanceOf(IntervalCharacteristicValue.class);
  }

  @Test
  void observationMustBeAnUnderivedPoint() {
    final var firstInput = MeasurementRecordId.newId();
    final var secondInput = MeasurementRecordId.newId();
    final var interval = IntervalCharacteristicValue.of(measuredValue("3.30"), measuredValue("3.50"));

    assertThatThrownBy(() -> observation(interval, null, List.of()))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measurement observations must be point values");
    assertThatThrownBy(() -> observation(
        PointCharacteristicValue.of(measuredValue("3.40")),
        MeasurementDerivationMethod.ARITHMETIC_MEAN,
        List.of(firstInput, secondInput)
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measurement observation cannot declare a derivation");
    assertThatThrownBy(() -> observation(
        PointCharacteristicValue.of(measuredValue("3.40")),
        null,
        List.of(firstInput)
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measurement observation cannot reference derivation inputs");
  }

  @Test
  void derivedResultRequiresTwoUniqueInputsPointValueAndDerivedProvenance() {
    final var firstInput = MeasurementRecordId.newId();
    final var secondInput = MeasurementRecordId.newId();
    final var point = PointCharacteristicValue.of(measuredValue("3.40"));
    final var interval = IntervalCharacteristicValue.of(measuredValue("3.30"), measuredValue("3.50"));

    assertThatThrownBy(() -> result(
        MeasurementRecordId.newId(),
        point,
        MeasurementProvenance.derived(),
        MeasurementDerivationMethod.ARITHMETIC_MEAN,
        List.of(firstInput)
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("derived measurement result requires at least two observations");
    assertThatThrownBy(() -> result(
        MeasurementRecordId.newId(),
        interval,
        MeasurementProvenance.derived(),
        MeasurementDerivationMethod.ARITHMETIC_MEAN,
        List.of(firstInput, secondInput)
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("derived measurement result must be a point value");
    assertThatThrownBy(() -> result(
        MeasurementRecordId.newId(),
        point,
        PROVENANCE,
        MeasurementDerivationMethod.ARITHMETIC_MEAN,
        List.of(firstInput, secondInput)
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("derived measurement result requires DERIVED provenance");
    assertThatThrownBy(() -> result(
        MeasurementRecordId.newId(),
        point,
        MeasurementProvenance.derived(),
        MeasurementDerivationMethod.ARITHMETIC_MEAN,
        List.of(firstInput, firstInput)
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measurement derivation inputs must be unique");
  }

  @Test
  void directResultCannotDeclareDerivedProvenanceOrInputReferences() {
    final var point = PointCharacteristicValue.of(measuredValue("3.40"));
    final var input = MeasurementRecordId.newId();

    assertThatThrownBy(() -> result(
        MeasurementRecordId.newId(),
        point,
        PROVENANCE,
        null,
        List.of(input)
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measurement derivation inputs require a derivation method");
    assertThatThrownBy(() -> result(
        MeasurementRecordId.newId(),
        point,
        MeasurementProvenance.derived(),
        null,
        List.of()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("DERIVED provenance requires a measurement derivation");
  }

  @Test
  void derivedResultCannotReferenceItsOwnIdentity() {
    final var recordId = MeasurementRecordId.newId();

    assertThatThrownBy(() -> result(
        recordId,
        PointCharacteristicValue.of(measuredValue("3.40")),
        MeasurementProvenance.derived(),
        MeasurementDerivationMethod.ARITHMETIC_MEAN,
        List.of(recordId, MeasurementRecordId.newId())
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measurement result cannot reference itself as derivation evidence");
  }

  private static MeasurementRecord observation(
      CharacteristicValue<MeasuredValue> value,
      MeasurementDerivationMethod derivationMethod,
      List<MeasurementRecordId> derivationInputs
  ) {
    return new MeasurementRecord(
        MeasurementRecordId.newId(),
        MeasurementCharacteristic.PH,
        MeasurementRecordRole.OBSERVATION,
        value,
        MEASURED_AT,
        PROVENANCE,
        derivationMethod,
        derivationInputs
    );
  }

  private static MeasurementRecord result(
      MeasurementRecordId recordId,
      CharacteristicValue<MeasuredValue> value,
      MeasurementProvenance provenance,
      MeasurementDerivationMethod derivationMethod,
      List<MeasurementRecordId> derivationInputs
  ) {
    return new MeasurementRecord(
        recordId,
        MeasurementCharacteristic.PH,
        MeasurementRecordRole.RESULT,
        value,
        MEASURED_AT,
        provenance,
        derivationMethod,
        derivationInputs
    );
  }

  private static MeasuredValue measuredValue(String value) {
    return value(MeasurementCharacteristic.PH, new BigDecimal(value));
  }
}
