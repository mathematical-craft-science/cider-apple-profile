package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.mapper;

import org.craft.mcs.cap.core.feedstock.application.detail.model.FeedstockBatchCurrentProfileOperationView;
import org.craft.mcs.cap.core.feedstock.application.detail.model.FeedstockBatchCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.detail.model.FeedstockBatchDetailView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityIndicator;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentMeasurementFreshnessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfilePotentialAbvView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessCheck;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityAxisPoint;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileCapabilityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.mapper.FeedstockBatchRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockProfileEvidenceRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockProfileSummaryRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto.FeedstockBatchCurrentProfileResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto.FeedstockBatchDetailDataQualityIndicatorResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto.FeedstockBatchDetailDataQualityResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto.FeedstockBatchDetailLastOperationResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto.FeedstockBatchDetailMeasurementFreshnessResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto.FeedstockBatchDetailPlanningReadinessCheckResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto.FeedstockBatchDetailPlanningReadinessResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto.FeedstockBatchDetailPotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto.FeedstockBatchDetailProfileSummaryResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto.FeedstockBatchDetailProfileValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto.FeedstockBatchDetailResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto.FeedstockBatchDetailSugarAcidityResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.mapper.FeedstockMeasurementRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.mapper.FeedstockBatchReadinessRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Maps feedstock detail application views to REST responses.
 *
 * <p>The mapper keeps transport concerns at the adapter boundary and delegates shared measurement
 * and profile-summary mapping to existing REST mappers.
 */
public final class FeedstockBatchDetailRestMapper {

  private FeedstockBatchDetailRestMapper() {
  }

  public static @NonNull FeedstockBatchDetailResponse toResponse(@NonNull FeedstockBatchDetailView view) {
    requireNonNull(view, "feedstock batch detail view cannot be null");
    return new FeedstockBatchDetailResponse(
        FeedstockBatchRestMapper.toResponse(view.batch()),
        view.currentMaterialStateId().value(),
        toResponse(view.currentProfile()),
        view.measurements().stream()
            .map(FeedstockMeasurementRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull FeedstockBatchCurrentProfileResponse toResponse(
      @NonNull FeedstockBatchCurrentProfileView view
  ) {
    requireNonNull(view, "feedstock batch current profile view cannot be null");
    final var latestMeasurement = view.latestMeasurement();
    return new FeedstockBatchCurrentProfileResponse(
        latestMeasurement == null ? null : FeedstockMeasurementRestMapper.toResponse(latestMeasurement),
        latestMeasurement == null ? null : toProfileSummaryResponse(latestMeasurement.chemicalProfile()),
        view.profileEvidence().stream()
            .map(FeedstockProfileEvidenceRestMapper::toResponse)
            .toList(),
        toSugarAcidityResponse(view.sugarAcidity()),
        toPotentialAbvResponse(view.potentialAbv()),
        toYeastAssimilableNitrogenResponse(view.yeastAssimilableNitrogen()),
        toMeasurementFreshnessResponse(view.measurementFreshness()),
        toResponse(view.planningReadiness()),
        toResponse(view.dataQuality()),
        view.fermentationStartReadiness() == null
            ? null
            : FeedstockBatchReadinessRestMapper.toResponse(view.fermentationStartReadiness()), // NOSONAR
        toLastOperationResponse(view.lastOperation())
    );
  }

  private static @Nullable FeedstockBatchDetailSugarAcidityResponse toSugarAcidityResponse(
      @Nullable SugarAcidityProfileEvaluation evaluation
  ) {
    if (evaluation == null) {
      return null;
    }

    final var assessment = evaluation.assessment();
    return new FeedstockBatchDetailSugarAcidityResponse(
        evaluation.plottable(),
        evaluation.point()
            .map(SugarAcidityZoneRestMapper::toResponse)
            .orElse(null), // NOSONAR
        assessment == null ? null : SugarAcidityZoneRestMapper.toResponse(assessment),
        ChemicalProfileCapabilityRestMapper.toResponse(evaluation.capabilityAssessment())
    );
  }

  private static @Nullable FeedstockBatchDetailPotentialAbvResponse toPotentialAbvResponse(
      @Nullable FeedstockCurrentProfilePotentialAbvView view
  ) {
    if (view == null) {
      return null;
    }

    return new FeedstockBatchDetailPotentialAbvResponse(
        view.originalGravityValueKind(),
        PotentialAbvPredictionRestMapper.toResponse(view.prediction())
    );
  }

  private static @Nullable SugarAcidityAxisPointResponse toYeastAssimilableNitrogenResponse(
      @Nullable AxisValue axisValue
  ) {
    if (axisValue == null) {
      return null;
    }

    return SugarAcidityZoneRestMapper.toResponse(
        new SugarAcidityAxisPoint(
            axisValue.value(),
            axisValue.valueKind(),
            axisValue.min(),
            axisValue.max()
        ),
        CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN
    );
  }

  private static @Nullable FeedstockBatchDetailMeasurementFreshnessResponse toMeasurementFreshnessResponse(
      @Nullable FeedstockCurrentMeasurementFreshnessView view
  ) {
    return view == null
        ? null
        : new FeedstockBatchDetailMeasurementFreshnessResponse(view.status(), view.stale(), view.reason());
  }

  private static @NonNull FeedstockBatchDetailPlanningReadinessResponse toResponse(
      @NonNull FeedstockPlanningReadinessView view
  ) {
    requireNonNull(view, "feedstock planning readiness view cannot be null");
    return new FeedstockBatchDetailPlanningReadinessResponse(
        view.status(),
        view.checks().stream()
            .map(FeedstockBatchDetailRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull FeedstockBatchDetailPlanningReadinessCheckResponse toResponse(
      @NonNull FeedstockPlanningReadinessCheck check
  ) {
    requireNonNull(check, "feedstock planning readiness check cannot be null");
    return new FeedstockBatchDetailPlanningReadinessCheckResponse(
        check.factor(),
        check.severity(),
        check.code(),
        check.message()
    );
  }

  private static @NonNull FeedstockBatchDetailDataQualityResponse toResponse(
      @NonNull FeedstockCurrentProfileDataQualityView view
  ) {
    requireNonNull(view, "feedstock data quality view cannot be null");
    return new FeedstockBatchDetailDataQualityResponse(
        view.status(),
        view.indicators().stream()
            .map(FeedstockBatchDetailRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull FeedstockBatchDetailDataQualityIndicatorResponse toResponse(
      @NonNull FeedstockCurrentProfileDataQualityIndicator indicator
  ) {
    requireNonNull(indicator, "feedstock data quality indicator cannot be null");
    return new FeedstockBatchDetailDataQualityIndicatorResponse(
        indicator.code(),
        indicator.severity(),
        indicator.message()
    );
  }

  private static @Nullable FeedstockBatchDetailLastOperationResponse toLastOperationResponse(
      @Nullable FeedstockBatchCurrentProfileOperationView view
  ) {
    return view == null ? null : new FeedstockBatchDetailLastOperationResponse(view.type(), view.occurredAt());
  }

  private static @NonNull FeedstockBatchDetailProfileSummaryResponse toProfileSummaryResponse(
      @NonNull ChemicalProfile profile
  ) {
    requireNonNull(profile, "chemical profile cannot be null");
    final var summary = FeedstockProfileSummaryRestMapper.toProfileSummary(
        profile,
        FeedstockBatchDetailProfileValueResponse::new
    );
    return new FeedstockBatchDetailProfileSummaryResponse(
        summary.sugar(),
        summary.titratableAcidity(),
        summary.ph(),
        summary.tannin(),
        summary.yan()
    );
  }
}
