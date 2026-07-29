package org.craft.mcs.cap.appleprofile.application.profiletarget.usecase;

import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.appleprofile.application.analysis.common.model.AppleVarietyAnalysisFilters;
import org.craft.mcs.cap.core.appleprofile.application.analysis.common.service.AppleVarietyGraphCandidateMatcher;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateCriteria;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateView;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.AppleVarietyProfileTargetCandidate;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.AppleVarietyProfileTargetCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.usecase.QueryAppleVarietyProfileTargetCandidatesService;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRole;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessment;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessor;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.profileanalysis.graph.application.port.in.EvaluateJolicoeurSugarAcidityGraphUseCase;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateScore;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateScores;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesCommand;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesUseCase;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.usecase.ProfileTargetAnalysisScenarioResolver;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class QueryAppleVarietyProfileTargetCandidatesServiceTest {

  @Test
  void appliesLimitAfterDiscardingUnscorableCatalogRows() {
    final var unscorable = candidate("No SG or TA", new AppleVarietyReferenceProfile(
        null,
        null,
        new PointCharacteristicValue<>(PhValue.of("3.4")),
        null
    ));
    final var scorable = candidate("Scorable", new AppleVarietyReferenceProfile(
        new PointCharacteristicValue<>(SpecificGravityValue.of("1.060")),
        new PointCharacteristicValue<>(TitratableAcidityValue.of("6.0")),
        null,
        null
    ));
    final var candidateQuery = mock(AppleVarietyAnalysisCandidateQuery.class);
    when(candidateQuery.findCandidates(any()))
        .thenReturn(List.of(unscorable, scorable))
        .thenReturn(List.of());
    final var graphEvaluator = mock(EvaluateJolicoeurSugarAcidityGraphUseCase.class);
    when(graphEvaluator.evaluate(any(), any()))
        .thenReturn(JolicoeurSugarAcidityGraphEvaluation.unplottable(
            Set.of(
                GraphMissingCharacteristic.SPECIFIC_GRAVITY,
                GraphMissingCharacteristic.TITRATABLE_ACIDITY
            )
        ))
        .thenReturn(JolicoeurSugarAcidityGraphEvaluation.plottable(
            new SugarAcidityGraphPoint(
                SpecificGravityValue.of("1.060"),
                TitratableAcidityValue.of("6.0")
            ),
            JolicoeurSugarAcidityZoneAssessment.unclassified()
        ));
    final var configuration = new ProfileTargetScoringConfiguration(
        ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
        ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
        ProfileTargetObjectiveWeights.balanced()
    );
    final var scoring = mock(ScoreProfileTargetDistanceCandidatesUseCase.class);
    final var distance = mock(ProfileTargetDistance.class);
    when(scoring.score(any())).thenAnswer(invocation -> {
      final var command = invocation.getArgument(
          0,
          ScoreProfileTargetDistanceCandidatesCommand.class
      );
      return new ProfileTargetDistanceCandidateScores(
          configuration,
          command.candidates().stream()
              .map(candidate -> new ProfileTargetDistanceCandidateScore(candidate.reference(), distance))
              .toList()
      );
    });
    final var scenarioResolver = mock(ProfileTargetAnalysisScenarioResolver.class);
    when(scenarioResolver.resolve(any(), any())).thenReturn(configuration);
    final var contributionRoleAssessor = mock(CompositionContributionRoleAssessor.class);
    when(contributionRoleAssessor.assess(any())).thenReturn(new CompositionContributionRoleAssessment(
        List.of(CompositionContributionRole.BALANCED_BASE),
        List.of(),
        Set.of()
    ));
    final var service = new QueryAppleVarietyProfileTargetCandidatesService(
        candidateQuery,
        graphEvaluator,
        new AppleVarietyGraphCandidateMatcher(),
        scoring,
        scenarioResolver,
        contributionRoleAssessor
    );

    final var result = service.query(new AppleVarietyProfileTargetCandidateQuery(
        new AppleVarietyAnalysisFilters(null, null, null),
        ProfileTargetAnalysisScenario.BLENDING_PLANNING,
        mock(ProfileTargetObjective.class),
        ProfileTargetObjectiveWeights.balanced(),
        1
    ));

    assertThat(result.candidates())
        .singleElement()
        .returns(scorable.varietyId(), AppleVarietyProfileTargetCandidate::varietyId);
    final var criteriaCaptor = ArgumentCaptor.forClass(AppleVarietyAnalysisCandidateCriteria.class);
    verify(candidateQuery, times(2))
        .findCandidates(criteriaCaptor.capture());
    assertThat(criteriaCaptor.getAllValues().getFirst().limit()).isEqualTo(2);
  }

  private static @NonNull AppleVarietyAnalysisCandidateView candidate(
      @NonNull String name,
      @NonNull AppleVarietyReferenceProfile referenceProfile
  ) {
    return new AppleVarietyAnalysisCandidateView(
        AppleVarietyId.newId(),
        AppleVarietyName.of(name),
        referenceProfile
    );
  }
}
