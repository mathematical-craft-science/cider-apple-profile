package org.craft.mcs.cap.core.feedstock.application.usecase;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidate;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateReference;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.feedstock.application.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.port.in.ScoreFeedstockBatchProfileTargetCommand;
import org.craft.mcs.cap.core.feedstock.application.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.platform.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Selects latest feedstock batch measurements that can participate in profile-target scoring.
 *
 * <p>The provider enforces the feedstock workspace rule: profile-target candidates are based on
 * the latest measurement only. It does not calculate distance or contribution roles; it only builds
 * the normalized candidate input shared by the score endpoint and analysis candidate endpoint.
 */
@Component
@RequiredArgsConstructor
public final class FeedstockBatchProfileTargetScorableCandidateProvider {

  private static final @NonNull String REFERENCE_TYPE = "feedstock-batch";

  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull SugarAcidityZoneAssessor zoneAssessor;

  @NonNull List<@NonNull FeedstockBatchProfileTargetScorableCandidate> findCandidates(
      @NonNull ScoreFeedstockBatchProfileTargetCommand command
  ) {
    requireNonNull(command, "score feedstock batch profile target command cannot be null");

    return currentMeasurementQueryService.findCurrentSugarAcidityPointCandidates(
            new LatestFeedstockMeasurementSugarAcidityPointCandidateQuery(
                command.ciderMakerId(),
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
      @NonNull FeedstockMeasurement measurement,
      @NonNull ScoreFeedstockBatchProfileTargetCommand command
  ) {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    final var axes = axisExtractor.extract(measurement.chemicalProfile());
    if (!capabilityAssessor.assess(axes, ChemicalProfileCapability.SUGAR_ACIDITY_PLOT).capable()) {
      return Optional.empty();
    }
    if (!matchesCapability(axes, command.filters().capability())) {
      return Optional.empty();
    }

    final var assessment = zoneAssessor.assess(axes).orElse(null); // NOSONAR
    if (assessment == null || !matchesSugarAcidityZone(assessment, command.filters().sugarAcidityZone())) {
      return Optional.empty();
    }

    return Optional.of(new FeedstockBatchProfileTargetScorableCandidate(
        measurement,
        axes,
        new ProfileTargetDistanceCandidate(
            referenceId(measurement),
            assessment.point()
        )
    ));
  }

  private boolean matchesCapability(
      @NonNull ChemicalProfileAxes axes,
      @Nullable ChemicalProfileCapabilityCriteria criteria
  ) {
    if (criteria == null || criteria.capable() == null) {
      return true;
    }
    return capabilityAssessor.assess(axes, criteria.capability()).capable() == criteria.capable(); // NOSONAR
  }

  private static boolean matchesSugarAcidityZone(
      @NonNull SugarAcidityZoneAssessment assessment,
      @Nullable SugarAcidityZoneCriteria criteria
  ) {
    return criteria == null || criteria.zones().contains(assessment.zone());
  }

  private static @NonNull ProfileTargetDistanceCandidateReference referenceId(
      @NonNull FeedstockMeasurement measurement
  ) {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    return new ProfileTargetDistanceCandidateReference(
        REFERENCE_TYPE,
        measurement.batchId().value().toString()
    );
  }
}
