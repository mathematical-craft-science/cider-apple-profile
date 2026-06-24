package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.profileanalysis.classification.engine.ChemicalProfileClassifier;
import org.craft.mcs.cap.core.profileanalysis.classification.engine.ClassificationConfidencePolicy;
import org.craft.mcs.cap.core.profileanalysis.classification.profile.BuiltInCiderClassificationThresholds;
import org.craft.mcs.cap.core.profileanalysis.classification.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class ProfileAnalysisClassificationConfiguration {

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
