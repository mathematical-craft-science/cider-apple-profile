package org.craft.mcs.cap.core.cideranalysis.graph.domain.zone;

import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGraphPoint;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
            JolicoeurSugarAcidityZoneId.PERFECT_APPLES,
            JolicoeurSugarAcidityZoneAssessment::zone
        );
  }

  @Test
  void assessesHighSugarApples() {
    assertThat(assess("1.060", "7.50").zone())
        .isEqualTo(JolicoeurSugarAcidityZoneId.HIGH_SUGAR_APPLES);
  }

  @Test
  void assessesLowAcidityApples() {
    assertThat(assess("1.045", "4.49").zone())
        .isEqualTo(JolicoeurSugarAcidityZoneId.LOW_ACIDITY_APPLES);
  }

  @Test
  void assessesMediumSugarApples() {
    assertThat(assess("1.045", "4.50").zone())
        .isEqualTo(JolicoeurSugarAcidityZoneId.MEDIUM_SUGAR_APPLES);
  }

  @Test
  void assessesLowSugarApples() {
    assertThat(assess("1.044", "4.50").zone())
        .isEqualTo(JolicoeurSugarAcidityZoneId.LOW_SUGAR_APPLES);
  }

  @ParameterizedTest
  @CsvSource({
      "1.045, 4.50, MEDIUM_SUGAR_APPLES",
      "1.045, 7.50, MEDIUM_SUGAR_APPLES",
      "1.045, 7.60, MEDIUM_SUGAR_APPLES",
      "1.060, 4.50, PERFECT_APPLES",
      "1.060, 7.50, HIGH_SUGAR_APPLES",
      "1.060, 7.60, HIGH_SUGAR_APPLES"
  })
  void assignsSharedHalfOpenBoundaryToExactlyOneAdjacentZone(
      String specificGravity,
      String titratableAcidity,
      JolicoeurSugarAcidityZoneId expectedZone
  ) {
    final var graphPoint = point(specificGravity, titratableAcidity);
    final var geometryProfile = BuiltInJolicoeurSugarAcidityZoneProfiles.rectangularZoneGeometryProfile();

    assertThat(geometryProfile.zones())
        .filteredOn(zone -> zone.contains(graphPoint))
        .singleElement()
        .returns(expectedZone, JolicoeurSugarAcidityZoneGeometry::zone);
    assertThat(assessor.assess(graphPoint))
        .returns(true, JolicoeurSugarAcidityZoneAssessment::classified)
        .returns(expectedZone, JolicoeurSugarAcidityZoneAssessment::zone);
  }

  @ParameterizedTest
  @CsvSource({
      "1.080, 6.0",
      "1.070, 10.0"
  })
  void keepsGraphScaleMaximumOutsideHalfOpenZonePartition(
      String specificGravity,
      String titratableAcidity
  ) {
    assertThat(assess(specificGravity, titratableAcidity))
        .returns(false, JolicoeurSugarAcidityZoneAssessment::classified)
        .returns(null, JolicoeurSugarAcidityZoneAssessment::zone);
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
        .isEqualTo(JolicoeurSugarAcidityZoneId.PERFECT_APPLES);
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
