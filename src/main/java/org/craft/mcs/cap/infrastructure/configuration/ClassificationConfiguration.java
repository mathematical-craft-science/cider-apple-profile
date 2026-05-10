package org.craft.mcs.cap.infrastructure.configuration;


import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.classification.engine.ChemicalProfileClassifier;
import org.craft.mcs.cap.shared.domain.classification.engine.ClassificationConfidencePolicy;
import org.craft.mcs.cap.shared.domain.classification.profile.BuiltInCiderClassificationThresholds;
import org.craft.mcs.cap.shared.domain.classification.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.domain.fermentation.AbvPredictionStrategy;
import org.craft.mcs.cap.shared.domain.fermentation.FinalGravityPredictionStrategy;
import org.craft.mcs.cap.shared.domain.fermentation.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.shared.domain.fermentation.PotentialAbvPredictor;
import org.craft.mcs.cap.shared.domain.fermentation.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment.EllipseSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment.SugarAcidityZoneAssessor;
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
  ChemicalProfileCapabilityAssessor chemicalProfileCapabilityAssessor() {
    return new ChemicalProfileCapabilityAssessor();
  }

  @Bean
  FinalGravityPredictionStrategy finalGravityPredictionStrategy() {
    return FixedFinalGravityPredictionStrategy.dryCiderDefault();
  }

  @Bean
  AbvPredictionStrategy abvPredictionStrategy() {
    return new StandardGravityDropAbvPredictionStrategy();
  }

  @Bean
  PotentialAbvPredictor potentialAbvPredictor(
      FinalGravityPredictionStrategy finalGravityPredictionStrategy,
      AbvPredictionStrategy abvPredictionStrategy
  ) {
    return new PotentialAbvPredictor(finalGravityPredictionStrategy, abvPredictionStrategy);
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
