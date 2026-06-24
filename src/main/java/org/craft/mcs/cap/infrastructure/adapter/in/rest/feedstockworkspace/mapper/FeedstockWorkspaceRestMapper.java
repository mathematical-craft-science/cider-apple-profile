package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityAxisPoint;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.TanninTargetRange;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningTargetCandidateView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningBatchView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningBatchViewQuery;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningCurrentMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningDataQualityIndicator;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningDataQualityView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningMeasurementFreshnessView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningReadinessCheck;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningPotentialAbvView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningRoleExplanationView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningTargetAnalysisView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningView;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper.CompositionContributionRoleRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileCapabilityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.dto.FermentationStartReadinessAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.mapper.FeedstockBatchReadinessRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.mapper.FeedstockBatchRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceTargetCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceBatchQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceCurrentMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceDataQualityIndicatorResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceDataQualityResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceMeasurementFreshnessResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspacePlanningReadinessCheckResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspacePlanningReadinessResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspacePotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceProfileSummaryResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceProfileValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceRoleExplanationResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceSugarAcidityResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceTargetAnalysisResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.PredefinedZoneProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.RangeMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.StatisticalMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;


public final class FeedstockWorkspaceRestMapper {

  private FeedstockWorkspaceRestMapper() {
  }

  public static @NonNull FeedstockPlanningBatchViewQuery toQuery(
      @NonNull FeedstockWorkspaceBatchQueryRequest request
  ) {
    requireNonNull(request, "feedstock workspace batch view request cannot be null");
    final var purpose = purpose(request);
    return new FeedstockPlanningBatchViewQuery(
        CiderMakerId.of(request.ciderMakerId()),
        toSeason(request.season()),
        toTargetSelection(request.target()),
        toTanninTarget(request),
        ProfileTargetRestMapper.toWeights(request.weights()),
        purpose,
        requireNonNullElse(request.targetCandidateLimit(), CursorPageRequest.MAX_LIMIT)
    );
  }

  public static @NonNull FeedstockWorkspaceResponse toResponse(@NonNull FeedstockPlanningView view) {
    requireNonNull(view, "feedstock workspace view cannot be null");
    return new FeedstockWorkspaceResponse(
        view.batches().stream()
            .map(FeedstockWorkspaceRestMapper::toResponse)
            .toList(),
        toTargetAnalysisResponse(view.targetAnalysis())
    );
  }

  private static @NonNull FeedstockWorkspaceBatchResponse toResponse(
      @NonNull FeedstockPlanningBatchView view
  ) {
    requireNonNull(view, "feedstock workspace batch view cannot be null");
    return new FeedstockWorkspaceBatchResponse(
        FeedstockBatchRestMapper.toResponse(view.batch()),
        toCurrentMeasurementResponse(view.currentMeasurement()),
        toProfileSummaryResponse(view.currentMeasurement()),
        toMeasurementFreshnessResponse(view.measurementFreshness()),
        toSugarAcidityResponse(view.sugarAcidity()),
        toPotentialAbvResponse(view.potentialAbv()),
        toYeastAssimilableNitrogenResponse(view.yeastAssimilableNitrogen()),
        toResponse(view.planningReadiness()),
        toResponse(view.dataQuality()),
        toFermentationStartReadinessResponse(view),
        toTargetCandidateResponse(view.targetCandidate())
    );
  }

  private static @Nullable FeedstockProfileTargetSelection toTargetSelection(
      @Nullable ProfileTargetRequest request
  ) {
    return request == null ? null : toTarget(request);
  }

  private static @Nullable TanninTargetRange toTanninTarget(@NonNull FeedstockWorkspaceBatchQueryRequest request) {
    final var tanninTarget = request.tanninTarget();
    return tanninTarget == null ? null : new TanninTargetRange(tanninTarget.min(), tanninTarget.max());
  }

  private static @NonNull ProfileTargetScoringPurpose purpose(@NonNull FeedstockWorkspaceBatchQueryRequest request) {
    final var purpose = request.purpose();
    return requireNonNullElse(purpose, ProfileTargetScoringPurpose.BLENDING_PLANNING); // NOSONAR
  }

