package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.graph;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.cideranalysis.graph.application.port.in.EvaluateJolicoeurSugarAcidityGraphUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.contract.FeedstockSugarAcidityGraphView;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.port.out.FeedstockSugarAcidityGraphAnalyzer;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;


/** ACL from Feedstock graph scenarios to CiderAnalysis. */
@Component
@RequiredArgsConstructor
public final class FeedstockSugarAcidityGraphAnalysisAdapter implements FeedstockSugarAcidityGraphAnalyzer {

  private final @NonNull EvaluateJolicoeurSugarAcidityGraphUseCase useCase;

  @Override
  public @NonNull FeedstockSugarAcidityGraphView evaluate(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
      @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity
  ) {
    final var result = useCase.evaluate(specificGravity, titratableAcidity);
    final var point = result.point();
    final var zoneAssessment = result.zoneAssessment();
    return new FeedstockSugarAcidityGraphView(
        result.missingCharacteristics().stream()
            .map(characteristic -> FeedstockSugarAcidityGraphView.MissingCharacteristic.valueOf(
                characteristic.name()
            ))
            .collect(java.util.stream.Collectors.toUnmodifiableSet()),
        point == null
            ? null
            : new FeedstockSugarAcidityGraphView.Point(
                point.specificGravity(),
                point.titratableAcidity()
            ),
        zoneAssessment == null ? null : zoneAssessment(zoneAssessment)
    );
  }

  private static FeedstockSugarAcidityGraphView.@NonNull ZoneAssessment zoneAssessment(
      JolicoeurSugarAcidityGraphEvaluation.@NonNull ZoneAssessment assessment
  ) {
    final var zone = assessment.zone();
    return new FeedstockSugarAcidityGraphView.ZoneAssessment(
        zone == null ? null : FeedstockSugarAcidityGraphView.Zone.valueOf(zone.name())
    );
  }
}
