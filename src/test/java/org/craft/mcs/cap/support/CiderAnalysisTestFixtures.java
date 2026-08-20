package org.craft.mcs.cap.support;

import org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.BuiltInCiderClassificationThresholds;
import org.craft.mcs.cap.core.cideranalysis.abv.application.port.in.PredictPotentialAbvUseCase;
import org.craft.mcs.cap.core.cideranalysis.abv.application.usecase.PredictPotentialAbvService;
import org.craft.mcs.cap.core.cideranalysis.classification.application.usecase.ClassifyCiderAppleService;
import org.craft.mcs.cap.core.cideranalysis.classification.application.usecase.AssessNorthAmericanCiderProfileService;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.engine.CiderAppleClassifier;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.engine.ClassificationConfidencePolicy;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.usecase.AssessFermentationStartReadinessService;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.usecase.AssessPhPlanningGuidanceService;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.planning.PhPlanningGuidancePolicy;
import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.port.out.ApplePotentialAbvAnalyzer;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.port.out.AppleCiderClassificationAnalyzer;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.port.out.AppleSugarAcidityGraphAnalyzer;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.abv.port.out.FeedstockPotentialAbvAnalyzer;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.port.out.FeedstockFermentationStartReadinessAnalyzer;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.classification.port.out.FeedstockTanninLevelAnalyzer;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.port.out.FeedstockSugarAcidityGraphAnalyzer;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.port.out.FeedstockPlanningGraphAnalyzer;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.port.out.FeedstockPhPlanningGuidanceAnalyzer;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionPotentialAbvAnalyzer;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.MidpointGraphRepresentativeValuePolicy;
import org.craft.mcs.cap.core.formulation.calculation.domain.CalculatedCompositionCharacteristics;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionSourceCharacteristics;
import org.craft.mcs.cap.core.cideranalysis.graph.application.usecase.JolicoeurSugarAcidityGraphEvaluator;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.BuiltInJolicoeurSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.GeometryJolicoeurSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.JolicoeurSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.JolicoeurSugarAcidityZoneGeometryProfile;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in.ScoreTargetFitUseCase;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.usecase.ScoreTargetFitService;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.contribution.TargetAwareCompositionContributionRoleAssessor;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.normalization.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.objective.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target.ZoneGeometrySugarAcidityTargetResolver;
import org.craft.mcs.cap.core.formulation.calculation.application.port.out.FormulationAbvAnalyzer;
import org.craft.mcs.cap.core.formulation.calculation.application.port.out.FormulationTargetFitAnalyzer;
import org.craft.mcs.cap.core.formulation.interpretation.application.port.out.FormulationTanninLevelAnalyzer;
import org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.abv.ApplePotentialAbvAnalysisAdapter;
import org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.abv.FeedstockPotentialAbvAnalysisAdapter;
import org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.abv.ProductionPotentialAbvAnalysisAdapter;
import org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.classification.AppleCiderClassificationAnalysisAdapter;
import org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.classification.FeedstockTanninLevelAnalysisAdapter;
import org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.classification.FormulationTanninLevelAnalysisAdapter;
import org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.graph.AppleSugarAcidityGraphAnalysisAdapter;
import org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.graph.FeedstockSugarAcidityGraphAnalysisAdapter;
import org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.graph.FeedstockPlanningGraphAnalysisAdapter;
import org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.readiness.FeedstockFermentationStartReadinessAdapter;
import org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.readiness.FeedstockPhPlanningGuidanceAdapter;
import org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.abv.FormulationAbvAnalysisAdapter;
import org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.targetfit.FormulationTargetFitAnalysisAdapter;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Creates consistently configured CiderAnalysis services for focused tests. */
public final class CiderAnalysisTestFixtures {

  private CiderAnalysisTestFixtures() {
  }

  public static @NonNull JolicoeurSugarAcidityGraphEvaluator graphEvaluator() {
    return graphEvaluator(BuiltInJolicoeurSugarAcidityZoneProfiles.rectangularZoneGeometryProfile());
  }

  public static @NonNull JolicoeurSugarAcidityGraphEvaluator graphEvaluator(
      @NonNull JolicoeurSugarAcidityZoneGeometryProfile geometryProfile
  ) {
    final JolicoeurSugarAcidityZoneAssessor zoneAssessor =
        new GeometryJolicoeurSugarAcidityZoneAssessor(geometryProfile);
    return new JolicoeurSugarAcidityGraphEvaluator(
        new MidpointGraphRepresentativeValuePolicy(),
        zoneAssessor
    );
  }

  public static @NonNull NorthAmericanProfileCalculator northAmericanProfileCalculator() {
    return new NorthAmericanProfileCalculator(
        BuiltInCiderClassificationThresholds.northAmerican()
    );
  }

  public static @NonNull PredictPotentialAbvUseCase potentialAbvUseCase() {
    return new PredictPotentialAbvService(PotentialAbvPredictionFixtures.predictor());
  }

