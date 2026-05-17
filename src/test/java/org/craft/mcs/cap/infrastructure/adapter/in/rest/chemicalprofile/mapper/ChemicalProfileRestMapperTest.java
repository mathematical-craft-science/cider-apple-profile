package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.UnsupportedChemicalMeasurementUnitException;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ExactChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ExactChemicalMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.StatisticalChemicalMeasurementRequest;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.StatisticalMeasurement;
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
        "lab",
        null
    ));

    assertThat(profile.sugar().orElseThrow())
        .isInstanceOfSatisfying(ExactMeasurement.class, measurement -> {
          final var sugar = (SugarConcentration) measurement.value();
          assertThat(sugar.specificGravity().asBigDecimal()).isBetween(
                new BigDecimal("1.052"),
                new BigDecimal("1.053")
          );
        });
    assertThat(profile.ph().orElseThrow())
        .isInstanceOfSatisfying(ExactMeasurement.class, measurement -> {
          final var ph = (PhValue) measurement.value();
          assertThat(ph.asBigDecimal()).isEqualByComparingTo("3.55");
        });
    assertThat(profile.titratableAcidity().orElseThrow())
        .isInstanceOfSatisfying(ExactMeasurement.class, measurement -> {
          final var acidity = (TitratableAcidityValue) measurement.value();
          assertThat(acidity.asBigDecimal()).isBetween(
              new BigDecimal("4.46"),
              new BigDecimal("4.47")
          );
        });
    assertThat(profile.tanninity().orElseThrow())
        .isInstanceOfSatisfying(ExactMeasurement.class, measurement -> {
          final var tanninity = (TanninityValue) measurement.value();
          assertThat(tanninity.asBigDecimal()).isEqualByComparingTo("1250");
        });
  }

  @Test
  void convertsStatisticalStandardDeviationToCanonicalScale() {
    final var profile = ChemicalProfileRestMapper.toDomain(new ChemicalProfileRequest(
        new StatisticalChemicalMeasurementRequest(
            "STATISTICAL",
            "Brix",
            new BigDecimal("12.0"),
            null,
            new BigDecimal("0.5"),
            4
        ),
        null,
        null,
        null,
        null,
        null
    ));

    assertThat(profile.sugar().orElseThrow())
        .isInstanceOfSatisfying(StatisticalMeasurement.class, measurement ->
            assertThat(measurement.standardDeviation()).isNotNull()
        );
  }

  @Test
  void returnsCanonicalUnitsInResponse() {
    final var profile = ChemicalProfileRestMapper.toDomain(new ChemicalProfileRequest(
        new ExactChemicalMeasurementRequest("EXACT", "Plato", new BigDecimal("12.0")),
        null,
        new ExactChemicalMeasurementRequest("EXACT", "g/L as malic acid", new BigDecimal("5.8")),
        new ExactChemicalMeasurementRequest("EXACT", "mg/L", new BigDecimal("950")),
        null,
        null
    ));

    final var response = ChemicalProfileRestMapper.toResponse(profile);

    assertThat(response.sugar())
        .isInstanceOfSatisfying(ExactChemicalMeasurementResponse.class, measurement ->
            assertThat(measurement.unit()).isEqualTo("SG")
        );
    assertThat(response.titratableAcidity())
        .isInstanceOfSatisfying(ExactChemicalMeasurementResponse.class, measurement ->
            assertThat(measurement.unit()).isEqualTo("g/L as malic acid")
        );
    assertThat(response.tanninity())
        .isInstanceOfSatisfying(ExactChemicalMeasurementResponse.class, measurement ->
            assertThat(measurement.unit()).isEqualTo("mg/L TAE")
        );
  }

  @Test
  void rejectsUnsupportedUnits() {
    assertThatThrownBy(() -> ChemicalProfileRestMapper.toDomain(new ChemicalProfileRequest(
        new ExactChemicalMeasurementRequest("EXACT", "banana", new BigDecimal("12.0")),
        null,
        null,
        null,
        null,
        null
    )))
        .isInstanceOf(UnsupportedChemicalMeasurementUnitException.class);
  }
}
