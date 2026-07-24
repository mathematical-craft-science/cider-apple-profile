package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.application.UnsupportedMeasurementUnitException;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ExactChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ExactChemicalMeasurementResponse;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ChemicalProfileRestMapperTest {

  @Test
  void convertsExternalUnitsToCanonicalDomainUnits() {
    final var profile = ChemicalProfileRestMapper.toDomain(new ChemicalProfileRequest(
        new ExactChemicalMeasurementRequest("EXACT", "°Bx", new BigDecimal("13.0")),
        new ExactChemicalMeasurementRequest("EXACT", "pH", new BigDecimal("3.55")),
        new ExactChemicalMeasurementRequest("EXACT", "g/L as lactic acid", new BigDecimal("6.0")),
        new ExactChemicalMeasurementRequest("EXACT", "g/L TAE", new BigDecimal("1.25")),
        new ExactChemicalMeasurementRequest("EXACT", "mg N/L", new BigDecimal("140"))
    ));

    assertThat(profile.sugar().orElseThrow())
        .isInstanceOfSatisfying(PointCharacteristicValue.class, measurement -> {
          final var sugar = (SugarConcentration) measurement.value();
          assertThat(sugar.specificGravity().asBigDecimal()).isBetween(
                new BigDecimal("1.052"),
                new BigDecimal("1.053")
          );
        });
    assertThat(profile.ph().orElseThrow())
        .isInstanceOfSatisfying(PointCharacteristicValue.class, measurement -> {
          final var ph = (PhValue) measurement.value();
          assertThat(ph.asBigDecimal()).isEqualByComparingTo("3.55");
        });
    assertThat(profile.titratableAcidity().orElseThrow())
        .isInstanceOfSatisfying(PointCharacteristicValue.class, measurement -> {
          final var acidity = (TitratableAcidityValue) measurement.value();
          assertThat(acidity.asBigDecimal()).isBetween(
              new BigDecimal("4.46"),
              new BigDecimal("4.47")
          );
        });
    assertThat(profile.tannin().orElseThrow())
        .isInstanceOfSatisfying(PointCharacteristicValue.class, measurement -> {
          final var tannin = (TanninValue) measurement.value();
          assertThat(tannin.asBigDecimal()).isEqualByComparingTo("1250");
        });
    assertThat(profile.yeastAssimilableNitrogen().orElseThrow())
        .isInstanceOfSatisfying(PointCharacteristicValue.class, measurement -> {
          final var yan = (YeastAssimilableNitrogenValue) measurement.value();
          assertThat(yan.asBigDecimal()).isEqualByComparingTo("140");
        });
  }

  @Test
  void returnsCanonicalUnitsInResponse() {
    final var profile = ChemicalProfileRestMapper.toDomain(new ChemicalProfileRequest(
        new ExactChemicalMeasurementRequest("EXACT", "Plato", new BigDecimal("12.0")),
        null,
        new ExactChemicalMeasurementRequest("EXACT", "g/L as malic acid", new BigDecimal("5.8")),
        new ExactChemicalMeasurementRequest("EXACT", "mg/L", new BigDecimal("950")),
        new ExactChemicalMeasurementRequest("EXACT", "ppm N", new BigDecimal("165"))
    ));

    final var response = ChemicalProfileRestMapper.toResponse(profile);

    assertThat(response.sugar())
        .isInstanceOfSatisfying(ExactChemicalMeasurementResponse.class, measurement ->
            assertThat(measurement.value().unit()).isEqualTo("SG")
        );
    assertThat(response.titratableAcidity())
        .isInstanceOfSatisfying(ExactChemicalMeasurementResponse.class, measurement ->
            assertThat(measurement.value().unit()).isEqualTo("g/L as malic acid")
        );
    assertThat(response.tannin())
        .isInstanceOfSatisfying(ExactChemicalMeasurementResponse.class, measurement ->
            assertThat(measurement.value().unit()).isEqualTo("mg/L TAE")
        );
    assertThat(response.yeastAssimilableNitrogen())
        .isInstanceOfSatisfying(ExactChemicalMeasurementResponse.class, measurement ->
            assertThat(measurement.value().unit()).isEqualTo("mg N/L")
        );
  }

  @Test
  void rejectsUnsupportedUnits() {
    assertThatThrownBy(() -> ChemicalProfileRestMapper.toDomain(new ChemicalProfileRequest(
        new ExactChemicalMeasurementRequest("EXACT", "banana", new BigDecimal("12.0")),
        null,
        null,
        null,
        null
    )))
        .isInstanceOf(UnsupportedMeasurementUnitException.class);
  }
}
