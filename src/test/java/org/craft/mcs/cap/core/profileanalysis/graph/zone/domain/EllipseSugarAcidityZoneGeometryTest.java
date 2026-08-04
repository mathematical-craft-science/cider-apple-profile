package org.craft.mcs.cap.core.profileanalysis.graph.zone.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.EllipseSugarAcidityGeometry;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class EllipseSugarAcidityZoneGeometryTest {

  @Test
  void associatesReusableGeometryWithJolicoeurZone() {
    final var point = new SugarAcidityGraphPoint(
        SpecificGravityValue.of("1.060"),
        TitratableAcidityValue.of("6.0")
    );
    final var ellipse = new EllipseSugarAcidityGeometry(
        point.specificGravity(),
        point.titratableAcidity(),
        new BigDecimal("0.010"),
        new BigDecimal("2.0")
    );
    final var geometry = new EllipseSugarAcidityZoneGeometry(
        JolicoeurSugarAcidityZone.PERFECT_APPLES,
        ellipse
    );

    assertThat(geometry)
        .returns(JolicoeurSugarAcidityZone.PERFECT_APPLES, EllipseSugarAcidityZoneGeometry::zone)
        .returns(ellipse, EllipseSugarAcidityZoneGeometry::geometry)
        .matches(candidate -> candidate.contains(point));
  }
}
