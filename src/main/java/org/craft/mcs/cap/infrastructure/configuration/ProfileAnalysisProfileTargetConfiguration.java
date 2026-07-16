package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class ProfileAnalysisProfileTargetConfiguration {

  @Bean
  ProfileAxisNormalizerSelector profileAxisNormalizerSelector() {
    return new BuiltInProfileAxisNormalizerSelector(
        BuiltInSugarAcidityZoneProfiles.thresholds()
    );
  }

  @Bean
  ProfileTargetObjectiveFunctionFactory profileTargetObjectiveFunctionFactory() {
    return new BuiltInProfileTargetObjectiveFunctionFactory();
  }
}
