package org.craft.mcs.cap.infrastructure.configuration;


import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.classification.engine.ChemicalProfileClassifier;
import org.craft.mcs.cap.shared.domain.classification.engine.ClassificationConfidencePolicy;
import org.craft.mcs.cap.shared.domain.classification.profile.BuiltInCiderClassificationThresholds;
import org.craft.mcs.cap.shared.domain.classification.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.EllipseSugarAcidityZoneAssessor;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZoneAssessor;
import org.springframework.beans.factory.annotation.Value;
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

  @Bean
  SugarAcidityZoneAssessor sugarAcidityZoneAssessor(
      @Value("${cap.classification.sugar-acidity.assessor:rectangle}") String assessor
  ) {
    final var thresholds = BuiltInSugarAcidityZoneProfiles.thresholds();

    return switch (assessor) {
      case "ellipse" -> new EllipseSugarAcidityZoneAssessor(
          thresholds,
          BuiltInSugarAcidityZoneProfiles.ellipticalGraphMetadata()
      );
      case "rectangle" -> new RectangleSugarAcidityZoneAssessor(
          thresholds,
          BuiltInSugarAcidityZoneProfiles.rectangularGraphMetadata()
      );
      default -> throw new IllegalArgumentException("unsupported sugar-acidity assessor: " + assessor);
    };
  }
}
