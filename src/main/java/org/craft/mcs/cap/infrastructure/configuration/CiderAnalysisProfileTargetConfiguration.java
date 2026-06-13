package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class CiderAnalysisProfileTargetConfiguration {

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
