package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.mapper;

import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.point.domain.SugarAcidityAxisPoint;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.TanninTargetRange;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.workspace.port.in.FeedstockTargetCandidateView;
import org.craft.mcs.cap.core.feedstock.application.workspace.port.in.FeedstockWorkspaceBatchView;
import org.craft.mcs.cap.core.feedstock.application.workspace.port.in.FeedstockWorkspaceBatchViewQuery;
import org.craft.mcs.cap.core.feedstock.application.workspace.port.in.FeedstockWorkspaceCurrentMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.workspace.port.in.FeedstockWorkspacePotentialAbvView;
import org.craft.mcs.cap.core.feedstock.application.workspace.port.in.FeedstockWorkspaceTargetAnalysisView;
import org.craft.mcs.cap.core.feedstock.application.workspace.port.in.FeedstockWorkspaceView;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
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
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspacePotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceSugarAcidityResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceTargetAnalysisResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.PredefinedZoneProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;


public final class FeedstockWorkspaceRestMapper {

  private FeedstockWorkspaceRestMapper() {
  }

  public static @NonNull FeedstockWorkspaceBatchViewQuery toQuery(
      @NonNull FeedstockWorkspaceBatchQueryRequest request
  ) {
    requireNonNull(request, "feedstock workspace batch view request cannot be null");
    final var purpose = purpose(request);
    return new FeedstockWorkspaceBatchViewQuery(
        CiderMakerId.of(request.ciderMakerId()),
        toSeason(request.season()),
        toTargetSelection(request.target()),
        toTanninTarget(request),
        ProfileTargetRestMapper.toWeights(request.weights()),
        purpose,
        requireNonNullElse(request.targetCandidateLimit(), CursorPageRequest.MAX_LIMIT)
    );
  }

  public static @NonNull FeedstockWorkspaceResponse toResponse(@NonNull FeedstockWorkspaceView view) {
    requireNonNull(view, "feedstock workspace view cannot be null");
    return new FeedstockWorkspaceResponse(
        view.batches().stream()
            .map(FeedstockWorkspaceRestMapper::toResponse)
            .toList(),
        toTargetAnalysisResponse(view.targetAnalysis())
    );
  }

  private static @NonNull FeedstockWorkspaceBatchResponse toResponse(
      @NonNull FeedstockWorkspaceBatchView view
  ) {
    requireNonNull(view, "feedstock workspace batch view cannot be null");
    return new FeedstockWorkspaceBatchResponse(
        FeedstockBatchRestMapper.toResponse(view.batch()),
        toCurrentMeasurementResponse(view.currentMeasurement()),
        toSugarAcidityResponse(view.sugarAcidity()),
        toPotentialAbvResponse(view.potentialAbv()),
        toYeastAssimilableNitrogenResponse(view.yeastAssimilableNitrogen()),
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
        assessmentResponse,
        ChemicalProfileCapabilityRestMapper.toResponse(evaluation.capabilityAssessment())
    );
  }

  private static @Nullable FeedstockWorkspaceCurrentMeasurementResponse toCurrentMeasurementResponse(
      @Nullable FeedstockWorkspaceCurrentMeasurementView view
  ) {
    return view == null ? null : toResponse(view);
  }

  private static @Nullable FeedstockWorkspacePotentialAbvResponse toPotentialAbvResponse(
      @Nullable FeedstockWorkspacePotentialAbvView view
  ) {
    return view == null ? null : toResponse(view);
  }

  private static @Nullable SugarAcidityAxisPointResponse toYeastAssimilableNitrogenResponse(
      @Nullable AxisValue axisValue
  ) {
    return axisValue == null ? null : toResponse(axisValue);
  }

  private static @Nullable FeedstockWorkspaceTargetCandidateResponse toTargetCandidateResponse(
      @Nullable FeedstockTargetCandidateView view
  ) {
    return view == null ? null : toResponse(view);
  }

  private static @Nullable FeedstockWorkspaceTargetAnalysisResponse toTargetAnalysisResponse(
      @Nullable FeedstockWorkspaceTargetAnalysisView view
  ) {
    return view == null ? null : toResponse(view);
  }

  private static @Nullable FermentationStartReadinessAssessmentResponse toFermentationStartReadinessResponse(
      @NonNull FeedstockWorkspaceBatchView view
  ) {
    final var readiness = view.fermentationStartReadiness();
    return readiness == null ? null : FeedstockBatchReadinessRestMapper.toResponse(readiness);
  }

  private static @NonNull FeedstockWorkspaceCurrentMeasurementResponse toResponse(
      @NonNull FeedstockWorkspaceCurrentMeasurementView view
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
      @NonNull FeedstockWorkspacePotentialAbvView view
  ) {
    requireNonNull(view, "feedstock workspace potential ABV view cannot be null");
    return new FeedstockWorkspacePotentialAbvResponse(
        view.originalGravityValueKind(),
        PotentialAbvPredictionRestMapper.toResponse(view.prediction())
    );
  }

  private static @NonNull FeedstockWorkspaceTargetCandidateResponse toResponse(
      @NonNull FeedstockTargetCandidateView view
  ) {
    requireNonNull(view, "feedstock target candidate view cannot be null");
    return new FeedstockWorkspaceTargetCandidateResponse(
        SugarAcidityZoneRestMapper.toResponse(view.point()),
        ProfileTargetRestMapper.toResponse(view.distance()),
        requireNonNull(
            CompositionContributionRoleRestMapper.toResponse(view.contributionRole()),
            "composition contribution role response cannot be null"
        )
    );
  }

  private static @NonNull FeedstockWorkspaceTargetAnalysisResponse toResponse(
      @NonNull FeedstockWorkspaceTargetAnalysisView view
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
