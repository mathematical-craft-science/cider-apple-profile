package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.TanninTargetRange;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ProfileTargetRestMapperTest {

  @Test
  void mapsTransportCoordinatesToTypedCanonicalGeometry() {
    final var geometry = ProfileTargetRestMapper.toRectangleGeometry(
        new CustomRectangleProfileTargetRequest(
            "CUSTOM_RECTANGLE",
            new BigDecimal("1.060"),
            new BigDecimal("1.072"),
            new BigDecimal("4.5"),
            new BigDecimal("7.5")
        )
    );

    assertThat(geometry.specificGravityRange())
        .returns(SpecificGravityValue.of("1.060"), MeasuredValueRange::min)
        .returns(SpecificGravityValue.of("1.072"), MeasuredValueRange::max)
        .returns(MeasuredValueRange.UpperBound.INCLUSIVE, MeasuredValueRange::upperBound);
    assertThat(geometry.titratableAcidityRange())
        .returns(TitratableAcidityValue.of("4.5"), MeasuredValueRange::min)
        .returns(TitratableAcidityValue.of("7.5"), MeasuredValueRange::max)
        .returns(MeasuredValueRange.UpperBound.INCLUSIVE, MeasuredValueRange::upperBound);
  }

  @Test
  void mapsTransportTanninRangeToTypedCanonicalTarget() {
    final var target = ProfileTargetRestMapper.toTanninTarget(
        new ProfileTargetTanninRangeRequest(
            new BigDecimal("800"),
            new BigDecimal("1800")
        )
    );

    assertThat(target)
        .returns(TanninValue.of("800"), TanninTargetRange::min)
        .returns(TanninValue.of("1800"), TanninTargetRange::max);
  }
}
