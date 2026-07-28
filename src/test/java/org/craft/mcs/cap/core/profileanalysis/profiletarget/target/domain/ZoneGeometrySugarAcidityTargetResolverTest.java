package org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain;

import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.BuiltInJolicoeurSugarAcidityZoneProfiles;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ZoneGeometrySugarAcidityTargetResolverTest {

  @Test
  void resolvesPredefinedRectangleZoneFromGeometryProfile() {
    final var resolver = new ZoneGeometrySugarAcidityTargetResolver(
        BuiltInJolicoeurSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
    );

    final var target = resolver.resolvePredefinedZone(JolicoeurSugarAcidityZone.PERFECT_APPLES);

    assertThat(target)
        .returns(SugarAcidityTargetKind.PREDEFINED_ZONE, PredefinedJolicoeurSugarAcidityZoneTarget::kind)
        .returns(JolicoeurSugarAcidityZone.PERFECT_APPLES, PredefinedJolicoeurSugarAcidityZoneTarget::zone)
        .extracting(PredefinedJolicoeurSugarAcidityZoneTarget::geometry)
        .isInstanceOf(RectangleSugarAcidityTargetGeometry.class);
    final var rectangle = (RectangleSugarAcidityTargetGeometry) target.geometry();
    assertThat(rectangle.specificGravityRange())
        .returns(SpecificGravityValue.of("1.060"), MeasuredValueRange::min)
        .returns(SpecificGravityValue.of("1.080"), MeasuredValueRange::max)
        .returns(MeasuredValueRange.UpperBound.EXCLUSIVE, MeasuredValueRange::upperBound);
    assertThat(rectangle.titratableAcidityRange())
        .returns(TitratableAcidityValue.of("4.5"), MeasuredValueRange::min)
        .returns(TitratableAcidityValue.of("7.5"), MeasuredValueRange::max)
        .returns(MeasuredValueRange.UpperBound.EXCLUSIVE, MeasuredValueRange::upperBound);
  }

  @Test
  void resolvesPredefinedEllipseZoneFromGeometryProfile() {
    final var resolver = new ZoneGeometrySugarAcidityTargetResolver(
        BuiltInJolicoeurSugarAcidityZoneProfiles.ellipticalZoneGeometryProfile()
    );

    final var target = resolver.resolvePredefinedZone(JolicoeurSugarAcidityZone.PERFECT_APPLES);

    assertThat(target)
        .returns(SugarAcidityTargetKind.PREDEFINED_ZONE, PredefinedJolicoeurSugarAcidityZoneTarget::kind)
        .returns(JolicoeurSugarAcidityZone.PERFECT_APPLES, PredefinedJolicoeurSugarAcidityZoneTarget::zone)
        .extracting(PredefinedJolicoeurSugarAcidityZoneTarget::geometry)
        .isInstanceOf(EllipseSugarAcidityTargetGeometry.class);
  }
}
