package org.craft.mcs.cap.infrastructure.configuration;


import org.craft.mcs.cap.core.cideranalysis.classification.engine.ChemicalProfileClassifier;
import org.craft.mcs.cap.core.cideranalysis.classification.engine.ClassificationConfidencePolicy;
import org.craft.mcs.cap.core.cideranalysis.classification.profile.BuiltInCiderClassificationThresholds;
import org.craft.mcs.cap.core.cideranalysis.classification.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment.EllipseSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment.SugarAcidityZoneAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class CiderAnalysisConfiguration {

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
      @Value("${cap.cider-analysis.sugar-acidity.assessor:rectangle}") String assessor
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
