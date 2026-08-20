package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.JolicoeurSugarAcidityZoneGeometryProfile;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.contribution.CompositionContributionRoleAssessor;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.contribution.TargetAwareCompositionContributionRoleAssessor;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.normalization.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.objective.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.normalization.ProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.objective.ProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target.ZoneGeometrySugarAcidityTargetResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class CiderAnalysisTargetFitConfiguration {

  @Bean
  ProfileAxisNormalizerSelector profileAxisNormalizerSelector() {
    return new BuiltInProfileAxisNormalizerSelector();
  }

  @Bean
  ProfileTargetObjectiveFunctionFactory profileTargetObjectiveFunctionFactory() {
    return new BuiltInProfileTargetObjectiveFunctionFactory();
  }

  @Bean
  CompositionContributionRoleAssessor compositionContributionRoleAssessor() {
    return new TargetAwareCompositionContributionRoleAssessor();
  }

  @Bean
  SugarAcidityTargetResolver sugarAcidityTargetResolver(
      JolicoeurSugarAcidityZoneGeometryProfile geometryProfile
  ) {
    return new ZoneGeometrySugarAcidityTargetResolver(geometryProfile);
  }
}
