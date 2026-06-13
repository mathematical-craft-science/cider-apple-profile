package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.BuiltInChemicalProfileCompositionCalculatorSelector;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorSelector;
import org.craft.mcs.cap.core.cideranalysis.composition.contribution.domain.CompositionContributionRoleAssessor;
import org.craft.mcs.cap.core.cideranalysis.composition.contribution.domain.TargetAwareCompositionContributionRoleAssessor;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.BuiltInCompositionOptimizationStrategySelector;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.CompositionOptimizationStrategySelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class CiderAnalysisCompositionConfiguration {

  @Bean
  ChemicalProfileCompositionCalculatorSelector chemicalProfileCompositionCalculatorSelector() {
    return new BuiltInChemicalProfileCompositionCalculatorSelector();
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
