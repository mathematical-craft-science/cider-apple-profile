package org.craft.mcs.cap.core.profileanalysis.graph.zone.domain;

import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class JolicoeurSugarAcidityZoneAssessorTest {

  private final JolicoeurSugarAcidityZoneAssessor assessor =
      new GeometryJolicoeurSugarAcidityZoneAssessor(
          BuiltInJolicoeurSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
      );

  @Test
  void assessesPerfectApples() {
    assertThat(assess("1.060", "4.50"))
        .returns(true, JolicoeurSugarAcidityZoneAssessment::classified)
        .returns(
            JolicoeurSugarAcidityZone.PERFECT_APPLES,
            JolicoeurSugarAcidityZoneAssessment::zone
        );
  }

  @Test
  void assessesHighSugarApples() {
    assertThat(assess("1.060", "7.50").zone())
        .isEqualTo(JolicoeurSugarAcidityZone.HIGH_SUGAR_APPLES);
  }

  @Test
  void assessesLowAcidityApples() {
    assertThat(assess("1.045", "4.49").zone())
        .isEqualTo(JolicoeurSugarAcidityZone.LOW_ACIDITY_APPLES);
  }

  @Test
  void assessesMediumSugarApples() {
    assertThat(assess("1.045", "4.50").zone())
        .isEqualTo(JolicoeurSugarAcidityZone.MEDIUM_SUGAR_APPLES);
  }

  @Test
  void assessesLowSugarApples() {
    assertThat(assess("1.044", "4.50").zone())
        .isEqualTo(JolicoeurSugarAcidityZone.LOW_SUGAR_APPLES);
  }

  @Test
  void derivesUnclassifiedStatusFromAbsentZone() {
    final var ellipticalAssessor = new GeometryJolicoeurSugarAcidityZoneAssessor(
        BuiltInJolicoeurSugarAcidityZoneProfiles.ellipticalZoneGeometryProfile()
    );

    assertThat(ellipticalAssessor.assess(point("1.100", "11.0")))
        .returns(false, JolicoeurSugarAcidityZoneAssessment::classified)
        .returns(null, JolicoeurSugarAcidityZoneAssessment::zone);
  }

  @Test
  void ellipticalAssessorUsesEllipseGeometry() {
    final var ellipticalAssessor = new GeometryJolicoeurSugarAcidityZoneAssessor(
        BuiltInJolicoeurSugarAcidityZoneProfiles.ellipticalZoneGeometryProfile()
    );

    assertThat(ellipticalAssessor.assess(point("1.065", "5.9")).zone())
        .isEqualTo(JolicoeurSugarAcidityZone.PERFECT_APPLES);
  }

  private JolicoeurSugarAcidityZoneAssessment assess(
      String specificGravity,
      String titratableAcidity
  ) {
    return assessor.assess(point(specificGravity, titratableAcidity));
  }

  private static SugarAcidityGraphPoint point(
      String specificGravity,
      String titratableAcidity
  ) {
    return new SugarAcidityGraphPoint(
        SpecificGravityValue.of(specificGravity),
        TitratableAcidityValue.of(titratableAcidity)
    );
  }

}
