package org.craft.mcs.cap.appleprofile.analysis.application.profiletarget.usecase;

import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisFilters;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisPageQuery;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.port.out.AppleVarietyAnalysisProfilePageProvider;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.in.AppleVarietyProfileTargetCandidate;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.in.AppleVarietyProfileTargetCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.usecase.QueryAppleVarietyProfileTargetCandidatesService;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleContributionView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetDefinition;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetDistanceView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetFitConfiguration.AnalysisScenario;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetFitConfiguration.Normalizer;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetFitConfiguration.ObjectiveWeights;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.out.AppleTargetFitAnalyzer;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphView.MissingCharacteristic;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphView.Point;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphView.Zone;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphView.ZoneAssessment;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.port.out.AppleSugarAcidityGraphAnalyzer;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisName;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyReferenceProfile;
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
    final var profilePageProvider = mock(AppleVarietyAnalysisProfilePageProvider.class);
    when(profilePageProvider.findProfiles(any()))
        .thenReturn(List.of(unscorable, scorable))
        .thenReturn(List.of());
    final var graphEvaluator = mock(AppleSugarAcidityGraphAnalyzer.class);
    when(graphEvaluator.evaluate(any(), any()))
        .thenReturn(new AppleSugarAcidityGraphView(
            Set.of(
                MissingCharacteristic.SPECIFIC_GRAVITY,
                MissingCharacteristic.TITRATABLE_ACIDITY
            ),
            null,
            null
        ))
        .thenReturn(new AppleSugarAcidityGraphView(
            Set.of(),
            new Point(
                SpecificGravityValue.of("1.060"),
                TitratableAcidityValue.of("6.0")
            ),
            new ZoneAssessment(null)
        ));
    final var scoring = mock(AppleTargetFitAnalyzer.class);
    final var distance = mock(AppleTargetDistanceView.class);
    final var contribution = mock(AppleContributionView.class);
    when(scoring.score(any())).thenAnswer(invocation -> {
      final var command = invocation.getArgument(
          0,
          AppleTargetFitAnalyzer.Request.class
      );
      return new AppleTargetFitAnalyzer.Scores(
          Normalizer.EFFECTIVE_RANGE,
          command.candidates().stream()
              .map(candidate -> new AppleTargetFitAnalyzer.Score(
                  candidate.reference(),
                  distance,
                  contribution
              ))
              .toList()
      );
    });
    final var service = new QueryAppleVarietyProfileTargetCandidatesService(
        profilePageProvider,
        graphEvaluator,
        scoring
    );

    final var result = service.query(new AppleVarietyProfileTargetCandidateQuery(
        new AppleVarietyAnalysisFilters(null),
        null,
        AnalysisScenario.BLENDING_PLANNING,
        new AppleTargetDefinition(
            new AppleTargetDefinition.PredefinedTarget(Zone.PERFECT_APPLES),
            null
        ),
        ObjectiveWeights.balanced(),
        1
    ));

    assertThat(result.candidates())
        .singleElement()
        .returns(scorable.varietyId(), AppleVarietyProfileTargetCandidate::varietyId);
    final var criteriaCaptor = ArgumentCaptor.forClass(AppleVarietyAnalysisPageQuery.class);
    verify(profilePageProvider, times(2))
        .findProfiles(criteriaCaptor.capture());
    assertThat(criteriaCaptor.getAllValues().getFirst().limit()).isEqualTo(2);
  }

  private static @NonNull AppleVarietyAnalysisProfile candidate(
      @NonNull String name,
      @NonNull AppleVarietyReferenceProfile referenceProfile
  ) {
    return new AppleVarietyAnalysisProfile(
        AppleVarietyId.newId(),
        AppleVarietyAnalysisName.of(name),
        referenceProfile.specificGravity(),
        referenceProfile.titratableAcidity(),
        referenceProfile.ph(),
        referenceProfile.tannin()
    );
  }
}
