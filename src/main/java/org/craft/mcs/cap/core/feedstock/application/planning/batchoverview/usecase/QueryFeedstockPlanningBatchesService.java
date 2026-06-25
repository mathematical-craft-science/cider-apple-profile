package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.usecase;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetCandidate;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.ListFeedstockBatchesQuery;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.ListFeedstockBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.QueryFeedstockBatchProfileTargetCandidatesUseCase;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockCurrentMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockCurrentProfilePotentialAbvView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.usecase.FeedstockCurrentProfileAssessmentService;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningTargetAnalysisView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningTargetCandidateView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningBatchView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningBatchViewQuery;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.QueryFeedstockPlanningBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application read model for the feedstock planning.
 *
 * <p>This query service deliberately composes batch metadata, latest measurement state, graph
 * projection, target candidate scoring, ABV, YAN and fermentation-start readiness into one
 * application read model. It is not a domain aggregate; its responsibility is to give API clients a
 * coherent planning snapshot without forcing them to stitch several business endpoints
 * together.
 */
@Component
@RequiredArgsConstructor
public final class QueryFeedstockPlanningBatchesService implements QueryFeedstockPlanningBatchesUseCase {

  private final @NonNull ListFeedstockBatchesUseCase listFeedstockBatchesUseCase;
  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull QueryFeedstockBatchProfileTargetCandidatesUseCase queryTargetCandidatesUseCase;
  private final @NonNull SugarAcidityProfileProjector sugarAcidityProfileProjector;
  private final @NonNull PotentialAbvPredictor potentialAbvPredictor;
  private final @NonNull FermentationStartReadinessAssessor fermentationStartReadinessAssessor;
  private final @NonNull FeedstockCurrentProfileAssessmentService batchAssessmentService;
  private final @NonNull FeedstockPlanningContributionRoleExplainer contributionRoleExplainer;

  @Override
  public @NonNull FeedstockPlanningView query(@NonNull FeedstockPlanningBatchViewQuery query) {
    requireNonNull(query, "feedstock planning batch view query cannot be null");

    final var batches = listFeedstockBatchesUseCase.list(new ListFeedstockBatchesQuery(
        query.ciderMakerId(),
        query.season()
    ));
    final var batchIds = batches.stream()
        .map(FeedstockBatchSummary::batchId)
        .collect(Collectors.toSet());
    final var measurementsByBatchId = currentMeasurementQueryService.findCurrentMeasurementsByBatchIds(batchIds)
        .stream()
        .collect(Collectors.toMap(
            FeedstockMeasurement::batchId,
            Function.identity()
        ));
    final var targetCandidates = targetCandidates(query);

    return new FeedstockPlanningView(
        batches.stream()
            .map(batch -> toBatchView(
                batch,
                measurementsByBatchId.get(batch.batchId()),
                targetCandidates.candidatesByBatchId().get(batch.batchId())
            ))
            .toList(),
        targetCandidates.analysis()
    );
  }

  private @NonNull FeedstockPlanningBatchView toBatchView(
      @NonNull FeedstockBatchSummary batch,
      @Nullable FeedstockMeasurement measurement,
      @Nullable FeedstockPlanningTargetCandidateView targetCandidate
  ) {
    if (measurement == null) {
      return new FeedstockPlanningBatchView(
          batch,
          null,
          null,
          null,
          null,
          null,
          batchAssessmentService.planningReadinessWithoutMeasurement(batch),
          batchAssessmentService.dataQualityWithoutMeasurement(),
          null,
          targetCandidate
      );
    }

    final var evaluation = sugarAcidityProfileProjector.project(measurement.chemicalProfile());
    final var freshness = batchAssessmentService.freshness(measurement);
    return new FeedstockPlanningBatchView(
        batch,
        toCurrentMeasurement(measurement),
        freshness,
        evaluation,
        potentialAbv(evaluation),
        yeastAssimilableNitrogen(evaluation),
        batchAssessmentService.planningReadiness(batch, measurement.chemicalProfile()),
        batchAssessmentService.dataQuality(measurement.chemicalProfile(), evaluation, freshness),
        fermentationStartReadinessAssessor.assess(measurement.chemicalProfile()),
        targetCandidate
    );
  }

  private @NonNull TargetCandidates targetCandidates(@NonNull FeedstockPlanningBatchViewQuery query) {
    final var target = query.target();
    if (target == null) {
      return TargetCandidates.empty();
    }

    final var candidates = queryTargetCandidatesUseCase.query(new FeedstockBatchProfileTargetCandidateQuery(
        query.ciderMakerId(),
        new FeedstockMeasurementPointFilters(null, query.season(), null, null),
        query.purpose(),
        target,
        query.tanninTarget(),
        query.weights(),
        query.targetCandidateLimit()
    ));

    return new TargetCandidates(
        new FeedstockPlanningTargetAnalysisView(candidates.purpose(), candidates.normalizerCode()),
        candidates.candidates().stream()
            .collect(Collectors.toMap(
                FeedstockBatchProfileTargetCandidate::batchId,
                this::toTargetCandidateView
            ))
    );
  }

  private static @NonNull FeedstockCurrentMeasurementView toCurrentMeasurement(
      @NonNull FeedstockMeasurement measurement
  ) {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    return new FeedstockCurrentMeasurementView(
        measurement.measurementId(),
        measurement.name(),
        measurement.measuredAt(),
        measurement.chemicalProfile(),
        measurement.note()
    );
  }

  private @Nullable FeedstockCurrentProfilePotentialAbvView potentialAbv(
      @NonNull SugarAcidityProfileEvaluation evaluation
  ) {
    final var capability = sugarAcidityProfileProjector.assessCapability(
        evaluation,
        ChemicalProfileCapability.ABV_PREDICTION
    );
    if (!capability.capable()) {
      return null;
    }

    final var sugar = evaluation.axes().valueOf(ChemicalAxis.SUGAR)
        .orElseThrow(() -> new IllegalStateException("ABV-capable profile must have sugar axis"));
    return new FeedstockCurrentProfilePotentialAbvView(
        sugar.valueKind(),
        potentialAbvPredictor.predict(SpecificGravityValue.of(sugar.value()))
    );
  }

  private static @Nullable AxisValue yeastAssimilableNitrogen(@NonNull SugarAcidityProfileEvaluation evaluation) {
    final var axis = evaluation.axes().valueOf(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN);
    return axis.orElse(null); // NOSONAR
  }

  private @NonNull FeedstockPlanningTargetCandidateView toTargetCandidateView(
      @NonNull FeedstockBatchProfileTargetCandidate candidate
  ) {
    requireNonNull(candidate, "feedstock batch profile target candidate cannot be null");
    return new FeedstockPlanningTargetCandidateView(
        candidate.point(),
        candidate.distance(),
        candidate.contributionRole(),
        contributionRoleExplainer.explain(candidate.contributionRole())
    );
  }

  private record TargetCandidates(
      @Nullable FeedstockPlanningTargetAnalysisView analysis,
      @NonNull Map<@NonNull FeedstockBatchId, @NonNull FeedstockPlanningTargetCandidateView> candidatesByBatchId
  ) {

    private TargetCandidates {
      candidatesByBatchId = Map.copyOf(requireNonNull(candidatesByBatchId, "target candidates cannot be null"));
    }

    private static @NonNull TargetCandidates empty() {
      return new TargetCandidates(null, Map.of());
    }
  }
}
