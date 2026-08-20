package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.FormulationTargetRequestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ProfileTargetRestMapperTest {

  @Test
  void mapsTransportCoordinatesToTypedFormulationTarget() {
    final var target = FormulationTargetRequestMapper.toTarget(
        rectangleRequest(),
        null,
        null,
        ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
        ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA
    );

    assertThat(target)
        .isNotNull()
        .extracting(FormulationTargetDefinition::sugarAcidity)
        .isInstanceOfSatisfying(
            FormulationTargetDefinition.CustomRectangleTarget.class,
            rectangle -> {
              assertThat(rectangle.specificGravity())
                  .returns(SpecificGravityValue.of("1.060"), MeasuredValueRange::min)
                  .returns(SpecificGravityValue.of("1.072"), MeasuredValueRange::max)
                  .returns(MeasuredValueRange.UpperBound.INCLUSIVE, MeasuredValueRange::upperBound);
              assertThat(rectangle.titratableAcidity())
                  .returns(TitratableAcidityValue.of("4.5"), MeasuredValueRange::min)
                  .returns(TitratableAcidityValue.of("7.5"), MeasuredValueRange::max)
                  .returns(MeasuredValueRange.UpperBound.INCLUSIVE, MeasuredValueRange::upperBound);
            }
        );
  }

  @Test
  void mapsTransportTanninRangeToTypedFormulationTarget() {
    final var target = FormulationTargetRequestMapper.toTarget(
        rectangleRequest(),
        new ProfileTargetTanninRangeRequest(new BigDecimal("800"), new BigDecimal("1800")),
        null,
        ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
        ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA
    );

    assertThat(target)
        .isNotNull()
        .extracting(FormulationTargetDefinition::tannin)
        .isNotNull()
        .returns(TanninValue.of("800"), MeasuredValueRange::min)
        .returns(TanninValue.of("1800"), MeasuredValueRange::max);
  }

  private static CustomRectangleProfileTargetRequest rectangleRequest() {
    return new CustomRectangleProfileTargetRequest(
        "CUSTOM_RECTANGLE",
        new BigDecimal("1.060"),
        new BigDecimal("1.072"),
        new BigDecimal("4.5"),
        new BigDecimal("7.5")
    );
  }
}
