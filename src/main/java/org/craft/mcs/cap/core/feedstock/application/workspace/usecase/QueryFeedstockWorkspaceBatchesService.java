package org.craft.mcs.cap.core.feedstock.application.workspace.usecase;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.cideranalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetCandidate;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.ListFeedstockBatchesQuery;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.ListFeedstockBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.QueryFeedstockBatchProfileTargetCandidatesUseCase;
import org.craft.mcs.cap.core.feedstock.application.workspace.port.in.FeedstockTargetCandidateView;
import org.craft.mcs.cap.core.feedstock.application.workspace.port.in.FeedstockWorkspaceBatchView;
import org.craft.mcs.cap.core.feedstock.application.workspace.port.in.FeedstockWorkspaceBatchViewQuery;
import org.craft.mcs.cap.core.feedstock.application.workspace.port.in.FeedstockWorkspaceCurrentMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.workspace.port.in.FeedstockWorkspacePotentialAbvView;
import org.craft.mcs.cap.core.feedstock.application.workspace.port.in.FeedstockWorkspaceTargetAnalysisView;
import org.craft.mcs.cap.core.feedstock.application.workspace.port.in.FeedstockWorkspaceView;
import org.craft.mcs.cap.core.feedstock.application.workspace.port.in.QueryFeedstockWorkspaceBatchesUseCase;
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
 * Application read model for the Feedstock Workspace.
 *
 * <p>This query service deliberately composes batch metadata, latest measurement state, graph
 * projection, target candidate scoring, ABV, YAN and fermentation-start readiness into one
 * application read model. It is not a domain aggregate; its responsibility is to give API clients a
 * coherent workspace snapshot without forcing them to stitch several business endpoints
 * together.
 */
@Component
@RequiredArgsConstructor
public final class QueryFeedstockWorkspaceBatchesService implements QueryFeedstockWorkspaceBatchesUseCase {

  private static final @NonNull SpecificGravityValue DRY_CIDER_FINAL_GRAVITY = SpecificGravityValue.of("1.000");

  private final @NonNull ListFeedstockBatchesUseCase listFeedstockBatchesUseCase;
  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull QueryFeedstockBatchProfileTargetCandidatesUseCase queryTargetCandidatesUseCase;
  private final @NonNull SugarAcidityProfileProjector sugarAcidityProfileProjector;
  private final @NonNull PotentialAbvPredictor potentialAbvPredictor;
  private final @NonNull FermentationStartReadinessAssessor fermentationStartReadinessAssessor;

  @Override
  public @NonNull FeedstockWorkspaceView query(@NonNull FeedstockWorkspaceBatchViewQuery query) {
    requireNonNull(query, "feedstock workspace batch view query cannot be null");

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

    return new FeedstockWorkspaceView(
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

  private @NonNull FeedstockWorkspaceBatchView toBatchView(
      @NonNull FeedstockBatchSummary batch,
      @Nullable FeedstockMeasurement measurement,
      @Nullable FeedstockTargetCandidateView targetCandidate
  ) {
    if (measurement == null) {
      return new FeedstockWorkspaceBatchView(batch, null, null, null, null, null, targetCandidate);
    }

    final var evaluation = sugarAcidityProfileProjector.project(measurement.chemicalProfile());
    return new FeedstockWorkspaceBatchView(
        batch,
        toCurrentMeasurement(measurement),
        evaluation,
        potentialAbv(evaluation),
        yeastAssimilableNitrogen(evaluation),
        fermentationStartReadinessAssessor.assess(measurement.chemicalProfile()),
        targetCandidate
    );
  }

  private @NonNull TargetCandidates targetCandidates(@NonNull FeedstockWorkspaceBatchViewQuery query) {
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
        new FeedstockWorkspaceTargetAnalysisView(candidates.purpose(), candidates.normalizerCode()),
        candidates.candidates().stream()
            .collect(Collectors.toMap(
                FeedstockBatchProfileTargetCandidate::batchId,
                QueryFeedstockWorkspaceBatchesService::toTargetCandidateView
            ))
    );
  }

  private static @NonNull FeedstockWorkspaceCurrentMeasurementView toCurrentMeasurement(
      @NonNull FeedstockMeasurement measurement
  ) {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    return new FeedstockWorkspaceCurrentMeasurementView(
        measurement.measurementId(),
        measurement.name(),
        measurement.measuredAt(),
        measurement.chemicalProfile(),
        measurement.note()
    );
  }

  private @Nullable FeedstockWorkspacePotentialAbvView potentialAbv(@NonNull SugarAcidityProfileEvaluation evaluation) {
    final var capability = sugarAcidityProfileProjector.assessCapability(
        evaluation,
        ChemicalProfileCapability.ABV_PREDICTION
    );
    if (!capability.capable()) {
      return null;
    }

    final var sugar = evaluation.axes().valueOf(ChemicalAxis.SUGAR)
        .orElseThrow(() -> new IllegalStateException("ABV-capable profile must have sugar axis"));
    return new FeedstockWorkspacePotentialAbvView(
        sugar.valueKind(),
        potentialAbvPredictor.predict(SpecificGravityValue.of(sugar.value()), DRY_CIDER_FINAL_GRAVITY)
    );
  }

  private static @Nullable AxisValue yeastAssimilableNitrogen(@NonNull SugarAcidityProfileEvaluation evaluation) {
    final var axis = evaluation.axes().valueOf(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN);
    return axis.orElse(null); // NOSONAR
  }

  private static @NonNull FeedstockTargetCandidateView toTargetCandidateView(
      @NonNull FeedstockBatchProfileTargetCandidate candidate
  ) {
    requireNonNull(candidate, "feedstock batch profile target candidate cannot be null");
    return new FeedstockTargetCandidateView(
        candidate.point(),
        candidate.distance(),
        candidate.contributionRole()
    );
  }

  private record TargetCandidates(
      @Nullable FeedstockWorkspaceTargetAnalysisView analysis,
      @NonNull Map<@NonNull FeedstockBatchId, @NonNull FeedstockTargetCandidateView> candidatesByBatchId
  ) {

    private TargetCandidates {
      candidatesByBatchId = Map.copyOf(requireNonNull(candidatesByBatchId, "target candidates cannot be null"));
    }

    private static @NonNull TargetCandidates empty() {
      return new TargetCandidates(null, Map.of());
    }
  }
}
