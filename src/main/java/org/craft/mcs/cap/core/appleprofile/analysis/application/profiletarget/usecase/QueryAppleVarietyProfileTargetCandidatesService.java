package org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.usecase;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisPagePosition;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisPageQuery;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.port.out.AppleVarietyAnalysisProfilePageProvider;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.in.AppleVarietyProfileTargetCandidate;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.in.AppleVarietyProfileTargetCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.in.AppleVarietyProfileTargetCandidates;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.in.QueryAppleVarietyProfileTargetCandidatesUseCase;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetCharacteristics;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetFitConfiguration;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.out.AppleTargetFitAnalyzer;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.out.AppleTargetFitAnalyzer.Candidate;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.port.out.AppleSugarAcidityGraphAnalyzer;
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

  private static final @NonNull String APPLE_VARIETY_REQUIRED = "apple variety cannot be null";

  private final @NonNull AppleVarietyAnalysisProfilePageProvider profilePageProvider;
  private final @NonNull AppleSugarAcidityGraphAnalyzer graphAnalyzer;
  private final @NonNull AppleTargetFitAnalyzer targetFitAnalyzer;

  @Override
  public @NonNull AppleVarietyProfileTargetCandidates query(
      @NonNull AppleVarietyProfileTargetCandidateQuery query
  ) {
    requireNonNull(query, "apple variety profile target candidate query cannot be null");

    final var candidates = ChunkedCursorPageCollector.collect(
        CursorPageRequest.firstPage(query.limit()),
        (afterPosition, limit) -> profilePageProvider.findProfiles(
            new AppleVarietyAnalysisPageQuery(
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
            candidate -> candidate.candidate().reference(),
            Function.identity()
        ));
    final var scores = targetFitAnalyzer.score(
        new AppleTargetFitAnalyzer.Request(
            query.target(),
            new AppleTargetFitConfiguration(query.analysisScenario(), query.weights()),
            candidates.stream()
                .map(ScorableAppleVarietyCandidate::candidate)
                .toList()
        )
    );

    return new AppleVarietyProfileTargetCandidates(
        query.analysisScenario(),
        scores.normalizer(),
        scores.scores().stream()
            .map(score -> {
              final var candidate = requireNonNull(
                  candidatesByReference.get(score.reference()),
                  "apple variety candidate is missing for score"
              );
              final var variety = candidate.variety();
              return new AppleVarietyProfileTargetCandidate(
                  variety.varietyId(),
                  variety.name(),
                  candidate.candidate().characteristics(),
                  score.distance(),
                  score.contribution()
              );
            })
            .toList()
    );
  }

  private @NonNull Optional<@NonNull ScorableAppleVarietyCandidate> toCandidateIfScorable(
      @NonNull AppleVarietyAnalysisProfile variety,
      @NonNull AppleVarietyProfileTargetCandidateQuery query
  ) {
    requireNonNull(variety, APPLE_VARIETY_REQUIRED);
    final var graphEvaluation = graphAnalyzer.evaluate(
        variety.specificGravity(),
        variety.titratableAcidity()
    );
    if (!graphEvaluation.plottable()) {
      return Optional.empty();
    }
    final var sourceZoneCriteria = query.sourceZoneCriteria();
    if (sourceZoneCriteria != null && !sourceZoneCriteria.matches(graphEvaluation)) {
      return Optional.empty();
    }
    final var specificGravity = variety.specificGravity();
    final var titratableAcidity = variety.titratableAcidity();
    if (specificGravity == null || titratableAcidity == null) {
      return Optional.empty();
    }

    return Optional.of(new ScorableAppleVarietyCandidate(
        variety,
        new AppleTargetFitAnalyzer.Candidate(
            referenceId(variety),
            new AppleTargetCharacteristics(
                specificGravity,
                titratableAcidity,
                variety.tannin()
            )
        )
    ));
  }

  private static @NonNull String referenceId(
      @NonNull AppleVarietyAnalysisProfile variety
  ) {
    requireNonNull(variety, APPLE_VARIETY_REQUIRED);
    return variety.varietyId().value().toString();
  }

  private static @NonNull AppleVarietyAnalysisPagePosition toPagePosition(
      @NonNull AppleVarietyAnalysisProfile variety
  ) {
    requireNonNull(variety, APPLE_VARIETY_REQUIRED);
    return new AppleVarietyAnalysisPagePosition(
        variety.name().value(),
        variety.varietyId().value()
    );
  }

  private record ScorableAppleVarietyCandidate(
      @NonNull AppleVarietyAnalysisProfile variety,
      @NonNull Candidate candidate
  ) {

    private ScorableAppleVarietyCandidate {
      requireNonNull(variety, APPLE_VARIETY_REQUIRED);
      requireNonNull(candidate, "profile target distance candidate cannot be null");
    }
  }
}
