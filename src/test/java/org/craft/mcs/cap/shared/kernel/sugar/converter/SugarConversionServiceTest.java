package org.craft.mcs.cap.shared.kernel.sugar.converter;

import org.craft.mcs.cap.shared.kernel.sugar.BrixValue;
import org.craft.mcs.cap.shared.kernel.sugar.PlatoValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class SugarConversionServiceTest {

  private final SugarConversionService service = BuiltInSugarConversions.defaultService();

  @Test
  void convertsBrixToSpecificGravity() {
    final var specificGravity = service.convert(BrixValue.of("13.0"), SpecificGravityValue.class);

    assertThat(specificGravity.asBigDecimal()).isBetween(
        SpecificGravityValue.of("1.052").asBigDecimal(),
        SpecificGravityValue.of("1.054").asBigDecimal()
    );
  }

  @Test
  void convertsSpecificGravityToBrix() {
    final var brix = service.convert(SpecificGravityValue.of("1.052"), BrixValue.class);

    assertThat(brix.asBigDecimal()).isBetween(
        BrixValue.of("12.8").asBigDecimal(),
        BrixValue.of("13.1").asBigDecimal()
    );
  }

  @Test
  void convertsBetweenBrixAndPlatoThroughBuiltInStrategies() {
    final var plato = service.convert(BrixValue.of("13.0"), PlatoValue.class);
    final var brix = service.convert(plato, BrixValue.class);

    assertThat(plato.asBigDecimal()).isBetween(
        PlatoValue.of("12.0").asBigDecimal(),
        PlatoValue.of("14.0").asBigDecimal()
    );
    assertThat(brix.asBigDecimal()).isBetween(
        BrixValue.of("12.8").asBigDecimal(),
        BrixValue.of("13.2").asBigDecimal()
    );
  }
}
