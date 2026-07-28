package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.CiderAppleClassifier;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.ClassificationConfidencePolicy;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.BuiltInCiderClassificationThresholds;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanProfileCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class ProfileAnalysisClassificationConfiguration {

  @Bean
  ClassificationConfidencePolicy classificationConfidencePolicy() {
    return new ClassificationConfidencePolicy();
  }

  @Bean
  CiderAppleClassifier ciderAppleClassifier(
      ClassificationConfidencePolicy confidencePolicy
  ) {
    return new CiderAppleClassifier(confidencePolicy);
  }

  @Bean
  NorthAmericanProfileCalculator northAmericanProfileCalculator() {
    return new NorthAmericanProfileCalculator(
        BuiltInCiderClassificationThresholds.northAmerican()
    );
  }
}
