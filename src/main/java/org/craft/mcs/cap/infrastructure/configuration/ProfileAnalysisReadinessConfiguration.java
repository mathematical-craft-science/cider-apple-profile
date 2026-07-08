package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhFermentationStartPolicy;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhPlanningGuidancePolicy;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhRuleSource;
import org.craft.mcs.cap.core.profileanalysis.yan.domain.YanFermentationStartPolicy;
import org.craft.mcs.cap.core.profileanalysis.yan.domain.YanRuleSource;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(ProfileAnalysisReadinessProperties.class)
class ProfileAnalysisReadinessConfiguration {

  @Bean
  PhRuleSource phRuleSource(@NonNull ProfileAnalysisReadinessProperties properties) {
    return new PhRuleSource(properties.ph().sourceCode(), properties.ph().sourceComment());
  }

  @Bean
  YanRuleSource yanRuleSource(@NonNull ProfileAnalysisReadinessProperties properties) {
    return new YanRuleSource(properties.yan().sourceCode(), properties.yan().sourceComment());
  }

  @Bean
  PhPlanningGuidancePolicy phPlanningGuidancePolicy(
      @NonNull ProfileAnalysisReadinessProperties properties,
      @NonNull PhRuleSource phRuleSource
  ) {
    return new PhPlanningGuidancePolicy(
        properties.ph().ciderGuidanceMin(),
        properties.ph().ciderGuidanceMax(),
        phRuleSource
    );
  }

  @Bean
  PhFermentationStartPolicy phFermentationStartPolicy(
      @NonNull ProfileAnalysisReadinessProperties properties,
      @NonNull PhRuleSource phRuleSource
  ) {
    return new PhFermentationStartPolicy(
        properties.ph().genericYeastToleranceMin(),
        properties.ph().ciderGuidanceMin(),
        properties.ph().ciderGuidanceMax(),
        properties.ph().highMicrobialRisk(),
        phRuleSource
    );
  }

  @Bean
  YanFermentationStartPolicy yanFermentationStartPolicy(
      @NonNull ProfileAnalysisReadinessProperties properties,
      @NonNull YanRuleSource yanRuleSource
  ) {
    return new YanFermentationStartPolicy(
        properties.yan().severeLow(),
        properties.yan().low(),
        properties.yan().high(),
        yanRuleSource
    );
  }

  @Bean
  FermentationStartReadinessAssessor fermentationStartReadinessAssessor(
      @NonNull PhFermentationStartPolicy phPolicy,
      @NonNull YanFermentationStartPolicy yanPolicy
  ) {
    return new FermentationStartReadinessAssessor(phPolicy, yanPolicy);
  }
}
