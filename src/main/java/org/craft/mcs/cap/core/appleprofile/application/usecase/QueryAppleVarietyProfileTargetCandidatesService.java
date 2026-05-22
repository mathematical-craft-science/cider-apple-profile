package org.craft.mcs.cap.core.appleprofile.application.usecase;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyProfileTargetCandidate;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyProfileTargetCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyProfileTargetCandidates;
import org.craft.mcs.cap.core.appleprofile.application.port.in.QueryAppleVarietyProfileTargetCandidatesUseCase;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietySugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietySugarAcidityPointCandidateRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.cideranalysis.composition.contribution.domain.CompositionContributionRoleAssessmentRequest;
import org.craft.mcs.cap.core.cideranalysis.composition.contribution.domain.CompositionContributionRoleAssessor;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidate;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateReference;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesCommand;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesUseCase;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessor;
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
 * Analysis read model for target-aware apple variety candidates.
 *
 * <p>This use case is intentionally separate from generic catalog graph endpoints. It requires a
 * selected profile target and enriches plottable varieties with target distance and contribution
 * role for analysis/workspace screens.
 */
@Component
@RequiredArgsConstructor
public final class QueryAppleVarietyProfileTargetCandidatesService
    implements QueryAppleVarietyProfileTargetCandidatesUseCase {

  private static final @NonNull String REFERENCE_TYPE = "apple-variety";

  private final @NonNull AppleVarietySugarAcidityPointCandidateRepository candidateRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull SugarAcidityZoneAssessor zoneAssessor;
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
              return new AppleVarietyProfileTargetCandidate(
                  variety.varietyId(),
                  variety.name(),
                  candidate.candidate().point(),
                  score.distance(),
                  contributionRoleAssessor.assess(new CompositionContributionRoleAssessmentRequest(candidate.axes(), query.target()))
              );
            })
            .toList()
    );
  }

  private @NonNull Optional<@NonNull ScorableAppleVarietyCandidate> toCandidateIfScorable(
      @NonNull AppleVariety variety,
      @NonNull AppleVarietyProfileTargetCandidateQuery query
  ) {
    requireNonNull(variety, "apple variety cannot be null");
    final var axes = axisExtractor.extract(variety.chemicalProfile());
    if (!capabilityAssessor.assess(axes, ChemicalProfileCapability.SUGAR_ACIDITY_PLOT).capable()) {
      return Optional.empty();
    }
    if (!matchesCapability(axes, query.filters().capability())) {
      return Optional.empty();
    }

    final var assessment = zoneAssessor.assess(axes).orElse(null); // NOSONAR
    if (assessment == null || !matchesSugarAcidityZone(assessment, query.filters().sugarAcidityZone())) {
      return Optional.empty();
    }

    return Optional.of(new ScorableAppleVarietyCandidate(
        variety,
        axes,
        new ProfileTargetDistanceCandidate(
            referenceId(variety),
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
      @NonNull AppleVariety variety
  ) {
    requireNonNull(variety, "apple variety cannot be null");
    return new ProfileTargetDistanceCandidateReference(
        REFERENCE_TYPE,
        variety.varietyId().value().toString()
    );
  }

  private record ScorableAppleVarietyCandidate(
      @NonNull AppleVariety variety,
      @NonNull ChemicalProfileAxes axes,
      @NonNull ProfileTargetDistanceCandidate candidate
  ) {

    private ScorableAppleVarietyCandidate {
      requireNonNull(variety, "apple variety cannot be null");
      requireNonNull(axes, "chemical profile axes cannot be null");
      requireNonNull(candidate, "profile target distance candidate cannot be null");
    }
  }
}
