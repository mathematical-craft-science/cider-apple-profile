package org.craft.mcs.cap.infrastructure.adapter.in.measurement;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.measurement.value.TemperatureValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.TurbidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class MeasurementValueInputAclTest {

  private final MeasurementValueInputAcl acl = new MeasurementValueInputAcl();

  @Test
  void convertsExternalTanninUnitBeforeCreatingCanonicalDomainValue() {
    final var value = acl.point(
        MeasurementCharacteristic.TANNIN,
        new BigDecimal("1.2"),
        "g/L TAE"
    );

    assertThat(value.value())
        .isInstanceOfSatisfying(TanninValue.class, tannin -> assertThat(tannin)
            .returns(BeverageUnits.MILLIGRAM_PER_LITER_AS_TANNIC_ACID, TanninValue::unit)
            .satisfies(measurement -> assertThat(measurement.asBigDecimal())
                .isEqualByComparingTo("1200")));
  }

  @Test
  void preservesCanonicalTanninUnitWithoutAdditionalPersistenceMetadata() {
    final var value = acl.point(
        MeasurementCharacteristic.TANNIN,
        new BigDecimal("1200"),
        "mg/L TAE"
    );

    assertThat(value.value())
        .isInstanceOfSatisfying(TanninValue.class, tannin -> assertThat(tannin)
            .returns(BeverageUnits.MILLIGRAM_PER_LITER_AS_TANNIC_ACID, TanninValue::unit)
            .satisfies(measurement -> assertThat(measurement.asBigDecimal())
                .isEqualByComparingTo("1200")));
  }

  @Test
  void normalizesTemperatureAndTurbidityToUnitsDefinedByBeverageUnitSystem() {
    final var temperature = acl.point(
        MeasurementCharacteristic.TEMPERATURE,
        new BigDecimal("18.5"),
        BeverageUnits.label(BeverageUnits.CELSIUS)
    );
    final var turbidity = acl.point(
        MeasurementCharacteristic.TURBIDITY,
        new BigDecimal("80"),
        BeverageUnits.label(BeverageUnits.FTU)
    );

    assertThat(temperature.value())
        .isInstanceOfSatisfying(TemperatureValue.class, value ->
            assertThat(BeverageUnits.label(value.unit()))
                .isEqualTo(BeverageUnits.label(BeverageUnits.CELSIUS)));
    assertThat(turbidity.value())
        .isInstanceOfSatisfying(TurbidityValue.class, value ->
            assertThat(BeverageUnits.label(value.unit()))
                .isEqualTo(BeverageUnits.label(BeverageUnits.NTU)));
  }
}
