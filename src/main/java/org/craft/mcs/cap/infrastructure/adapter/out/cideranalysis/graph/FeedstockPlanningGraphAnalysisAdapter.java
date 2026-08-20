package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.graph;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.cideranalysis.graph.application.port.in.EvaluateJolicoeurSugarAcidityGraphUseCase;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.contract.FeedstockPlanningGraphView;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.port.out.FeedstockPlanningGraphAnalyzer;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;


/** ACL from Feedstock Planning graph requirements to CiderAnalysis. */
@Component
@RequiredArgsConstructor
public final class FeedstockPlanningGraphAnalysisAdapter implements FeedstockPlanningGraphAnalyzer {

  private final @NonNull EvaluateJolicoeurSugarAcidityGraphUseCase useCase;

  @Override
  public @NonNull FeedstockPlanningGraphView evaluate(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
      @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity
  ) {
    final var result = useCase.evaluate(specificGravity, titratableAcidity);
    final var point = result.point();
    final var zoneAssessment = result.zoneAssessment();
    return new FeedstockPlanningGraphView(
        result.missingCharacteristics().stream()
            .map(characteristic -> FeedstockPlanningGraphView.MissingCharacteristic.valueOf(
                characteristic.name()
            ))
            .collect(java.util.stream.Collectors.toUnmodifiableSet()),
        point == null ? null : new FeedstockPlanningGraphView.Point(
            point.specificGravity(),
            point.titratableAcidity()
        ),
        zoneAssessment == null ? null : zoneAssessment(zoneAssessment)
    );
  }

  private static FeedstockPlanningGraphView.@NonNull ZoneAssessment zoneAssessment(
      JolicoeurSugarAcidityGraphEvaluation.@NonNull ZoneAssessment assessment
  ) {
    final var zone = assessment.zone();
    return new FeedstockPlanningGraphView.ZoneAssessment(
        zone == null ? null : FeedstockPlanningGraphView.Zone.valueOf(zone.name())
    );
  }
}
