package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.mapper;

import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessment;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningTargetCandidateView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningBatchView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSummaryView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityIndicator;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileFreshnessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessCheck;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfilePotentialAbvView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningTargetAnalysisView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningView;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessment;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper.CompositionContributionRoleRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileCapabilityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileResponseRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.dto.FermentationStartReadinessAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.mapper.FeedstockBatchReadinessRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.mapper.FeedstockBatchRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockProfileEvidenceRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockProfileSummaryRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceTargetCandidateResponse;
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
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceSugarAcidityResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceTargetAnalysisResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetScoringCharacteristicsRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.ProfileCharacteristicValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.mapper.ProfileCharacteristicRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.ProfileGraphRestMapper;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class FeedstockWorkspaceResponseRestMapper {

  private FeedstockWorkspaceResponseRestMapper() {
  }

  public static @NonNull FeedstockWorkspaceResponse toResponse(@NonNull FeedstockPlanningView view) {
    requireNonNull(view, "feedstock workspace view cannot be null");
    return new FeedstockWorkspaceResponse(
        view.batches().stream()
            .map(FeedstockWorkspaceResponseRestMapper::toResponse)
            .toList(),
        toTargetAnalysisResponse(view.targetAnalysis())
    );
  }

  private static @NonNull FeedstockWorkspaceBatchResponse toResponse(
      @NonNull FeedstockPlanningBatchView view
  ) {
    requireNonNull(view, "feedstock workspace batch view cannot be null");
    final var assessment = view.currentProfileAssessment();
    return new FeedstockWorkspaceBatchResponse(
        FeedstockBatchRestMapper.toResponse(view.batch()),
        view.batch().currentMaterialStateId().value(),
        toCurrentMeasurementResponse(view.currentProfileSummary()),
        toProfileSummaryResponse(view.currentProfileSummary()),
        assessment.profileEvidence().stream()
            .map(FeedstockProfileEvidenceRestMapper::toResponse)
            .toList(),
        toMeasurementFreshnessResponse(assessment.freshness()),
        toSugarAcidityResponse(assessment.profileAssessment()),
        toPotentialAbvResponse(assessment.potentialAbv()),
        toYeastAssimilableNitrogenResponse(assessment.yeastAssimilableNitrogen()),
        toResponse(assessment.planningReadiness()),
        toResponse(assessment.dataQuality()),
        toFermentationStartReadinessResponse(assessment.fermentationStartReadiness()),
        toTargetCandidateResponse(view.targetCandidate())
    );
  }

  private static @Nullable FeedstockWorkspaceSugarAcidityResponse toSugarAcidityResponse(
      @Nullable ChemicalProfileAssessment evaluation
  ) {
    if (evaluation == null) {
      return null;
    }

    final var assessment = evaluation.assessment();
    final var assessmentResponse = assessment == null
        ? null
        : ProfileGraphRestMapper.toResponse(evaluation);
    return new FeedstockWorkspaceSugarAcidityResponse(
        evaluation.plottable(),
        assessment == null ? null : ProfileGraphRestMapper.toResponse(evaluation.axes()),
        assessmentResponse,
        ChemicalProfileCapabilityRestMapper.toResponse(evaluation.capabilityAssessment())
    );
  }

  private static @Nullable FeedstockWorkspaceCurrentMeasurementResponse toCurrentMeasurementResponse(
      @Nullable FeedstockCurrentProfileSummaryView view
  ) {
    return view == null ? null : toResponse(view);
  }

  private static @Nullable FeedstockWorkspaceProfileSummaryResponse toProfileSummaryResponse(
      @Nullable FeedstockCurrentProfileSummaryView view
  ) {
    return view == null ? null : toProfileSummaryResponse(view.chemicalProfile());
  }

  private static @NonNull FeedstockWorkspaceProfileSummaryResponse toProfileSummaryResponse(
      @NonNull ChemicalProfile profile
  ) {
    requireNonNull(profile, "chemical profile cannot be null");
    final var summary = FeedstockProfileSummaryRestMapper.toProfileSummary(
        profile,
        FeedstockWorkspaceProfileValueResponse::new
    );
    return new FeedstockWorkspaceProfileSummaryResponse(
        summary.specificGravity(),
        summary.titratableAcidity(),
        summary.ph(),
        summary.tannin(),
        summary.yan()
    );
  }

  private static @Nullable FeedstockWorkspacePotentialAbvResponse toPotentialAbvResponse(
      @Nullable FeedstockCurrentProfilePotentialAbvView view
  ) {
    return view == null ? null : toResponse(view);
  }

  private static @Nullable FeedstockWorkspaceMeasurementFreshnessResponse toMeasurementFreshnessResponse(
      @Nullable FeedstockCurrentProfileFreshnessView view
  ) {
    return view == null ? null : toResponse(view);
  }

  private static @Nullable ProfileCharacteristicValueResponse toYeastAssimilableNitrogenResponse(
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
      @Nullable FermentationStartReadinessAssessment readiness
  ) {
    return readiness == null ? null : FeedstockBatchReadinessRestMapper.toResponse(readiness);
  }

  private static @NonNull FeedstockWorkspaceCurrentMeasurementResponse toResponse(
      @NonNull FeedstockCurrentProfileSummaryView view
  ) {
    requireNonNull(view, "feedstock workspace current profile summary cannot be null");
    final var latestMeasurement = view.latestMeasurement();
    return new FeedstockWorkspaceCurrentMeasurementResponse(
        latestMeasurement.measurementId().value(),
        latestMeasurement.name().value(),
        latestMeasurement.measuredAt(),
        ChemicalProfileResponseRestMapper.toResponse(view.chemicalProfile()),
        latestMeasurement.note()
    );
  }

  private static @NonNull FeedstockWorkspacePotentialAbvResponse toResponse(
      @NonNull FeedstockCurrentProfilePotentialAbvView view
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
        view.currentMaterialStateId().value(),
        ProfileTargetScoringCharacteristicsRestMapper.toResponse(view.characteristics()),
        ProfileTargetRestMapper.toResponse(view.distance()),
        requireNonNull(
            CompositionContributionRoleRestMapper.toResponse(view.contributionRole()),
            "composition contribution role response cannot be null"
        )
    );
  }

  private static @NonNull FeedstockWorkspaceMeasurementFreshnessResponse toResponse(
      @NonNull FeedstockCurrentProfileFreshnessView view
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
            .map(FeedstockWorkspaceResponseRestMapper::toResponse)
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
      @NonNull FeedstockCurrentProfileDataQualityView view
  ) {
    requireNonNull(view, "feedstock workspace data quality view cannot be null");
    return new FeedstockWorkspaceDataQualityResponse(
        view.status(),
        view.indicators().stream()
            .map(FeedstockWorkspaceResponseRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull FeedstockWorkspaceDataQualityIndicatorResponse toResponse(
      @NonNull FeedstockCurrentProfileDataQualityIndicator indicator
  ) {
    requireNonNull(indicator, "feedstock workspace data quality indicator cannot be null");
    return new FeedstockWorkspaceDataQualityIndicatorResponse(
        indicator.code(),
        indicator.severity(),
        indicator.message()
    );
  }

  private static @NonNull FeedstockWorkspaceTargetAnalysisResponse toResponse(
      @NonNull FeedstockPlanningTargetAnalysisView view
  ) {
    requireNonNull(view, "feedstock workspace target analysis view cannot be null");
    return new FeedstockWorkspaceTargetAnalysisResponse(view.analysisScenario(), view.normalizerCode());
  }

  private static @NonNull ProfileCharacteristicValueResponse toResponse(@NonNull AxisValue axisValue) {
    requireNonNull(axisValue, "chemical axis value cannot be null");
    return ProfileCharacteristicRestMapper.toResponse(
        axisValue,
        CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN
    );
  }

}
