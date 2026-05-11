package org.craft.mcs.cap.infrastructure.configuration;


import org.craft.mcs.cap.core.cideranalysis.classification.engine.ChemicalProfileClassifier;
import org.craft.mcs.cap.core.cideranalysis.classification.engine.ClassificationConfidencePolicy;
import org.craft.mcs.cap.core.cideranalysis.classification.profile.BuiltInCiderClassificationThresholds;
import org.craft.mcs.cap.core.cideranalysis.classification.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.BuiltInChemicalProfileCompositionCalculatorSelector;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionCalculatorSelector;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.BuiltInCompositionOptimizationStrategySelector;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.CompositionOptimizationStrategySelector;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.ZoneGeometrySugarAcidityTargetResolver;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.EllipseSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZoneGeometryProfile;
import org.craft.mcs.cap.core.cideranalysis.profilegraph.domain.BuiltInSugarAcidityGraphMetadataFactory;
import org.craft.mcs.cap.core.cideranalysis.profilegraph.domain.SugarAcidityGraphMetadata;
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
  SugarAcidityZoneGeometryProfile sugarAcidityZoneGeometryProfile(
      @Value("${cap.cider-analysis.sugar-acidity.assessor:rectangle}") String assessor
  ) {
    return switch (assessor) {
      case "ellipse" -> BuiltInSugarAcidityZoneProfiles.ellipticalZoneGeometryProfile();
      case "rectangle" -> BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile();
      default -> throw new IllegalArgumentException("unsupported sugar-acidity assessor: " + assessor);
    };
  }

  @Bean
  SugarAcidityGraphMetadata sugarAcidityGraphMetadata(
      SugarAcidityZoneGeometryProfile geometryProfile
  ) {
    return BuiltInSugarAcidityGraphMetadataFactory.from(geometryProfile);
  }

  @Bean
  SugarAcidityZoneAssessor sugarAcidityZoneAssessor(
      SugarAcidityZoneGeometryProfile geometryProfile
  ) {
    final var thresholds = BuiltInSugarAcidityZoneProfiles.thresholds();

    return switch (geometryProfile.zones().getFirst().shape()) {
      case ELLIPSE -> new EllipseSugarAcidityZoneAssessor(thresholds, geometryProfile);
      case RECTANGLE -> new RectangleSugarAcidityZoneAssessor(thresholds, geometryProfile);
    };
  }

  @Bean
  SugarAcidityTargetResolver sugarAcidityTargetResolver(
      SugarAcidityZoneGeometryProfile geometryProfile
  ) {
    return new ZoneGeometrySugarAcidityTargetResolver(geometryProfile);
  }

  @Bean
  ProfileAxisNormalizerSelector profileAxisNormalizerSelector() {
    return new BuiltInProfileAxisNormalizerSelector(
        BuiltInSugarAcidityZoneProfiles.thresholds()
    );
  }

  @Bean
  ChemicalProfileCompositionCalculatorSelector chemicalProfileCompositionCalculatorSelector() {
    return new BuiltInChemicalProfileCompositionCalculatorSelector();
  }

  @Bean
  CompositionOptimizationStrategySelector compositionOptimizationStrategySelector() {
    return new BuiltInCompositionOptimizationStrategySelector();
  }

  @Bean
  ProfileTargetObjectiveFunctionFactory profileTargetObjectiveFunctionFactory() {
    return new BuiltInProfileTargetObjectiveFunctionFactory();
  }
}
