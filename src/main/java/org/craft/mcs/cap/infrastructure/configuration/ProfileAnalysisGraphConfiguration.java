package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.profileanalysis.graph.domain.MidpointGraphRepresentativeValuePolicy;
import org.craft.mcs.cap.core.profileanalysis.graph.application.service.JolicoeurSugarAcidityGraphEvaluator;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.ZoneGeometrySugarAcidityTargetResolver;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.BuiltInJolicoeurSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.GeometryJolicoeurSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneGeometryProfile;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(ProfileAnalysisGraphProperties.class)
class ProfileAnalysisGraphConfiguration {

  @Bean
  JolicoeurSugarAcidityZoneGeometryProfile sugarAcidityZoneGeometryProfile(
      ProfileAnalysisGraphProperties properties
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

  @Bean
  SugarAcidityTargetResolver sugarAcidityTargetResolver(
      JolicoeurSugarAcidityZoneGeometryProfile geometryProfile
  ) {
    return new ZoneGeometrySugarAcidityTargetResolver(geometryProfile);
  }
}
