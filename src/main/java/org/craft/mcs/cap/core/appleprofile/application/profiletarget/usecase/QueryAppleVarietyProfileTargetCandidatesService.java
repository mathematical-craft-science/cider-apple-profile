package org.craft.mcs.cap.core.appleprofile.application.profiletarget.usecase;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.catalog.model.AppleVarietyCatalogPagePosition;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.AppleVarietyProfileTargetCandidate;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.AppleVarietyProfileTargetCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.AppleVarietyProfileTargetCandidates;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.QueryAppleVarietyProfileTargetCandidatesUseCase;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateCriteria;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateView;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessmentRequest;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessor;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidate;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateReference;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesCommand;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesUseCase;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.usecase.ProfileTargetAnalysisScenarioResolver;
import org.craft.mcs.cap.core.profileanalysis.graph.application.port.in.EvaluateJolicoeurSugarAcidityGraphUseCase;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringInput;
import org.craft.mcs.cap.shared.application.query.pagination.ChunkedCursorPageCollector;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.jspecify.annotations.NonNull;
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

  private final @NonNull AppleVarietyAnalysisCandidateQuery candidateQuery;
  private final @NonNull EvaluateJolicoeurSugarAcidityGraphUseCase graphEvaluator;
  private final @NonNull ScoreProfileTargetDistanceCandidatesUseCase scoreProfileTargetDistanceCandidatesUseCase;
  private final @NonNull ProfileTargetAnalysisScenarioResolver scoringConfigurationResolver;
  private final @NonNull CompositionContributionRoleAssessor contributionRoleAssessor;

  @Override
  public @NonNull AppleVarietyProfileTargetCandidates query(
      @NonNull AppleVarietyProfileTargetCandidateQuery query
  ) {
    requireNonNull(query, "apple variety profile target candidate query cannot be null");

    final var candidates = ChunkedCursorPageCollector.collect(
        CursorPageRequest.firstPage(query.limit()),
        (afterPosition, limit) -> candidateQuery.findCandidates(
            new AppleVarietyAnalysisCandidateCriteria(
                query.filters().nameContains(),
                afterPosition,
                limit
            )
        ),
        QueryAppleVarietyProfileTargetCandidatesService::toPagePosition,
        variety -> toCandidateIfScorable(variety, query)
    ).items();

    final Map<@NonNull String, @NonNull ScorableAppleVarietyCandidate> candidatesByReference = candidates.stream()
        .collect(Collectors.toMap(
            candidate -> candidate.candidate().reference().id(),
            Function.identity()
        ));
    final var scores = scoreProfileTargetDistanceCandidatesUseCase.score(
        new ScoreProfileTargetDistanceCandidatesCommand(
            query.target(),
            scoringConfigurationResolver.resolve(query.analysisScenario(), query.weights()),
            candidates.stream()
                .map(ScorableAppleVarietyCandidate::candidate)
                .toList()
        )
    );

    return new AppleVarietyProfileTargetCandidates(
        query.analysisScenario(),
        scores.configuration().normalizerCode(),
        scores.scores().stream()
            .map(score -> {
              final var candidate = requireNonNull(
                  candidatesByReference.get(score.reference().id()),
                  "apple variety candidate is missing for score"
              );
              final var variety = candidate.variety();
              final var roleAssessmentRequest = new CompositionContributionRoleAssessmentRequest(
                  score.distance()
              );
              return new AppleVarietyProfileTargetCandidate(
                  variety.varietyId(),
                  variety.name(),
                  candidate.candidate().input(),
                  score.distance(),
                  contributionRoleAssessor.assess(roleAssessmentRequest)
              );
            })
            .toList()
    );
  }

  private @NonNull Optional<@NonNull ScorableAppleVarietyCandidate> toCandidateIfScorable(
      @NonNull AppleVarietyAnalysisCandidateView variety,
      @NonNull AppleVarietyProfileTargetCandidateQuery query
  ) {
    requireNonNull(variety, APPLE_VARIETY_REQUIRED);
    final var referenceProfile = variety.referenceProfile();
    final var graphEvaluation = graphEvaluator.evaluate(
        referenceProfile.specificGravity(),
        referenceProfile.titratableAcidity()
    );
    if (!graphEvaluation.plottable()) {
      return Optional.empty();
    }
    final var sourceZoneCriteria = query.sourceZoneCriteria();
    if (sourceZoneCriteria != null && !sourceZoneCriteria.matches(requireNonNull(graphEvaluation.zoneAssessment()))) {
      return Optional.empty();
    }
    final var specificGravity = referenceProfile.specificGravity();
    final var titratableAcidity = referenceProfile.titratableAcidity();
    if (specificGravity == null || titratableAcidity == null) {
      return Optional.empty();
    }

    return Optional.of(new ScorableAppleVarietyCandidate(
        variety,
        new ProfileTargetDistanceCandidate(
            referenceId(variety),
            new ProfileTargetScoringInput(
                specificGravity,
                titratableAcidity,
                referenceProfile.tannin()
            )
        )
    ));
  }

  private static @NonNull ProfileTargetDistanceCandidateReference referenceId(
      @NonNull AppleVarietyAnalysisCandidateView variety
  ) {
    requireNonNull(variety, APPLE_VARIETY_REQUIRED);
    return new ProfileTargetDistanceCandidateReference(
        REFERENCE_TYPE,
        variety.varietyId().value().toString()
    );
  }

  private static @NonNull AppleVarietyCatalogPagePosition toPagePosition(
      @NonNull AppleVarietyAnalysisCandidateView variety
  ) {
    requireNonNull(variety, APPLE_VARIETY_REQUIRED);
    return new AppleVarietyCatalogPagePosition(
        variety.name().value(),
        variety.varietyId().value()
    );
  }

  private record ScorableAppleVarietyCandidate(
      @NonNull AppleVarietyAnalysisCandidateView variety,
      @NonNull ProfileTargetDistanceCandidate candidate
  ) {

    private ScorableAppleVarietyCandidate {
      requireNonNull(variety, APPLE_VARIETY_REQUIRED);
      requireNonNull(candidate, "profile target distance candidate cannot be null");
    }
  }
}