  private static @Nullable FeedstockWorkspaceSugarAcidityResponse toSugarAcidityResponse(
      @Nullable SugarAcidityProfileEvaluation evaluation
  ) {
    if (evaluation == null) {
      return null;
    }

    final var assessment = evaluation.assessment();
    final var assessmentResponse = assessment == null
        ? null
        : SugarAcidityZoneRestMapper.toResponse(assessment);
    return new FeedstockWorkspaceSugarAcidityResponse(
        evaluation.plottable(),
        evaluation.point()
            .map(SugarAcidityZoneRestMapper::toResponse)
            .orElse(null), // NOSONAR
        assessmentResponse,
        ChemicalProfileCapabilityRestMapper.toResponse(evaluation.capabilityAssessment())
    );
  }

  private static @Nullable FeedstockWorkspaceCurrentMeasurementResponse toCurrentMeasurementResponse(
      @Nullable FeedstockPlanningCurrentMeasurementView view
  ) {
    return view == null ? null : toResponse(view);
  }

  private static @Nullable FeedstockWorkspaceProfileSummaryResponse toProfileSummaryResponse(
      @Nullable FeedstockPlanningCurrentMeasurementView view
  ) {
    return view == null ? null : toProfileSummaryResponse(view.chemicalProfile());
  }

  private static @NonNull FeedstockWorkspaceProfileSummaryResponse toProfileSummaryResponse(
      @NonNull ChemicalProfile profile
  ) {
    requireNonNull(profile, "chemical profile cannot be null");
    return new FeedstockWorkspaceProfileSummaryResponse(
        profile.sugar()
            .map(measurement -> toProfileValue(
                measurement,
                FeedstockWorkspaceRestMapper::specificGravity,
                CanonicalUnitLabels.SUGAR
            ))
            .orElse(null), // NOSONAR
        profile.titratableAcidity()
            .map(measurement -> toProfileValue(
                measurement,
                TitratableAcidityValue::asBigDecimal,
                CanonicalUnitLabels.TITRATABLE_ACIDITY
            ))
            .orElse(null), // NOSONAR
        profile.ph()
            .map(measurement -> toProfileValue(
                measurement,
                PhValue::asBigDecimal,
                CanonicalUnitLabels.PH
            ))
            .orElse(null), // NOSONAR
        profile.tanninity()
            .map(measurement -> toProfileValue(
                measurement,
                TanninityValue::asBigDecimal,
                CanonicalUnitLabels.TANNINITY
            ))
            .orElse(null), // NOSONAR
        profile.yeastAssimilableNitrogen()
            .map(measurement -> toProfileValue(
                measurement,
                YeastAssimilableNitrogenValue::asBigDecimal,
                CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN
            ))
            .orElse(null) // NOSONAR
    );
  }

  private static @Nullable FeedstockWorkspacePotentialAbvResponse toPotentialAbvResponse(
      @Nullable FeedstockPlanningPotentialAbvView view
  ) {
    return view == null ? null : toResponse(view);
  }

  private static @Nullable FeedstockWorkspaceMeasurementFreshnessResponse toMeasurementFreshnessResponse(
      @Nullable FeedstockPlanningMeasurementFreshnessView view
  ) {
    return view == null ? null : toResponse(view);
  }

  private static @Nullable SugarAcidityAxisPointResponse toYeastAssimilableNitrogenResponse(
      @Nullable AxisValue axisValue
  ) {
    return axisValue == null ? null : toResponse(axisValue);
  }

  private static @Nullable FeedstockWorkspaceTargetCandidateResponse toTargetCandidateResponse(
      @Nullable FeedstockPlanningTargetCandidateView view
  ) {
    return view == null ? null : toResponse(view);
  }

  private static @Nullable FeedstockWorkspaceTargetAnalysisResponse toTargetAnalysisResponse(
      @Nullable FeedstockPlanningTargetAnalysisView view
  ) {
    return view == null ? null : toResponse(view);
  }

  private static @Nullable FermentationStartReadinessAssessmentResponse toFermentationStartReadinessResponse(
      @NonNull FeedstockPlanningBatchView view
  ) {
    final var readiness = view.fermentationStartReadiness();
    return readiness == null ? null : FeedstockBatchReadinessRestMapper.toResponse(readiness);
  }

  private static @NonNull FeedstockWorkspaceCurrentMeasurementResponse toResponse(
      @NonNull FeedstockPlanningCurrentMeasurementView view
  ) {
    requireNonNull(view, "feedstock workspace current measurement view cannot be null");
    return new FeedstockWorkspaceCurrentMeasurementResponse(
        view.measurementId().value(),
        view.name().value(),
        view.measuredAt(),
        ChemicalProfileRestMapper.toResponse(view.chemicalProfile()),
        view.note()
    );
  }

