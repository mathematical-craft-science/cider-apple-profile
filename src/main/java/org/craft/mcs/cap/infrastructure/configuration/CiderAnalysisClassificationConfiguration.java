package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.cideranalysis.classification.engine.ChemicalProfileClassifier;
import org.craft.mcs.cap.core.cideranalysis.classification.engine.ClassificationConfidencePolicy;
import org.craft.mcs.cap.core.cideranalysis.classification.profile.BuiltInCiderClassificationThresholds;
import org.craft.mcs.cap.core.cideranalysis.classification.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class CiderAnalysisClassificationConfiguration {

  @Bean
  ClassificationConfidencePolicy classificationConfidencePolicy() {
    return new ClassificationConfidencePolicy();
  }

  @Bean
  ChemicalProfileClassifier chemicalProfileClassifier(
      ChemicalProfileAxisExtractor axisExtractor,
      ClassificationConfidencePolicy confidencePolicy
  ) {
    return new ChemicalProfileClassifier(
        axisExtractor,
        confidencePolicy
    );
  }

  @Bean
  NorthAmericanProfileCalculator northAmericanProfileCalculator() {
    return new NorthAmericanProfileCalculator(
        BuiltInCiderClassificationThresholds.northAmerican()
    );
  }
}
