package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.formulation.calculation.domain.BuiltInCompositionCalculatorSelector;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionCalculatorSelector;
import org.craft.mcs.cap.core.formulation.optimization.domain.strategy.BuiltInCompositionOptimizationStrategySelector;
import org.craft.mcs.cap.core.formulation.optimization.domain.strategy.CompositionOptimizationStrategySelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class FormulationConfiguration {

  @Bean
  CompositionCalculatorSelector compositionCalculatorSelector() {
    return new BuiltInCompositionCalculatorSelector();
  }

  @Bean
  CompositionOptimizationStrategySelector compositionOptimizationStrategySelector() {
    return new BuiltInCompositionOptimizationStrategySelector();
  }
}
