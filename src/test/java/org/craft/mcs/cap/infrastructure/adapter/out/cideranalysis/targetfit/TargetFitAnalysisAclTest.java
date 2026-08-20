package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.targetfit;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.AppleTargetFitUnavailableException;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetCharacteristics;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetDefinition;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetFitConfiguration;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.out.AppleTargetFitAnalyzer;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.TargetFitTargetUnavailableException;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in.ScoreTargetFitUseCase;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.FeedstockTargetFitUnavailableException;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.contract.FeedstockPlanningGraphView.Zone;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetCharacteristics;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.out.FeedstockTargetFitAnalyzer;
import org.craft.mcs.cap.core.formulation.calculation.application.FormulationTargetFitUnavailableException;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculatedCompositionView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class TargetFitAnalysisAclTest {

  private static final RuntimeException CAUSE = new RuntimeException("missing target geometry");

  @Test
  void translatesProviderFailureForFeedstockPlanning() {
    final var provider = unavailableProvider();
    final var adapter = new FeedstockTargetFitAnalysisAdapter(provider);
    final var request = new FeedstockTargetFitAnalyzer.Request(
        new PredefinedFeedstockProfileTargetSelection(
            Zone.PERFECT_APPLES
        ),
        null,
        new FeedstockTargetFitConfiguration(
            FeedstockTargetFitConfiguration.AnalysisScenario.BLENDING_PLANNING,
            FeedstockTargetFitConfiguration.ObjectiveWeights.balanced()
        ),
        List.of(new FeedstockTargetFitAnalyzer.Candidate(
            "batch-1",
            new FeedstockTargetCharacteristics(
                PointCharacteristicValue.of(SpecificGravityValue.of("1.052")),
                PointCharacteristicValue.of(TitratableAcidityValue.of("6.2")),
                null
            )
        ))
    );

    assertThatThrownBy(() -> adapter.score(request))
        .isInstanceOf(FeedstockTargetFitUnavailableException.class)
        .hasMessage("target is unavailable")
        .hasRootCause(CAUSE);
  }

  @Test
  void translatesProviderFailureForAppleAnalysis() {
    final var provider = unavailableProvider();
    final var adapter = new AppleTargetFitAnalysisAdapter(provider);
    final var request = new AppleTargetFitAnalyzer.Request(
        new AppleTargetDefinition(
            new AppleTargetDefinition.PredefinedTarget(AppleSugarAcidityGraphView.Zone.PERFECT_APPLES),
            null
        ),
        new AppleTargetFitConfiguration(
            AppleTargetFitConfiguration.AnalysisScenario.BLENDING_PLANNING,
            AppleTargetFitConfiguration.ObjectiveWeights.balanced()
        ),
        List.of(new AppleTargetFitAnalyzer.Candidate(
            "variety-1",
            new AppleTargetCharacteristics(
                PointCharacteristicValue.of(SpecificGravityValue.of("1.052")),
                PointCharacteristicValue.of(TitratableAcidityValue.of("6.2")),
                null
            )
        ))
    );

    assertThatThrownBy(() -> adapter.score(request))
        .isInstanceOf(AppleTargetFitUnavailableException.class)
        .hasMessage("target is unavailable")
        .hasRootCause(CAUSE);
  }

  @Test
  void translatesProviderFailureForFormulation() {
    final var provider = unavailableProvider();
    final var adapter = new FormulationTargetFitAnalysisAdapter(provider);
    final var characteristics = new CalculatedCompositionView.Characteristics(
        SpecificGravityValue.of("1.052"),
        TitratableAcidityValue.of("6.2"),
        null,
        null
    );
    final var target = new FormulationTargetDefinition(
        new FormulationTargetDefinition.PredefinedTarget(FormulationTargetDefinition.Zone.PERFECT_APPLES),
        null,
        new FormulationTargetDefinition.ScoringConfiguration(
            FormulationTargetDefinition.Normalizer.EFFECTIVE_RANGE,
            FormulationTargetDefinition.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
            new FormulationTargetDefinition.Weights(BigDecimal.ONE, BigDecimal.ONE, null)
        )
    );

    assertThatThrownBy(() -> adapter.evaluate(characteristics, target))
        .isInstanceOf(FormulationTargetFitUnavailableException.class)
        .hasMessage("target is unavailable")
        .hasRootCause(CAUSE);
  }

  private static ScoreTargetFitUseCase unavailableProvider() {
    final var provider = mock(ScoreTargetFitUseCase.class);
    when(provider.score(any())).thenThrow(new TargetFitTargetUnavailableException(
        "target is unavailable",
        CAUSE
    ));
    return provider;
  }
}
