package org.craft.mcs.cap.infrastructure.configuration;


import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.classification.engine.ChemicalProfileClassifier;
import org.craft.mcs.cap.shared.domain.classification.engine.ClassificationConfidencePolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class ClassificationConfiguration {

  @Bean
  ChemicalProfileClassifier chemicalProfileClassifier() {
    return new ChemicalProfileClassifier(
        new ChemicalProfileAxisExtractor(),
        new ClassificationConfidencePolicy()
    );
  }
}
