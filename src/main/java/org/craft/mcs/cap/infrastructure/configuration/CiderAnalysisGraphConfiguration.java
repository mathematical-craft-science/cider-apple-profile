package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.MidpointGraphRepresentativeValuePolicy;
import org.craft.mcs.cap.core.cideranalysis.graph.application.usecase.JolicoeurSugarAcidityGraphEvaluator;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.BuiltInJolicoeurSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.GeometryJolicoeurSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.JolicoeurSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.JolicoeurSugarAcidityZoneGeometryProfile;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(CiderAnalysisGraphProperties.class)
class CiderAnalysisGraphConfiguration {

  @Bean
  JolicoeurSugarAcidityZoneGeometryProfile sugarAcidityZoneGeometryProfile(
      CiderAnalysisGraphProperties properties
  ) {
    return switch (properties.geometry()) {
      case ELLIPSE -> BuiltInJolicoeurSugarAcidityZoneProfiles.ellipticalZoneGeometryProfile();
      case RECTANGLE -> BuiltInJolicoeurSugarAcidityZoneProfiles.rectangularZoneGeometryProfile();
    };
  }

  @Bean
  JolicoeurSugarAcidityZoneAssessor sugarAcidityZoneAssessor(
      JolicoeurSugarAcidityZoneGeometryProfile geometryProfile
  ) {
    return new GeometryJolicoeurSugarAcidityZoneAssessor(geometryProfile);
  }

  @Bean
  MidpointGraphRepresentativeValuePolicy midpointGraphRepresentativeValuePolicy() {
    return new MidpointGraphRepresentativeValuePolicy();
  }

  @Bean
  JolicoeurSugarAcidityGraphEvaluator jolicoeurSugarAcidityGraphEvaluator(
      MidpointGraphRepresentativeValuePolicy representativeValuePolicy,
      JolicoeurSugarAcidityZoneAssessor zoneAssessor
  ) {
    return new JolicoeurSugarAcidityGraphEvaluator(
        representativeValuePolicy,
        zoneAssessor
    );
  }

}
