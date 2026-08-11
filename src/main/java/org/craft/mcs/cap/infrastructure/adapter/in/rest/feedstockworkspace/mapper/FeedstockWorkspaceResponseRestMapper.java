package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.mapper;

import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningTargetCandidateView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningBatchView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSummaryView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningTargetAnalysisView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningView;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessment;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper.CompositionContributionRoleRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.dto.FermentationStartReadinessAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.mapper.FeedstockBatchReadinessRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.mapper.FeedstockBatchRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockProfileEvidenceRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.mapper.FeedstockCurrentProfileRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceTargetCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceCurrentMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceTargetAnalysisResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetScoringCharacteristicsRestMapper;
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
        view.batch().batch().currentMaterialStateId().value(),
        toCurrentMeasurementResponse(view.currentProfileSummary()),
        FeedstockCurrentProfileRestMapper.toProfileSummary(view.currentProfileSummary()),
        assessment.profileEvidence().stream()
            .map(FeedstockProfileEvidenceRestMapper::toResponse)
            .toList(),
        FeedstockCurrentProfileRestMapper.toFreshness(assessment.freshness()),
        FeedstockCurrentProfileRestMapper.toGraph(
            assessment.graphEvaluation(),
            assessment.northAmericanTanninLevel(),
            view.currentProfileSummary()
        ),
        FeedstockCurrentProfileRestMapper.toPotentialAbv(assessment.potentialAbv()),
        FeedstockCurrentProfileRestMapper.toPlanningReadiness(assessment.planningReadiness()),
        FeedstockCurrentProfileRestMapper.toDataQuality(assessment.dataQuality()),
        toFermentationStartReadinessResponse(assessment.fermentationStartReadiness()),
        toTargetCandidateResponse(view.targetCandidate())
    );
  }

  private static @Nullable FeedstockWorkspaceCurrentMeasurementResponse toCurrentMeasurementResponse(
      @Nullable FeedstockCurrentProfileSummaryView view
  ) {
    return view == null ? null : toResponse(view);
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
    final var latestMeasurement = view.latestMeasurementSet();
    return new FeedstockWorkspaceCurrentMeasurementResponse(
        latestMeasurement.measurementSetId(),
        latestMeasurement.name(),
        latestMeasurement.measuredAt(),
        latestMeasurement.note()
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

  private static @NonNull FeedstockWorkspaceTargetAnalysisResponse toResponse(
      @NonNull FeedstockPlanningTargetAnalysisView view
  ) {
    requireNonNull(view, "feedstock workspace target analysis view cannot be null");
    return new FeedstockWorkspaceTargetAnalysisResponse(view.analysisScenario(), view.normalizerCode());
  }

}
