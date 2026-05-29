package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.graph.domain.BuiltInSugarAcidityGraphMetadataFactory;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.graph.domain.SugarAcidityGraphMetadata;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.ZoneGeometrySugarAcidityTargetResolver;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.EllipseSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneGeometryProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class CiderAnalysisSugarAcidityConfiguration {

  @Bean
  SugarAcidityZoneGeometryProfile sugarAcidityZoneGeometryProfile(
      @Value("${cap.cider-analysis.sugar-acidity.assessor:rectangle}") String assessor
  ) {
    return switch (assessor) {
      case "ellipse" -> BuiltInSugarAcidityZoneProfiles.ellipticalZoneGeometryProfile();
      case "rectangle" -> BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile();
      default -> throw new IllegalArgumentException("unsupported sugar-acidity assessor: " + assessor);
    };
  }

  @Bean
  SugarAcidityGraphMetadata sugarAcidityGraphMetadata(
      SugarAcidityZoneGeometryProfile geometryProfile
  ) {
    return BuiltInSugarAcidityGraphMetadataFactory.from(geometryProfile);
  }

  @Bean
  SugarAcidityZoneAssessor sugarAcidityZoneAssessor(
      SugarAcidityZoneGeometryProfile geometryProfile
  ) {
    final var thresholds = BuiltInSugarAcidityZoneProfiles.thresholds();

    return switch (geometryProfile.zones().getFirst().shape()) {
      case ELLIPSE -> new EllipseSugarAcidityZoneAssessor(thresholds, geometryProfile);
      case RECTANGLE -> new RectangleSugarAcidityZoneAssessor(thresholds, geometryProfile);
    };
  }

  @Bean
  SugarAcidityProfileProjector sugarAcidityProfileProjector(
      ChemicalProfileAxisExtractor axisExtractor,
      ChemicalProfileCapabilityAssessor capabilityAssessor,
      SugarAcidityZoneAssessor zoneAssessor
  ) {
    return new SugarAcidityProfileProjector(
        axisExtractor,
        capabilityAssessor,
        zoneAssessor
    );
  }

  @Bean
  SugarAcidityTargetResolver sugarAcidityTargetResolver(
      SugarAcidityZoneGeometryProfile geometryProfile
  ) {
    return new ZoneGeometrySugarAcidityTargetResolver(geometryProfile);
  }
}
