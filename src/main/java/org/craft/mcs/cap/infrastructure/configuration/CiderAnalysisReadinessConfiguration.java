package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.cideranalysis.readiness.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.ReadinessRuleSource;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.ph.CiderPhGuidanceRange;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.ph.PhFermentationStartPolicy;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.planning.PhPlanningGuidancePolicy;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.yan.YanFermentationStartPolicy;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CiderAnalysisReadinessProperties.class)
class CiderAnalysisReadinessConfiguration {

  @Bean
  ReadinessRuleSource phRuleSource(@NonNull CiderAnalysisReadinessProperties properties) {
    return new ReadinessRuleSource(properties.ph().sourceCode(), properties.ph().sourceComment());
  }

  @Bean
  ReadinessRuleSource yanRuleSource(@NonNull CiderAnalysisReadinessProperties properties) {
    return new ReadinessRuleSource(properties.yan().sourceCode(), properties.yan().sourceComment());
  }

  @Bean
  CiderPhGuidanceRange ciderPhGuidanceRange(@NonNull CiderAnalysisReadinessProperties properties) {
    return new CiderPhGuidanceRange(
        properties.ph().ciderGuidanceMin(),
        properties.ph().ciderGuidanceMax()
    );
  }

  @Bean
  PhPlanningGuidancePolicy phPlanningGuidancePolicy(
      @NonNull CiderPhGuidanceRange ciderPhGuidanceRange,
      @Qualifier("phRuleSource") @NonNull ReadinessRuleSource phRuleSource
  ) {
    return new PhPlanningGuidancePolicy(
        ciderPhGuidanceRange,
        phRuleSource
    );
  }

  @Bean
  PhFermentationStartPolicy phFermentationStartPolicy(
      @NonNull CiderAnalysisReadinessProperties properties,
      @NonNull CiderPhGuidanceRange ciderPhGuidanceRange,
      @Qualifier("phRuleSource") @NonNull ReadinessRuleSource phRuleSource
  ) {
    return new PhFermentationStartPolicy(
        properties.ph().genericYeastToleranceMin(),
        ciderPhGuidanceRange,
        properties.ph().highMicrobialRisk(),
        phRuleSource
    );
  }

  @Bean
  YanFermentationStartPolicy yanFermentationStartPolicy(
      @NonNull CiderAnalysisReadinessProperties properties,
      @Qualifier("yanRuleSource") @NonNull ReadinessRuleSource yanRuleSource
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
