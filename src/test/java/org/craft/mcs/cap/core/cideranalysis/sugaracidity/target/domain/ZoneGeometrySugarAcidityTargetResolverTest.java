package org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain;

import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ZoneGeometrySugarAcidityTargetResolverTest {

  @Test
  void resolvesPredefinedRectangleZoneFromGeometryProfile() {
    final var resolver = new ZoneGeometrySugarAcidityTargetResolver(
        BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
    );

    final var target = resolver.resolvePredefinedZone(SugarAcidityZone.PERFECT_APPLES);

    assertThat(target.kind()).isEqualTo(SugarAcidityTargetKind.PREDEFINED_ZONE);
    assertThat(target.zone()).isEqualTo(SugarAcidityZone.PERFECT_APPLES);
    assertThat(target.geometry()).isInstanceOf(RectangleSugarAcidityTargetGeometry.class);
    final var rectangle = (RectangleSugarAcidityTargetGeometry) target.geometry();
    assertThat(rectangle.sugarMin()).isEqualByComparingTo("1.060");
    assertThat(rectangle.sugarMax()).isEqualByComparingTo("1.080");
    assertThat(rectangle.acidityMin()).isEqualByComparingTo("4.5");
    assertThat(rectangle.acidityMax()).isEqualByComparingTo("7.5");
  }

  @Test
  void rejectsUnsupportedGeometryForDistanceTarget() {
    final var resolver = new ZoneGeometrySugarAcidityTargetResolver(
        BuiltInSugarAcidityZoneProfiles.ellipticalZoneGeometryProfile()
    );

    assertThatThrownBy(() -> resolver.resolvePredefinedZone(SugarAcidityZone.PERFECT_APPLES))
        .isInstanceOf(SugarAcidityTargetResolutionException.class)
        .hasMessageContaining("Unsupported sugar-acidity target geometry");
  }
}
