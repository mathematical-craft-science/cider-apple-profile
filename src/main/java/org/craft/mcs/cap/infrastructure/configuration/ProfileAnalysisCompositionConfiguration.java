package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.BuiltInCompositionCalculatorSelector;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorSelector;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessor;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.TargetAwareCompositionContributionRoleAssessor;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.BuiltInCompositionOptimizationStrategySelector;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationStrategySelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class ProfileAnalysisCompositionConfiguration {

  @Bean
  CompositionCalculatorSelector compositionCalculatorSelector() {
    return new BuiltInCompositionCalculatorSelector();
  }

  @Bean
  CompositionOptimizationStrategySelector compositionOptimizationStrategySelector() {
    return new BuiltInCompositionOptimizationStrategySelector();
  }

  @Bean
  CompositionContributionRoleAssessor compositionContributionRoleAssessor() {
    return new TargetAwareCompositionContributionRoleAssessor();
  }
}
