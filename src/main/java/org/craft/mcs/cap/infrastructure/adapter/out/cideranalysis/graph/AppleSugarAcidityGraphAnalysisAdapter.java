package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.graph;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.port.out.AppleSugarAcidityGraphAnalyzer;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.cideranalysis.graph.application.port.in.EvaluateJolicoeurSugarAcidityGraphUseCase;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;


/** Shared ACL from AppleProfile graph consumers to CiderAnalysis. */
@Component
@RequiredArgsConstructor
public final class AppleSugarAcidityGraphAnalysisAdapter implements AppleSugarAcidityGraphAnalyzer {

  private final @NonNull EvaluateJolicoeurSugarAcidityGraphUseCase useCase;

  @Override
  public @NonNull AppleSugarAcidityGraphView evaluate(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
      @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity
  ) {
    final var result = useCase.evaluate(specificGravity, titratableAcidity);
    final var point = result.point();
    final var zoneAssessment = result.zoneAssessment();
    return new AppleSugarAcidityGraphView(
        result.missingCharacteristics().stream()
            .map(characteristic -> AppleSugarAcidityGraphView.MissingCharacteristic.valueOf(
                characteristic.name()
            ))
            .collect(java.util.stream.Collectors.toUnmodifiableSet()),
        point == null
            ? null
            : new AppleSugarAcidityGraphView.Point(
                point.specificGravity(),
                point.titratableAcidity()
            ),
        zoneAssessment == null ? null : zoneAssessment(zoneAssessment)
    );
  }

  private static AppleSugarAcidityGraphView.@NonNull ZoneAssessment zoneAssessment(
      JolicoeurSugarAcidityGraphEvaluation.@NonNull ZoneAssessment assessment
  ) {
    final var zone = assessment.zone();
    return new AppleSugarAcidityGraphView.ZoneAssessment(
        zone == null ? null : AppleSugarAcidityGraphView.Zone.valueOf(zone.name())
    );
  }
}
