package org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.usecase;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidate;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateReference;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.application.model.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.ScoreFeedstockBatchProfileTargetCommand;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.CurrentFeedstockBatchSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Selects current Feedstock material states that can participate in profile-target scoring.
 *
 * <p>The provider uses state-aware current evidence and only builds the normalized candidate input
 * shared by the score endpoint and analysis candidate endpoint. It does not calculate distance or
 * contribution roles.
 */
@Component
@RequiredArgsConstructor
public final class FeedstockBatchProfileTargetScorableCandidateProvider {

  private static final @NonNull String REFERENCE_TYPE = "feedstock-batch";

  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull SugarAcidityProfileProjector sugarAcidityProfileProjector;

  @NonNull List<@NonNull FeedstockBatchProfileTargetScorableCandidate> findCandidates(
      @NonNull ScoreFeedstockBatchProfileTargetCommand command
  ) {
    requireNonNull(command, "score feedstock batch profile target command cannot be null");

    return currentMeasurementQueryService.findCurrentSugarAcidityPointCandidates(
            new CurrentFeedstockBatchSugarAcidityPointCandidateQuery(
                command.workspaceId(),
                command.filters().batch(),
                command.filters().season(),
                null,
                command.limit()
            )
        )
        .stream()
        .map(measurement -> toCandidateIfScorable(measurement, command))
        .flatMap(Optional::stream)
        .toList();
  }

  private @NonNull Optional<@NonNull FeedstockBatchProfileTargetScorableCandidate> toCandidateIfScorable(
      @NonNull FeedstockMeasurementView measurement,
      @NonNull ScoreFeedstockBatchProfileTargetCommand command
  ) {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    final var evaluation = sugarAcidityProfileProjector.project(measurement.chemicalProfile());
    if (!evaluation.plottable()) {
      return Optional.empty();
    }
    if (!matchesCapability(evaluation, command.filters().capability())) {
      return Optional.empty();
    }

    final var assessment = evaluation.assessment();
    if (assessment == null || !matchesSugarAcidityZone(assessment, command.filters().sugarAcidityZone())) {
      return Optional.empty();
    }
    final var point = evaluation.point().orElseThrow();

    return Optional.of(new FeedstockBatchProfileTargetScorableCandidate(
        measurement,
        evaluation.axes(),
        new ProfileTargetDistanceCandidate(
            referenceId(measurement),
            point
        )
    ));
  }

  private boolean matchesCapability(
      @NonNull SugarAcidityProfileEvaluation evaluation,
      @Nullable ChemicalProfileCapabilityCriteria criteria
  ) {
    if (criteria == null || criteria.capable() == null) {
      return true;
    }
    return sugarAcidityProfileProjector.assessCapability(evaluation, criteria.capability()).capable()
        == criteria.capable(); // NOSONAR
  }

  private static boolean matchesSugarAcidityZone(
      @NonNull SugarAcidityZoneAssessment assessment,
      @Nullable SugarAcidityZoneCriteria criteria
  ) {
    return criteria == null || criteria.zones().contains(assessment.zone());
  }

  private static @NonNull ProfileTargetDistanceCandidateReference referenceId(
      @NonNull FeedstockMeasurementView measurement
  ) {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    return new ProfileTargetDistanceCandidateReference(
        REFERENCE_TYPE,
        measurement.batchId().value().toString()
    );
  }
}
