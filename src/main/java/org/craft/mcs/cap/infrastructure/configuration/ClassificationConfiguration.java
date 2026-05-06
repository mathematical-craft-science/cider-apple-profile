package org.craft.mcs.cap.infrastructure.configuration;


import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.classification.engine.ChemicalProfileClassifier;
import org.craft.mcs.cap.shared.domain.classification.engine.ClassificationConfidencePolicy;
import org.craft.mcs.cap.shared.domain.classification.profile.BuiltInCiderClassificationThresholds;
import org.craft.mcs.cap.shared.domain.classification.profile.northamerical.NorthAmericanProfileCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class ClassificationConfiguration {

  @Bean
  ChemicalProfileAxisExtractor chemicalProfileAxisExtractor() {
    return new ChemicalProfileAxisExtractor();
  }

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