  public static @NonNull ScoreTargetFitUseCase targetFitUseCase() {
    return new ScoreTargetFitService(
        new ZoneGeometrySugarAcidityTargetResolver(
            BuiltInJolicoeurSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
        ),
        new BuiltInProfileAxisNormalizerSelector(),
        new BuiltInProfileTargetObjectiveFunctionFactory(),
        new TargetAwareCompositionContributionRoleAssessor()
    );
  }

  public static @NonNull FormulationAbvAnalyzer formulationAbvAnalyzer() {
    return new FormulationAbvAnalysisAdapter(potentialAbvUseCase());
  }

  public static @NonNull FormulationTargetFitAnalyzer formulationTargetFitAnalyzer() {
    return new FormulationTargetFitAnalysisAdapter(targetFitUseCase());
  }

  public static @NonNull FormulationTanninLevelAnalyzer formulationTanninLevelAnalyzer() {
    return new FormulationTanninLevelAnalysisAdapter(northAmericanProfileService());
  }

  public static @NonNull ApplePotentialAbvAnalyzer applePotentialAbvAnalyzer() {
    return new ApplePotentialAbvAnalysisAdapter(potentialAbvUseCase());
  }

  public static @NonNull FeedstockPotentialAbvAnalyzer feedstockPotentialAbvAnalyzer() {
    return new FeedstockPotentialAbvAnalysisAdapter(potentialAbvUseCase());
  }

  public static @NonNull ProductionPotentialAbvAnalyzer productionPotentialAbvAnalyzer() {
    return new ProductionPotentialAbvAnalysisAdapter(potentialAbvUseCase());
  }

  public static @NonNull AppleCiderClassificationAnalyzer appleClassificationAnalyzer() {
    return new AppleCiderClassificationAnalysisAdapter(
        classificationService(),
        northAmericanProfileService()
    );
  }

  public static @NonNull FeedstockTanninLevelAnalyzer feedstockTanninLevelAnalyzer() {
    return new FeedstockTanninLevelAnalysisAdapter(northAmericanProfileService());
  }

  private static @NonNull ClassifyCiderAppleService classificationService() {
    return new ClassifyCiderAppleService(new CiderAppleClassifier(new ClassificationConfidencePolicy()));
  }

  private static @NonNull AssessNorthAmericanCiderProfileService northAmericanProfileService() {
    return new AssessNorthAmericanCiderProfileService(northAmericanProfileCalculator());
  }

  public static @NonNull AppleSugarAcidityGraphAnalyzer appleGraphAnalyzer() {
    return new AppleSugarAcidityGraphAnalysisAdapter(graphEvaluator());
  }

  public static @NonNull FeedstockSugarAcidityGraphAnalyzer feedstockGraphAnalyzer() {
    return new FeedstockSugarAcidityGraphAnalysisAdapter(graphEvaluator());
  }

  public static @NonNull FeedstockSugarAcidityGraphAnalyzer feedstockGraphAnalyzer(
      @NonNull JolicoeurSugarAcidityZoneGeometryProfile geometryProfile
  ) {
    return new FeedstockSugarAcidityGraphAnalysisAdapter(graphEvaluator(geometryProfile));
  }

  public static @NonNull FeedstockPlanningGraphAnalyzer feedstockPlanningGraphAnalyzer() {
    return new FeedstockPlanningGraphAnalysisAdapter(graphEvaluator());
  }

  public static @NonNull FeedstockPlanningGraphAnalyzer feedstockPlanningGraphAnalyzer(
      @NonNull JolicoeurSugarAcidityZoneGeometryProfile geometryProfile
  ) {
    return new FeedstockPlanningGraphAnalysisAdapter(graphEvaluator(geometryProfile));
  }

  public static @NonNull FeedstockFermentationStartReadinessAnalyzer feedstockReadinessAnalyzer() {
    return new FeedstockFermentationStartReadinessAdapter(
        new AssessFermentationStartReadinessService(new FermentationStartReadinessAssessor())
    );
  }

  public static @NonNull FeedstockPhPlanningGuidanceAnalyzer feedstockPhPlanningAnalyzer() {
    return new FeedstockPhPlanningGuidanceAdapter(
        new AssessPhPlanningGuidanceService(PhPlanningGuidancePolicy.curatedCiderV1())
    );
  }

  public static @NonNull CompositionSourceCharacteristics compositionSourceCharacteristics(
      @NonNull String specificGravity,
      @NonNull String titratableAcidity,
      @Nullable String tannin,
      @Nullable String yeastAssimilableNitrogen
  ) {
    return new CompositionSourceCharacteristics(
        PointCharacteristicValue.of(SpecificGravityValue.of(specificGravity)),
        PointCharacteristicValue.of(TitratableAcidityValue.of(titratableAcidity)),
        tannin == null ? null : PointCharacteristicValue.of(TanninValue.of(tannin)),
        yeastAssimilableNitrogen == null
            ? null
            : PointCharacteristicValue.of(YeastAssimilableNitrogenValue.of(yeastAssimilableNitrogen))
    );
  }

  public static @NonNull CalculatedCompositionCharacteristics calculatedCompositionCharacteristics() {
    return new CalculatedCompositionCharacteristics(
        SpecificGravityValue.of("1.050"),
        TitratableAcidityValue.of("6.0"),
        null,
        null
    );
  }
}