  private static @NonNull FeedstockWorkspacePotentialAbvResponse toResponse(
      @NonNull FeedstockPlanningPotentialAbvView view
  ) {
    requireNonNull(view, "feedstock workspace potential ABV view cannot be null");
    return new FeedstockWorkspacePotentialAbvResponse(
        view.originalGravityValueKind(),
        PotentialAbvPredictionRestMapper.toResponse(view.prediction())
    );
  }

  private static @NonNull FeedstockWorkspaceTargetCandidateResponse toResponse(
      @NonNull FeedstockPlanningTargetCandidateView view
  ) {
    requireNonNull(view, "feedstock target candidate view cannot be null");
    return new FeedstockWorkspaceTargetCandidateResponse(
        SugarAcidityZoneRestMapper.toResponse(view.point()),
        ProfileTargetRestMapper.toResponse(view.distance()),
        requireNonNull(
            CompositionContributionRoleRestMapper.toResponse(view.contributionRole()),
            "composition contribution role response cannot be null"
        ),
        toResponse(view.roleExplanation())
    );
  }

  private static @NonNull FeedstockWorkspaceMeasurementFreshnessResponse toResponse(
      @NonNull FeedstockPlanningMeasurementFreshnessView view
  ) {
    requireNonNull(view, "feedstock workspace measurement freshness view cannot be null");
    return new FeedstockWorkspaceMeasurementFreshnessResponse(
        view.status(),
        view.stale(),
        view.reason()
    );
  }

