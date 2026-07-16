package org.craft.mcs.cap.core.appleprofile.application.profiletarget.usecase;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.AppleVarietyProfileTargetCandidate;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.AppleVarietyProfileTargetCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.AppleVarietyProfileTargetCandidates;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.QueryAppleVarietyProfileTargetCandidatesUseCase;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateRepository;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateView;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessmentRequest;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessor;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidate;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateReference;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesCommand;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesUseCase;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.application.model.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Analysis read model for target-aware apple variety candidates.
 *
 * <p>This use case is intentionally separate from generic catalog graph endpoints. It requires a
 * selected profile target and enriches plottable varieties with target distance and contribution
 * role for target-aware analysis clients.
 */
@Component
@RequiredArgsConstructor
public final class QueryAppleVarietyProfileTargetCandidatesService
    implements QueryAppleVarietyProfileTargetCandidatesUseCase {

  private static final @NonNull String REFERENCE_TYPE = "apple-variety";
  private static final @NonNull String APPLE_VARIETY_REQUIRED = "apple variety cannot be null";

  private final @NonNull AppleVarietySugarAcidityPointCandidateRepository candidateRepository;
  private final @NonNull SugarAcidityProfileProjector sugarAcidityProfileProjector;
  private final @NonNull ScoreProfileTargetDistanceCandidatesUseCase scoreProfileTargetDistanceCandidatesUseCase;
  private final @NonNull CompositionContributionRoleAssessor contributionRoleAssessor;

  @Override
  public @NonNull AppleVarietyProfileTargetCandidates query(
      @NonNull AppleVarietyProfileTargetCandidateQuery query
  ) {
    requireNonNull(query, "apple variety profile target candidate query cannot be null");

    final var candidates = candidateRepository.findCandidates(
            new AppleVarietySugarAcidityPointCandidateQuery(
                query.filters().nameContains(),
                null,
                query.limit()
            )
        )
        .stream()
        .map(variety -> toCandidateIfScorable(variety, query))
        .flatMap(Optional::stream)
        .toList();

    final Map<@NonNull String, @NonNull ScorableAppleVarietyCandidate> candidatesByReference = candidates.stream()
        .collect(Collectors.toMap(
            candidate -> candidate.candidate().reference().id(),
            Function.identity()
        ));
    final var scores = scoreProfileTargetDistanceCandidatesUseCase.score(
        new ScoreProfileTargetDistanceCandidatesCommand(
            query.purpose(),
            query.target(),
            query.weights(),
            candidates.stream()
                .map(ScorableAppleVarietyCandidate::candidate)
                .toList()
        )
    );

    return new AppleVarietyProfileTargetCandidates(
        scores.purpose(),
        scores.normalizerCode(),
        scores.scores().stream()
            .map(score -> {
              final var candidate = requireNonNull(
                  candidatesByReference.get(score.reference().id()),
                  "apple variety candidate is missing for score"
              );
              final var variety = candidate.variety();
              final var roleAssessmentRequest = new CompositionContributionRoleAssessmentRequest(
                  candidate.axes(),
                  query.target()
              );
              return new AppleVarietyProfileTargetCandidate(
                  variety.varietyId(),
                  variety.name(),
                  candidate.candidate().point(),
                  score.distance(),
                  contributionRoleAssessor.assess(roleAssessmentRequest)
              );
            })
            .toList()
    );
  }

  private @NonNull Optional<@NonNull ScorableAppleVarietyCandidate> toCandidateIfScorable(
      @NonNull AppleVarietySugarAcidityPointCandidateView variety,
      @NonNull AppleVarietyProfileTargetCandidateQuery query
  ) {
    requireNonNull(variety, APPLE_VARIETY_REQUIRED);
    final var evaluation = sugarAcidityProfileProjector.project(variety.chemicalProfile());
    if (!evaluation.plottable()) {
      return Optional.empty();
    }
    if (!matchesCapability(evaluation, query.filters().capability())) {
      return Optional.empty();
    }

    final var assessment = evaluation.assessment();
    if (assessment == null || !matchesSugarAcidityZone(assessment, query.filters().sugarAcidityZone())) {
      return Optional.empty();
    }
    final var point = evaluation.point().orElseThrow();

    return Optional.of(new ScorableAppleVarietyCandidate(
        variety,
        evaluation.axes(),
        new ProfileTargetDistanceCandidate(
            referenceId(variety),
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
        == Boolean.TRUE.equals(criteria.capable());
  }

  private static boolean matchesSugarAcidityZone(
      @NonNull SugarAcidityZoneAssessment assessment,
      @Nullable SugarAcidityZoneCriteria criteria
  ) {
    return criteria == null || criteria.zones().contains(assessment.zone());
  }

  private static @NonNull ProfileTargetDistanceCandidateReference referenceId(
      @NonNull AppleVarietySugarAcidityPointCandidateView variety
  ) {
    requireNonNull(variety, APPLE_VARIETY_REQUIRED);
    return new ProfileTargetDistanceCandidateReference(
        REFERENCE_TYPE,
        variety.varietyId().value().toString()
    );
  }

  private record ScorableAppleVarietyCandidate(
      @NonNull AppleVarietySugarAcidityPointCandidateView variety,
      @NonNull ChemicalProfileAxes axes,
      @NonNull ProfileTargetDistanceCandidate candidate
  ) {

    private ScorableAppleVarietyCandidate {
      requireNonNull(variety, APPLE_VARIETY_REQUIRED);
      requireNonNull(axes, "chemical profile axes cannot be null");
      requireNonNull(candidate, "profile target distance candidate cannot be null");
    }
  }
}