  private static @NonNull FeedstockWorkspacePlanningReadinessResponse toResponse(
      @NonNull FeedstockPlanningReadinessView view
  ) {
    requireNonNull(view, "feedstock workspace planning readiness view cannot be null");
    return new FeedstockWorkspacePlanningReadinessResponse(
        view.status(),
        view.checks().stream()
            .map(FeedstockWorkspaceRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull FeedstockWorkspacePlanningReadinessCheckResponse toResponse(
      @NonNull FeedstockPlanningReadinessCheck check
  ) {
    requireNonNull(check, "feedstock workspace planning readiness check cannot be null");
    return new FeedstockWorkspacePlanningReadinessCheckResponse(
        check.factor(),
        check.severity(),
        check.code(),
        check.message()
    );
  }

  private static @NonNull FeedstockWorkspaceDataQualityResponse toResponse(
      @NonNull FeedstockPlanningDataQualityView view
  ) {
    requireNonNull(view, "feedstock workspace data quality view cannot be null");
    return new FeedstockWorkspaceDataQualityResponse(
        view.status(),
        view.indicators().stream()
            .map(FeedstockWorkspaceRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull FeedstockWorkspaceDataQualityIndicatorResponse toResponse(
      @NonNull FeedstockPlanningDataQualityIndicator indicator
  ) {
    requireNonNull(indicator, "feedstock workspace data quality indicator cannot be null");
    return new FeedstockWorkspaceDataQualityIndicatorResponse(
        indicator.code(),
        indicator.severity(),
        indicator.message()
    );
  }

  private static @NonNull FeedstockWorkspaceRoleExplanationResponse toResponse(
      @NonNull FeedstockPlanningRoleExplanationView view
  ) {
    requireNonNull(view, "feedstock workspace role explanation view cannot be null");
    return new FeedstockWorkspaceRoleExplanationResponse(
        view.summary(),
        view.reasons()
    );
  }

  private static @NonNull FeedstockWorkspaceTargetAnalysisResponse toResponse(
      @NonNull FeedstockPlanningTargetAnalysisView view
  ) {
    requireNonNull(view, "feedstock workspace target analysis view cannot be null");
    return new FeedstockWorkspaceTargetAnalysisResponse(view.purpose(), view.normalizerCode());
  }

  private static @NonNull SugarAcidityAxisPointResponse toResponse(@NonNull AxisValue axisValue) {
    requireNonNull(axisValue, "chemical axis value cannot be null");
    return SugarAcidityZoneRestMapper.toResponse(
        new SugarAcidityAxisPoint(
            axisValue.value(),
            axisValue.valueKind(),
            axisValue.min(),
            axisValue.max(),
            axisValue.standardDeviation(),
            axisValue.sampleSize()
        ),
        CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN
    );
  }

  private static <T extends Comparable<T>> @NonNull FeedstockWorkspaceProfileValueResponse toProfileValue(
      @NonNull MeasuredCharacteristic<T> measurement,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor,
      @NonNull String unit
  ) {
    requireNonNull(measurement, "measured characteristic cannot be null");
    requireNonNull(extractor, "profile value extractor cannot be null");
    requireNonNull(unit, "profile value unit cannot be null");

    return switch (measurement) {
      case ExactMeasurement<T> exact -> exactProfileValue(exact, extractor, unit);
      case RangeMeasurement<T> range -> rangeProfileValue(range, extractor, unit);
      case StatisticalMeasurement<T> statistical -> statisticalProfileValue(statistical, extractor, unit);
    };
  }

  private static <T extends Comparable<T>> @NonNull FeedstockWorkspaceProfileValueResponse exactProfileValue(
      @NonNull ExactMeasurement<T> measurement,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor,
      @NonNull String unit
  ) {
    return new FeedstockWorkspaceProfileValueResponse(
        quantity(extractor.apply(measurement.value()), unit),
        MeasurementValueKind.EXACT,
        null,
        null,
        null,
        null
    );
  }

  private static <T extends Comparable<T>> @NonNull FeedstockWorkspaceProfileValueResponse rangeProfileValue(
      @NonNull RangeMeasurement<T> measurement,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor,
      @NonNull String unit
  ) {
    final var min = extractor.apply(measurement.min());
    final var max = extractor.apply(measurement.max());
    return new FeedstockWorkspaceProfileValueResponse(
        quantity(rangeMidpoint(min, max), unit),
        MeasurementValueKind.RANGE_MIDPOINT,
        quantity(min, unit),
        quantity(max, unit),
        null,
        null
    );
  }

  private static <T extends Comparable<T>> @NonNull FeedstockWorkspaceProfileValueResponse statisticalProfileValue(
      @NonNull StatisticalMeasurement<T> measurement,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor,
      @NonNull String unit
  ) {
    final var hasMedian = measurement.median() != null;
    return new FeedstockWorkspaceProfileValueResponse(
        quantity(extractor.apply(statisticalValue(measurement, hasMedian)), unit),
        hasMedian ? MeasurementValueKind.STATISTICAL_MEDIAN : MeasurementValueKind.STATISTICAL_MEAN,
        null,
        null,
        nullableQuantity(measurement.standardDeviation(), unit),
        measurement.sampleSize()
    );
  }

  private static <T extends Comparable<T>> @NonNull T statisticalValue(
      @NonNull StatisticalMeasurement<T> measurement,
      boolean hasMedian
  ) {
    return hasMedian
        ? requireNonNull(measurement.median(), "statistical profile value median cannot be null")
        : measurement.mean();
  }

  private static @NonNull BigDecimal specificGravity(@NonNull SugarConcentration concentration) {
    requireNonNull(concentration, "sugar concentration cannot be null");
    return concentration.specificGravity().asBigDecimal();
  }

  private static @NonNull BigDecimal rangeMidpoint(
      @NonNull BigDecimal min,
      @NonNull BigDecimal max
  ) {
    return min.add(max).divide(BigDecimal.valueOf(2), RoundingMode.UNNECESSARY);
  }

  private static @NonNull DecimalQuantityResponse quantity(
      @NonNull BigDecimal value,
      @NonNull String unit
  ) {
    return new DecimalQuantityResponse(value, unit);
  }

  private static @Nullable DecimalQuantityResponse nullableQuantity(
      @Nullable BigDecimal value,
      @NonNull String unit
  ) {
    return value == null ? null : quantity(value, unit);
  }

  private static @Nullable FeedstockSeason toSeason(@Nullable FeedstockSeasonRequest request) {
    return request == null ? null : FeedstockSeason.of(request.startsOn(), request.endsOn());
  }

  private static @NonNull FeedstockProfileTargetSelection toTarget(@NonNull ProfileTargetRequest request) {
    requireNonNull(request, "profile target request cannot be null");
    return switch (request) {
      case PredefinedZoneProfileTargetRequest predefined ->
          new PredefinedFeedstockProfileTargetSelection(predefined.zone());
      case CustomRectangleProfileTargetRequest custom ->
          new CustomRectangleFeedstockProfileTargetSelection(new RectangleSugarAcidityTargetGeometry(
              custom.sugarMin(),
              custom.sugarMax(),
              custom.acidityMin(),
              custom.acidityMax()
          ));
    };
  }
}
